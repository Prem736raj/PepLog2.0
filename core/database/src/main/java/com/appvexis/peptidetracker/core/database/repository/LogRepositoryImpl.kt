package com.appvexis.peptidetracker.core.database.repository

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
import com.appvexis.peptidetracker.core.model.InjectionSiteArea
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
    private val logDao: LogDao,
    private val protocolDao: ProtocolDao,
    private val inventoryDao: InventoryDao,
    private val analyticsRepository: AnalyticsRepository
) : LogRepository {

    // Helper to find protocol ID for a compound
    private suspend fun getProtocolIdForCompound(compoundId: String): String? {
        return protocolDao.getCompoundById(compoundId)?.protocolId
    }

    // Helper to trigger recompute for a compound
    private suspend fun triggerRecomputeForCompound(compoundId: String) {
        getProtocolIdForCompound(compoundId)?.let { protocolId ->
            analyticsRepository.recomputeAnalyticsForProtocol(protocolId)
        }
    }

    // Helper to auto-deduct volume from active vial when dose is logged as TAKEN
    private suspend fun deductInventoryForDose(compoundId: String, doseAmount: Double, doseUnit: DoseUnit) {
        val compound = protocolDao.getCompoundById(compoundId)
        val peptideId = compound?.peptideId ?: compoundId
        val doseMg = when (doseUnit) {
            DoseUnit.MG -> doseAmount
            DoseUnit.MCG -> doseAmount / 1000.0
            // IU has no universal conversion to milligrams; never deduct it as if it did.
            DoseUnit.IU -> return
        }
        val activeVial = inventoryDao.getActiveInUseVialForPeptide(peptideId)
        val concentration = activeVial?.concentrationMgMl
        if (activeVial != null && concentration != null && concentration > 0.0) {
            val volumeToDeduct = doseMg / concentration
            if (!volumeToDeduct.isFinite() || volumeToDeduct <= 0.0) return
            val updatedRows = inventoryDao.deductRemainingVolume(activeVial.id, volumeToDeduct)
            val newVolume = inventoryDao.getInventoryItemByIdSync(activeVial.id)?.remainingVolumeMl
            if (updatedRows > 0 && newVolume != null && newVolume <= 0.001) {
                inventoryDao.updateInventoryStatus(activeVial.id, InventoryStatus.EMPTY.name)
            }
        }
    }

    // Dose logs
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
        requireCompoundExists(log.protocolCompoundId)
        validateDoseLog(log)
        logDao.insertDoseLog(log.toEntity())
        if (log.status == DoseStatus.TAKEN) {
            deductInventoryForDose(log.protocolCompoundId, log.doseAmount, log.doseUnit)
        }
        triggerRecomputeForCompound(log.protocolCompoundId)
    }

    override suspend fun insertDoseLogs(logs: List<DoseLog>) {
        if (logs.isEmpty()) return
        logs.forEach { log ->
            requireCompoundExists(log.protocolCompoundId)
            validateDoseLog(log)
        }
        logDao.insertDoseLogs(logs.map { it.toEntity() })
        for (compoundId in logs.map { it.protocolCompoundId }.distinct()) {
            triggerRecomputeForCompound(compoundId)
        }
    }

    override suspend fun updateDoseLog(log: DoseLog) {
        requireCompoundExists(log.protocolCompoundId)
        validateDoseLog(log)
        logDao.updateDoseLog(log.toEntity())
        triggerRecomputeForCompound(log.protocolCompoundId)
    }

    override suspend fun deleteDoseLog(id: String) {
        val log = logDao.getDoseLogById(id)
        logDao.deleteDoseLog(id)
        log?.let { triggerRecomputeForCompound(it.protocolCompoundId) }
    }

    override suspend fun logDoseTaken(id: String, actualTime: Long, site: String?, side: String?) {
        // The conditional update is important: a double tap, retry, or two
        // simultaneous UI callers can only transition a pending dose once.
        val updatedRows = logDao.logDoseTakenIfPending(id, actualTime, site, side)
        if (updatedRows == 0) return
        val log = logDao.getDoseLogById(id)
        log?.let {
            deductInventoryForDose(it.protocolCompoundId, it.doseAmount, it.doseUnit)
            triggerRecomputeForCompound(it.protocolCompoundId)
        }
    }

    // Injection Site logs
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
        validateInjectionSiteLog(log)
        logDao.insertSiteLog(log.toEntity())
        log.doseLogId?.let { doseLogId ->
            logDao.getDoseLogById(doseLogId)?.let { doseLog ->
                triggerRecomputeForCompound(doseLog.protocolCompoundId)
            }
        }
    }

    override suspend fun updateSiteLog(log: InjectionSiteLog) {
        validateInjectionSiteLog(log)
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

    // Side Effect logs
    override fun getSideEffectLogs(): Flow<List<SideEffectLog>> {
        return logDao.getSideEffectLogs().map { list -> list.map { it.toDomain() } }
    }

    override fun getSideEffectLogsForProtocol(protocolId: String): Flow<List<SideEffectLog>> {
        return logDao.getSideEffectLogsForProtocol(protocolId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertSideEffectLog(log: SideEffectLog) {
        validateSideEffectLog(log)
        logDao.insertSideEffectLog(log.toEntity())
        log.protocolId?.let { analyticsRepository.recomputeAnalyticsForProtocol(it) }
    }

    override suspend fun deleteSideEffectLog(id: String) {
        val log = logDao.getSideEffectLogById(id)
        logDao.deleteSideEffectLog(id)
        log?.protocolId?.let { analyticsRepository.recomputeAnalyticsForProtocol(it) }
    }

    // Biomarker logs
    override fun getBiomarkerLogs(): Flow<List<BiomarkerLog>> {
        return logDao.getBiomarkerLogs().map { list -> list.map { it.toDomain() } }
    }

    override fun getBiomarkerLogsForProtocol(protocolId: String): Flow<List<BiomarkerLog>> {
        return logDao.getBiomarkerLogsForProtocol(protocolId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertBiomarkerLog(log: BiomarkerLog) {
        validateBiomarkerLog(log)
        logDao.insertBiomarkerLog(log.toEntity())
        log.protocolId?.let { analyticsRepository.recomputeAnalyticsForProtocol(it) }
    }

    override suspend fun deleteBiomarkerLog(id: String) {
        val log = logDao.getBiomarkerLogById(id)
        logDao.deleteBiomarkerLog(id)
        log?.protocolId?.let { analyticsRepository.recomputeAnalyticsForProtocol(it) }
    }

    // Progress Photos
    override fun getProgressPhotos(): Flow<List<ProgressPhoto>> {
        return logDao.getProgressPhotos().map { list -> list.map { it.toDomain() } }
    }

    override fun getProgressPhotosForProtocol(protocolId: String): Flow<List<ProgressPhoto>> {
        return logDao.getProgressPhotosForProtocol(protocolId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertProgressPhoto(photo: ProgressPhoto) {
        require(photo.photoUri.isNotBlank()) { "A progress photo must reference an image" }
        require(photo.date >= 0L) { "Photo date must be a valid timestamp" }
        logDao.insertProgressPhoto(photo.toEntity())
        photo.protocolId?.let { analyticsRepository.recomputeAnalyticsForProtocol(it) }
    }

    override suspend fun deleteProgressPhoto(id: String) {
        val photo = logDao.getProgressPhotoById(id)
        logDao.deleteProgressPhoto(id)
        photo?.protocolId?.let { analyticsRepository.recomputeAnalyticsForProtocol(it) }
    }

    // Calculator Presets
    override fun getCalculatorPresets(): Flow<List<CalculatorPreset>> {
        return logDao.getCalculatorPresets().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertCalculatorPreset(preset: CalculatorPreset) {
        logDao.insertCalculatorPreset(preset.toEntity())
    }

    override suspend fun deleteCalculatorPreset(id: String) {
        logDao.deleteCalculatorPreset(id)
    }

    private suspend fun requireCompoundExists(compoundId: String) {
        require(compoundId.isNotBlank()) { "A dose must reference a protocol compound" }
        require(protocolDao.getCompoundById(compoundId) != null) {
            "The selected protocol compound no longer exists"
        }
    }

    private fun validateDoseLog(log: DoseLog) {
        require(log.scheduledTime >= 0L) { "Dose schedule must be a valid timestamp" }
        require(log.createdAt >= 0L) { "Dose creation time must be a valid timestamp" }
        val actualTime = log.actualTime
        require(actualTime == null || actualTime >= 0L) {
            "Dose time must be a valid timestamp"
        }
        require(log.doseAmount.isFinite() && log.doseAmount > 0.0) {
            "Dose amount must be a finite value greater than zero"
        }
    }

    private fun validateInjectionSiteLog(log: InjectionSiteLog) {
        require(InjectionSiteArea.fromString(log.bodyArea) != null) {
            "Select a valid injection site"
        }
        require(log.timestamp >= 0L) { "Injection time must be a valid timestamp" }
        require(log.painLevel == null || log.painLevel in 1..5) {
            "Pain level must be between 1 and 5"
        }
    }

    private fun validateSideEffectLog(log: SideEffectLog) {
        require(log.type in setOf("side_effect", "mood", "energy", "sleep", "pain", "libido")) {
            "Unsupported wellness log type"
        }
        require(log.severity in 1..10) { "Severity must be between 1 and 10" }
        require(log.date >= 0L && log.createdAt >= 0L) {
            "Wellness log times must be valid timestamps"
        }
        if (log.type == "side_effect") {
            require(!log.category.isNullOrBlank()) { "A side effect needs a category" }
        }
    }

    private fun validateBiomarkerLog(log: BiomarkerLog) {
        require(log.biomarkerName.isNotBlank()) { "A biomarker needs a name" }
        require(log.unit.isNotBlank()) { "A biomarker needs a unit" }
        require(log.value.isFinite()) { "Biomarker value must be finite" }
        require(log.date >= 0L) { "Biomarker date must be a valid timestamp" }
    }
}
