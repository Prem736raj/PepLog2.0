package com.appvexis.peptidetracker.core.backup.export

import androidx.room.withTransaction
import com.appvexis.peptidetracker.core.backup.model.BackupData
import com.appvexis.peptidetracker.core.database.PepLogDatabase
import com.appvexis.peptidetracker.core.database.entity.BiomarkerLogEntity
import com.appvexis.peptidetracker.core.database.entity.CalculatorPresetEntity
import com.appvexis.peptidetracker.core.database.entity.DoseLogEntity
import com.appvexis.peptidetracker.core.database.entity.HealthMetricEntity
import com.appvexis.peptidetracker.core.database.entity.InjectionSiteLogEntity
import com.appvexis.peptidetracker.core.database.entity.InventoryItemEntity
import com.appvexis.peptidetracker.core.database.entity.ProgressPhotoEntity
import com.appvexis.peptidetracker.core.database.entity.ProtocolCompoundEntity
import com.appvexis.peptidetracker.core.database.entity.ProtocolEntity
import com.appvexis.peptidetracker.core.database.entity.SideEffectLogEntity
import com.appvexis.peptidetracker.core.model.AdminRoute
import com.appvexis.peptidetracker.core.model.DoseStatus
import com.appvexis.peptidetracker.core.model.DoseUnit
import com.appvexis.peptidetracker.core.model.FrequencyType
import com.appvexis.peptidetracker.core.model.HealthMetricType
import com.appvexis.peptidetracker.core.model.HealingStatus
import com.appvexis.peptidetracker.core.model.InventoryStatus
import com.appvexis.peptidetracker.core.model.ProtocolStatus
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/** Validates and atomically applies a complete JSON backup to Room. */
@Singleton
class DatabaseImporter @Inject constructor(
    private val database: PepLogDatabase
) {
    suspend fun restore(data: BackupData) {
        validate(data)

        database.withTransaction {
            // Delete children first so all foreign-key constraints remain valid.
            database.analyticsDao().deleteAllDailySummaries()
            database.analyticsDao().deleteAllProtocolSummaries()
            database.logDao().deleteAllSiteLogs()
            database.logDao().deleteAllDoseLogs()
            database.logDao().deleteAllSideEffectLogs()
            database.logDao().deleteAllBiomarkerLogs()
            database.logDao().deleteAllProgressPhotos()
            database.logDao().deleteAllCalculatorPresets()
            database.inventoryDao().deleteAllInventoryItems()
            database.protocolDao().deleteAllCompounds()
            database.protocolDao().deleteAllProtocols()
            database.healthMetricDao().deleteAllHealthMetrics()

            database.protocolDao().insertProtocols(data.protocols.map { it.toEntity() })
            database.protocolDao().insertCompounds(data.protocolCompounds.map { it.toEntity() })
            database.logDao().insertDoseLogs(data.doseLogs.map { it.toEntity() })
            database.logDao().insertSiteLogs(data.injectionSiteLogs.map { it.toEntity() })
            database.inventoryDao().insertInventoryItems(data.inventoryItems.map { it.toEntity() })
            database.logDao().insertSideEffectLogs(data.sideEffectLogs.map { it.toEntity() })
            database.logDao().insertBiomarkerLogs(data.biomarkerLogs.map { it.toEntity() })
            database.logDao().insertProgressPhotos(data.progressPhotos.map { it.toEntity() })
            database.logDao().insertCalculatorPresets(data.calculatorPresets.map { it.toEntity() })
            database.healthMetricDao().insertHealthMetrics(data.healthMetrics.map { it.toEntity() })
        }
    }

    private suspend fun validate(data: BackupData) {
        require(data.version in 1..BackupData.CURRENT_VERSION) { "Unsupported backup version" }
        require(data.protocols.size <= MAX_RECORDS_PER_TABLE) { "Backup contains too many protocols" }
        require(data.doseLogs.size <= MAX_RECORDS_PER_TABLE) { "Backup contains too many dose logs" }

        val protocolIds = data.protocols.map { it.id }.requireUnique("protocol")
        val compoundIds = data.protocolCompounds.map { it.id }.requireUnique("protocol compound")
        val doseIds = data.doseLogs.map { it.id }.requireUnique("dose log")
        val peptideIds = database.peptideDao().getAllPeptides().first().map { it.id }.toSet()

        data.protocols.forEach { protocol ->
            require(protocol.id.isNotBlank() && protocol.name.isNotBlank()) { "Invalid protocol record" }
            requireEnum<ProtocolStatus>(protocol.status, "protocol status")
        }
        data.protocolCompounds.forEach { compound ->
            require(compound.protocolId in protocolIds) { "Compound references an unknown protocol" }
            require(compound.peptideId in peptideIds) { "Compound references an unknown peptide" }
            require(compound.doseAmount.isFinite() && compound.doseAmount > 0.0) { "Invalid compound dose" }
            requireEnum<DoseUnit>(compound.doseUnit, "dose unit")
            requireEnum<FrequencyType>(compound.frequencyType, "frequency")
            requireEnum<AdminRoute>(compound.adminRoute, "administration route")
        }
        data.doseLogs.forEach { log ->
            require(log.protocolCompoundId in compoundIds) { "Dose references an unknown compound" }
            require(log.doseAmount.isFinite() && log.doseAmount > 0.0) { "Invalid dose log amount" }
            requireEnum<DoseUnit>(log.doseUnit, "dose unit")
            requireEnum<DoseStatus>(log.status, "dose status")
        }
        data.injectionSiteLogs.forEach { log ->
            require(log.doseLogId == null || log.doseLogId in doseIds) { "Injection log references an unknown dose" }
            requireEnum<HealingStatus>(log.healingStatus, "healing status")
        }
        data.inventoryItems.forEach { item ->
            require(item.peptideId in peptideIds) { "Inventory references an unknown peptide" }
            require(item.quantity >= 0) { "Inventory quantity cannot be negative" }
            require(item.vialStrengthMg.isFinite() && item.vialStrengthMg >= 0.0) { "Invalid vial strength" }
            requireEnum<InventoryStatus>(item.status, "inventory status")
        }
        data.sideEffectLogs.forEach { log ->
            require(log.protocolId == null || log.protocolId in protocolIds) { "Side effect references an unknown protocol" }
            require(log.severity in 0..10) { "Side effect severity must be between 0 and 10" }
        }
        data.biomarkerLogs.forEach { log ->
            require(log.protocolId == null || log.protocolId in protocolIds) { "Biomarker references an unknown protocol" }
            require(log.value.isFinite()) { "Biomarker value must be finite" }
        }
        data.progressPhotos.forEach { photo ->
            require(photo.photoUri.isNotBlank()) { "Progress photo URI cannot be empty" }
            require(photo.protocolId == null || photo.protocolId in protocolIds) { "Photo references an unknown protocol" }
        }
        data.calculatorPresets.forEach { preset ->
            require(preset.peptideId == null || preset.peptideId in peptideIds) { "Preset references an unknown peptide" }
            require(preset.vialStrengthMg.isFinite() && preset.bacWaterMl.isFinite() && preset.desiredDoseMg.isFinite()) {
                "Calculator values must be finite"
            }
        }
        data.healthMetrics.forEach { metric ->
            requireEnum<HealthMetricType>(metric.metricType, "health metric type")
            require(metric.value.isFinite()) { "Health metric value must be finite" }
            require(metric.secondaryValue?.isFinite() != false) { "Health metric value must be finite" }
            require(metric.protocolId == null || metric.protocolId in protocolIds) { "Health metric references an unknown protocol" }
        }
    }

    private fun List<String>.requireUnique(label: String): Set<String> {
        require(all { it.isNotBlank() } && size == toSet().size) { "Duplicate or empty $label IDs" }
        return toSet()
    }

    private inline fun <reified T : Enum<T>> requireEnum(value: String, label: String) {
        require(runCatching { enumValueOf<T>(value) }.isSuccess) { "Invalid $label" }
    }

    private fun com.appvexis.peptidetracker.core.backup.model.ProtocolBackup.toEntity() =
        ProtocolEntity(id, name, goal, status, startDate, endDate, notes, createdAt, updatedAt)

    private fun com.appvexis.peptidetracker.core.backup.model.ProtocolCompoundBackup.toEntity() =
        ProtocolCompoundEntity(
            id = id,
            protocolId = protocolId,
            peptideId = peptideId,
            doseAmount = doseAmount,
            doseUnit = enumValueOf(doseUnit),
            frequencyType = enumValueOf(frequencyType),
            frequencyDays = frequencyDays,
            timeOfDay = timeOfDay,
            adminRoute = enumValueOf(adminRoute),
            titrationEnabled = titrationEnabled,
            titrationSchedule = titrationSchedule,
            startDate = startDate,
            endDate = endDate,
            isActive = isActive,
            notes = notes
        )

    private fun com.appvexis.peptidetracker.core.backup.model.DoseLogBackup.toEntity() =
        DoseLogEntity(id, protocolCompoundId, scheduledTime, actualTime, doseAmount, enumValueOf(doseUnit), enumValueOf(status), injectionSite, injectionSide, notes, createdAt)

    private fun com.appvexis.peptidetracker.core.backup.model.InjectionSiteLogBackup.toEntity() =
        InjectionSiteLogEntity(id, doseLogId, bodyArea, painLevel, reactionNotes, enumValueOf(healingStatus), timestamp)

    private fun com.appvexis.peptidetracker.core.backup.model.InventoryItemBackup.toEntity() =
        InventoryItemEntity(id, peptideId, vendor, batchNumber, purchaseDate, vialStrengthMg, quantity, storageLocation, isReconstituted, reconstitutionDate, bacWaterMl, concentrationMgMl, expirationDate, remainingVolumeMl, notes, enumValueOf(status))

    private fun com.appvexis.peptidetracker.core.backup.model.SideEffectLogBackup.toEntity() =
        SideEffectLogEntity(id, protocolId, date, type, category, severity, notes, createdAt)

    private fun com.appvexis.peptidetracker.core.backup.model.BiomarkerLogBackup.toEntity() =
        BiomarkerLogEntity(id, biomarkerName, value, unit, date, protocolId, labName, notes)

    private fun com.appvexis.peptidetracker.core.backup.model.ProgressPhotoBackup.toEntity() =
        ProgressPhotoEntity(id, photoUri, date, protocolId, category, notes)

    private fun com.appvexis.peptidetracker.core.backup.model.CalculatorPresetBackup.toEntity() =
        CalculatorPresetEntity(id, name, peptideId, vialStrengthMg, bacWaterMl, desiredDoseMg, createdAt)

    private fun com.appvexis.peptidetracker.core.backup.model.HealthMetricBackup.toEntity() =
        HealthMetricEntity(id, metricType, value, secondaryValue, timestamp, source, protocolId, syncedAt)

    private companion object {
        const val MAX_RECORDS_PER_TABLE = 100_000
    }
}
