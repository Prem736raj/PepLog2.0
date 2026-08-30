package com.appvexis.peptidetracker.core.database.repository

import androidx.room.withTransaction
import com.appvexis.peptidetracker.core.database.PepLogDatabase
import com.appvexis.peptidetracker.core.database.dao.InventoryDao
import com.appvexis.peptidetracker.core.database.dao.LogDao
import com.appvexis.peptidetracker.core.database.dao.ProtocolDao
import com.appvexis.peptidetracker.core.database.entity.toDomain
import com.appvexis.peptidetracker.core.database.entity.toEntity
import com.appvexis.peptidetracker.core.model.BiomarkerLog
import com.appvexis.peptidetracker.core.model.CalculatorPreset
import com.appvexis.peptidetracker.core.model.DoseLog
import com.appvexis.peptidetracker.core.model.DoseStatus
import com.appvexis.peptidetracker.core.model.DoseUnit
import com.appvexis.peptidetracker.core.model.InjectionSiteLog
import com.appvexis.peptidetracker.core.model.InventoryStatus
import com.appvexis.peptidetracker.core.model.ProgressPhoto
import com.appvexis.peptidetracker.core.model.SideEffectLog
import com.appvexis.peptidetracker.core.model.repository.AnalyticsRepository
import com.appvexis.peptidetracker.core.model.repository.LogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Database implementation of [LogRepository] mapping database operations to Domain.
 * Automatically triggers analytics updates when new metrics or doses are logged.
 */
@Singleton
class LogRepositoryImpl @Inject constructor(
    private val database: PepLogDatabase,
    private val logDao: LogDao,
    private val protocolDao: ProtocolDao,
    private val inventoryDao: InventoryDao,
    private val analyticsRepository: AnalyticsRepository
) : LogRepository {

    private suspend fun getProtocolIdForCompound(compoundId: String): String? {
        return protocolDao.getCompoundById(compoundId)?.protocolId
    }

    private suspend fun triggerRecomputeForCompound(compoundId: String) {
        getProtocolIdForCompound(compoundId)?.let { protocolId ->
            analyticsRepository.recomputeAnalyticsForProtocol(protocolId)
        }
    }

    /**
     * Deducts volume only when a dose can be converted to milligrams using the
     * current inventory schema. IU is intentionally not converted: IU is a
     * biological-activity unit and has no universal mg conversion.
     */
    private suspend fun deductInventoryForDose(
        compoundId: String,
        doseAmount: Double,
        doseUnit: DoseUnit
    ) {
        if (!doseAmount.isFinite() || doseAmount <= 0.0) return

        val doseMg = when (doseUnit) {
            DoseUnit.MG -> doseAmount
            DoseUnit.MCG -> doseAmount / 1000.0
            DoseUnit.IU -> return
        }

        val compound = protocolDao.getCompoundById(compoundId) ?: return
        val activeVial = inventoryDao.getActiveInUseVialForPeptide(compound.peptideId) ?: return
        val concentration = activeVial.concentrationMgMl ?: return
        if (!concentration.isFinite() || concentration <= 0.0) return

        val currentVolume = activeVial.remainingVolumeMl ?: activeVial.bacWaterMl ?: return
        if (!currentVolume.isFinite() || currentVolume < 0.0) return

        val volumeToDeduct = doseMg / concentration
        if (!volumeToDeduct.isFinite() || volumeToDeduct <= 0.0) return

        val newVolume = (currentVolume - volumeToDeduct).coerceAtLeast(0.0)
        inventoryDao.updateRemainingVolume(activeVial.id, newVolume)
        if (newVolume <= EMPTY_VOLUME_EPSILON_ML) {
            inventoryDao.updateInventoryStatus(activeVial.id, InventoryStatus.EMPTY.name)
        }
    }

    override fun getDoseLogs(startDate: Long, endDate: Long): Flow<List<DoseLog>> {
        return logDao.getDoseLogs(startDate, endDate).map { list -> list.map { it.toDomain() } }
    }

    override fun getDoseLogsForCompound(compoundId: String): Flow<List<DoseLog>> {
        return logDao.getDoseLogsForCompound(compoundId).map { list -> list.map { it.toDomain() } }
    }

    override fun getPendingDoseLogs(currentTime: Long): Flow<List<DoseLog>> {
        return logDao.getPendingDoseLogs(currentTime).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertDoseLog(log: DoseLog) {
        require(log.doseAmount.isFinite() && log.doseAmount > 0.0) {
            "Dose amount must be a positive finite value"
        }
        require(protocolDao.getCompoundById(log.protocolCompoundId) != null) {
            "Dose log must reference an existing protocol compound"
        }

        database.withTransaction {
            val previous = logDao.getDoseLogById(log.id)
            logDao.insertDoseLog(log.toEntity())
            if (log.status == DoseStatus.TAKEN && previous?.status != DoseStatus.TAKEN) {
                deductInventoryForDose(log.protocolCompoundId, log.doseAmount, log.doseUnit)
            }
        }
        triggerRecomputeForCompound(log.protocolCompoundId)
    }

    override suspend fun updateDoseLog(log: DoseLog) {
        require(log.doseAmount.isFinite() && log.doseAmount > 0.0) {
            "Dose amount must be a positive finite value"
        }

        database.withTransaction {
            val previous = logDao.getDoseLogById(log.id)
            logDao.updateDoseLog(log.toEntity())
            if (previous?.status != DoseStatus.TAKEN && log.status == DoseStatus.TAKEN) {
                deductInventoryForDose(log.protocolCompoundId, log.doseAmount, log.doseUnit)
            }
        }
        triggerRecomputeForCompound(log.protocolCompoundId)
    }

    override suspend fun deleteDoseLog(id: String) {
        val log = logDao.getDoseLogById(id)
        logDao.deleteDoseLog(id)
        // We intentionally do not "refund" inventory here. DoseLog currently does
        // not store which physical vial was used, so restoring an arbitrary active
        // vial could corrupt inventory history. A future schema should link each
        // taken dose to its inventory item before reversible deduction is offered.
        log?.let { triggerRecomputeForCompound(it.protocolCompoundId) }
    }

    override suspend fun logDoseTaken(id: String, actualTime: Long, site: String?, side: String?) {
        var changedCompoundId: String? = null

        database.withTransaction {
            val before = logDao.getDoseLogById(id) ?: return@withTransaction
            val rowsChanged = logDao.logDoseTaken(id, actualTime, site, side)
            if (rowsChanged == 1) {
                deductInventoryForDose(before.protocolCompoundId, before.doseAmount, before.doseUnit)
                changedCompoundId = before.protocolCompoundId
            }
        }

        changedCompoundId?.let { triggerRecomputeForCompound(it) }
    }

    override fun getInjectionSiteLogs(): Flow<List<InjectionSiteLog>> {
        return logDao.getInjectionSiteLogs().map { list -> list.map { it.toDomain() } }
    }

    override fun getInjectionSiteLogsByArea(bodyArea: String): Flow<List<InjectionSiteLog>> {
        return logDao.getInjectionSiteLogsByArea(bodyArea).map { list -> list.map { it.toDomain() } }
    }

    override fun getRecentInjectionSiteLogs(sinceTimestamp: Long): Flow<List<InjectionSiteLog>> {
        return logDao.getRecentInjectionSiteLogs(sinceTimestamp).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getInjectionSiteFrequency(sinceTimestamp: Long): Map<String, Int> {
        return logDao.getInjectionSiteFrequency(sinceTimestamp).associate { it.bodyArea to it.count }
    }

    override suspend fun insertSiteLog(log: InjectionSiteLog) {
        logDao.insertSiteLog(log.toEntity())
        log.doseLogId?.let { doseLogId ->
            logDao.getDoseLogById(doseLogId)?.let { doseLog ->
                triggerRecomputeForCompound(doseLog.protocolCompoundId)
            }
        }
    }

    override suspend fun updateSiteLog(log: InjectionSiteLog) {
        logDao.updateSiteLog(log.toEntity())
        log.doseLogId?.let { doseLogId ->
            logDao.getDoseLogById(doseLogId)?.let { doseLog ->
                triggerRecomputeForCompound(doseLog.protocolCompoundId)
            }
        }
    }

    override suspend fun deleteSiteLog(id: String) {
        logDao.deleteSiteLog(id)
    }

    override fun getSideEffectLogs(): Flow<List<SideEffectLog>> {
        return logDao.getSideEffectLogs().map { list -> list.map { it.toDomain() } }
    }

    override fun getSideEffectLogsForProtocol(protocolId: String): Flow<List<SideEffectLog>> {
        return logDao.getSideEffectLogsForProtocol(protocolId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertSideEffectLog(log: SideEffectLog) {
        logDao.insertSideEffectLog(log.toEntity())
        log.protocolId?.let { analyticsRepository.recomputeAnalyticsForProtocol(it) }
    }

    override suspend fun deleteSideEffectLog(id: String) {
        val log = logDao.getSideEffectLogById(id)
        logDao.deleteSideEffectLog(id)
        log?.protocolId?.let { analyticsRepository.recomputeAnalyticsForProtocol(it) }
    }

    override fun getBiomarkerLogs(): Flow<List<BiomarkerLog>> {
        return logDao.getBiomarkerLogs().map { list -> list.map { it.toDomain() } }
    }

    override fun getBiomarkerLogsForProtocol(protocolId: String): Flow<List<BiomarkerLog>> {
        return logDao.getBiomarkerLogsForProtocol(protocolId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertBiomarkerLog(log: BiomarkerLog) {
        logDao.insertBiomarkerLog(log.toEntity())
        log.protocolId?.let { analyticsRepository.recomputeAnalyticsForProtocol(it) }
    }

    override suspend fun deleteBiomarkerLog(id: String) {
        val log = logDao.getBiomarkerLogById(id)
        logDao.deleteBiomarkerLog(id)
        log?.protocolId?.let { analyticsRepository.recomputeAnalyticsForProtocol(it) }
    }

    override fun getProgressPhotos(): Flow<List<ProgressPhoto>> {
        return logDao.getProgressPhotos().map { list -> list.map { it.toDomain() } }
    }

    override fun getProgressPhotosForProtocol(protocolId: String): Flow<List<ProgressPhoto>> {
        return logDao.getProgressPhotosForProtocol(protocolId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertProgressPhoto(photo: ProgressPhoto) {
        logDao.insertProgressPhoto(photo.toEntity())
        photo.protocolId?.let { analyticsRepository.recomputeAnalyticsForProtocol(it) }
    }

    override suspend fun deleteProgressPhoto(id: String) {
        val photo = logDao.getProgressPhotoById(id)
        logDao.deleteProgressPhoto(id)
        photo?.protocolId?.let { analyticsRepository.recomputeAnalyticsForProtocol(it) }
    }

    override fun getCalculatorPresets(): Flow<List<CalculatorPreset>> {
        return logDao.getCalculatorPresets().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertCalculatorPreset(preset: CalculatorPreset) {
        require(
            preset.vialStrengthMg.isFinite() && preset.vialStrengthMg > 0.0 &&
                preset.bacWaterMl.isFinite() && preset.bacWaterMl > 0.0 &&
                preset.desiredDoseMg.isFinite() && preset.desiredDoseMg > 0.0 &&
                preset.desiredDoseMg <= preset.vialStrengthMg
        ) { "Calculator preset contains invalid values" }
        logDao.insertCalculatorPreset(preset.toEntity())
    }

    override suspend fun deleteCalculatorPreset(id: String) {
        logDao.deleteCalculatorPreset(id)
    }

    private companion object {
        const val EMPTY_VOLUME_EPSILON_ML = 0.001
    }
}
