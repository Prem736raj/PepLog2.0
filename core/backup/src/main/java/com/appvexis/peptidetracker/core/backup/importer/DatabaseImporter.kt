package com.appvexis.peptidetracker.core.backup.importer

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.appvexis.peptidetracker.core.backup.model.BackupData
import com.appvexis.peptidetracker.core.backup.model.BiomarkerLogBackup
import com.appvexis.peptidetracker.core.backup.model.CalculatorPresetBackup
import com.appvexis.peptidetracker.core.backup.model.DoseLogBackup
import com.appvexis.peptidetracker.core.backup.model.HealthMetricBackup
import com.appvexis.peptidetracker.core.backup.model.InjectionSiteLogBackup
import com.appvexis.peptidetracker.core.backup.model.InventoryItemBackup
import com.appvexis.peptidetracker.core.backup.model.ProgressPhotoBackup
import com.appvexis.peptidetracker.core.backup.model.ProtocolBackup
import com.appvexis.peptidetracker.core.backup.model.ProtocolCompoundBackup
import com.appvexis.peptidetracker.core.backup.model.SideEffectLogBackup
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
import com.appvexis.peptidetracker.core.database.entity.toEntity
import com.appvexis.peptidetracker.core.model.AdminRoute
import com.appvexis.peptidetracker.core.model.DoseStatus
import com.appvexis.peptidetracker.core.model.DoseUnit
import com.appvexis.peptidetracker.core.model.FrequencyType
import com.appvexis.peptidetracker.core.model.HealingStatus
import com.appvexis.peptidetracker.core.model.InventoryStatus
import com.appvexis.peptidetracker.core.model.ProtocolStatus
import com.appvexis.peptidetracker.core.model.repository.AnalyticsRepository
import com.appvexis.peptidetracker.core.model.repository.PeptideRepository
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Validates and replaces the user-owned portion of the Room database from a
 * PepLog backup. The operation is atomic: a malformed or incompatible file is
 * rejected before existing records are cleared.
 */
@Singleton
class DatabaseImporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: PepLogDatabase,
    private val peptideRepository: PeptideRepository,
    private val analyticsRepository: Lazy<AnalyticsRepository>
) {

    suspend fun importBackup(
        data: BackupData,
        photoUriOverrides: Map<String, String> = emptyMap()
    ): Int = withContext(Dispatchers.IO) {
        // The catalog is seeded lazily by PeptideRepository. Restore can be
        // opened before any catalog screen, so explicitly await that seed
        // before validating foreign-key references.
        peptideRepository.getAllPeptides().first()
        validate(data, photoUriOverrides)

        val restoredCount = database.withTransaction {
            clearUserData()

            database.peptideDao().insertAll(data.customPeptides.map { it.toEntity() })
            database.protocolDao().insertProtocols(data.protocols.map { it.toEntity() })
            database.protocolDao().insertCompounds(data.protocolCompounds.map { it.toEntity() })
            database.inventoryDao().insertInventoryItems(data.inventoryItems.map { it.toEntity() })
            database.logDao().insertDoseLogs(data.doseLogs.map { it.toEntity() })
            database.logDao().insertSiteLogs(data.injectionSiteLogs.map { it.toEntity() })
            database.logDao().insertSideEffectLogs(data.sideEffectLogs.map { it.toEntity() })
            database.logDao().insertBiomarkerLogs(data.biomarkerLogs.map { it.toEntity() })
            database.logDao().insertProgressPhotos(
                data.progressPhotos.map { it.toEntity(photoUriOverrides[it.id] ?: it.photoUri) }
            )
            database.logDao().insertCalculatorPresets(data.calculatorPresets.map { it.toEntity() })
            database.healthMetricDao().insertHealthMetrics(data.healthMetrics.map { it.toEntity() })

            data.customPeptides.size +
                data.protocols.size +
                data.protocolCompounds.size +
                data.doseLogs.size +
                data.injectionSiteLogs.size +
                data.inventoryItems.size +
                data.sideEffectLogs.size +
                data.biomarkerLogs.size +
                data.progressPhotos.size +
                data.calculatorPresets.size +
                data.healthMetrics.size
        }

        // Analytics is derived data. Rebuild it after the transaction so an
        // interrupted or invalid restore can never leave a half-written cache.
        data.protocols.forEach { protocol ->
            runCatching {
                analyticsRepository.get().recomputeAnalyticsForProtocol(protocol.id)
            }.onFailure { error ->
                Timber.w(error, "Could not rebuild analytics for restored protocol")
            }
        }
        restoredCount
    }

    private suspend fun clearUserData() {
        database.analyticsDao().deleteAllDailySummaries()
        database.analyticsDao().deleteAllProtocolSummaries()
        database.logDao().deleteAllSiteLogs()
        database.logDao().deleteAllDoseLogs()
        database.logDao().deleteAllSideEffectLogs()
        database.logDao().deleteAllBiomarkerLogs()
        database.logDao().deleteAllProgressPhotos()
        database.logDao().deleteAllCalculatorPresets()
        database.healthMetricDao().deleteAllHealthMetrics()
        database.inventoryDao().deleteAllInventoryItems()
        database.protocolDao().deleteAllCompounds()
        database.protocolDao().deleteAllProtocols()
        // Custom peptides are referenced by protocol compounds, so remove
        // them only after the child rows have been cleared.
        database.peptideDao().deleteCustomPeptides()
    }

    private suspend fun validate(data: BackupData, photoUriOverrides: Map<String, String>) {
        require(data.version in 1..BackupData.CURRENT_VERSION) {
            "This backup version is not supported by this app"
        }
        require(data.createdAt >= 0L) { "Backup timestamp is invalid" }
        requireAllWithinLimit(data)

        val protocolIds = data.protocols.map { it.id }.also { ensureUnique("protocol", it) }.toSet()
        val compoundIds = data.protocolCompounds.map { it.id }.also { ensureUnique("compound", it) }.toSet()
        val doseIds = data.doseLogs.map { it.id }.also { ensureUnique("dose", it) }.toSet()
        val photoIds = data.progressPhotos.map { it.id }.also { ensureUnique("photo", it) }.toSet()
        val customPeptideIds = data.customPeptides.map { it.id }
            .also { ensureUnique("custom peptide", it) }
            .toSet()

        data.customPeptides.forEach { peptide ->
            requireId(peptide.id, "custom peptide")
            require(peptide.id.startsWith(CUSTOM_ID_PREFIX)) {
                "Backup contains a peptide outside the custom namespace"
            }
            require(peptide.name.isNotBlank() && peptide.name.length <= 80) {
                "Custom peptide name is invalid"
            }
            require(peptide.category.isNotBlank() && peptide.category.length <= 40) {
                "Custom peptide category is invalid"
            }
            val halfLifeHours = peptide.halfLifeHours
            require(halfLifeHours == null ||
                (halfLifeHours.isFinite() && halfLifeHours > 0.0)
            ) { "Custom peptide half-life is invalid" }
        }

        require(photoUriOverrides.keys.all { it in photoIds }) { "Backup contains an unknown photo reference" }
        require(photoUriOverrides.values.all { it.startsWith("file:") }) {
            "Restored photo paths must remain private app files"
        }

        data.protocols.forEach { protocol ->
            requireId(protocol.id, "protocol")
            require(protocol.name.isNotBlank() && protocol.name.length <= 100) { "Protocol name is invalid" }
            require(protocol.createdAt >= 0L && protocol.updatedAt >= 0L) { "Protocol timestamp is invalid" }
        }

        val peptideIds = database.peptideDao().getAllPeptidesSync().map { it.id }.toSet() + customPeptideIds
        data.protocolCompounds.forEach { compound ->
            requireId(compound.id, "compound")
            require(compound.protocolId in protocolIds) { "Compound references a missing protocol" }
            require(compound.peptideId in peptideIds) { "Compound references an unknown catalog peptide" }
            require(compound.doseAmount.isFinite() && compound.doseAmount > 0.0) { "Compound dose is invalid" }
            require(compound.timeOfDay.isNotBlank()) { "Compound dose time is invalid" }
        }

        data.doseLogs.forEach { dose ->
            requireId(dose.id, "dose")
            require(dose.protocolCompoundId in compoundIds) { "Dose references a missing compound" }
            require(dose.scheduledTime >= 0L && dose.createdAt >= 0L) { "Dose timestamp is invalid" }
            require(dose.actualTime == null || dose.actualTime >= 0L) { "Dose actual time is invalid" }
            require(dose.doseAmount.isFinite() && dose.doseAmount > 0.0) { "Dose amount is invalid" }
            if (dose.status.toEnumOrDefault(DoseStatus.SKIPPED) == DoseStatus.TAKEN) {
                require(dose.actualTime != null) { "A taken dose must include its actual time" }
            }
        }

        data.injectionSiteLogs.forEach { log ->
            requireId(log.id, "injection-site log")
            require(log.doseLogId == null || log.doseLogId in doseIds) { "Injection-site log references a missing dose" }
            require(log.timestamp >= 0L) { "Injection-site timestamp is invalid" }
            require(log.painLevel == null || log.painLevel in 1..5) { "Injection-site pain level is invalid" }
        }

        data.inventoryItems.forEach { item ->
            requireId(item.id, "inventory item")
            require(item.peptideId in peptideIds) { "Inventory references an unknown catalog peptide" }
            require(item.vialStrengthMg.isFinite() && item.vialStrengthMg > 0.0) { "Vial strength is invalid" }
            require(item.quantity > 0) { "Inventory quantity is invalid" }
            require(item.bacWaterMl == null || (item.bacWaterMl.isFinite() && item.bacWaterMl > 0.0)) { "Bacteriostatic water volume is invalid" }
            require(item.concentrationMgMl == null || (item.concentrationMgMl.isFinite() && item.concentrationMgMl > 0.0)) { "Inventory concentration is invalid" }
            require(item.remainingVolumeMl == null || (item.remainingVolumeMl.isFinite() && item.remainingVolumeMl >= 0.0)) { "Remaining volume is invalid" }
        }

        data.sideEffectLogs.forEach { log ->
            requireId(log.id, "wellness log")
            require(log.protocolId == null || log.protocolId in protocolIds) { "Wellness log references a missing protocol" }
            require(log.date >= 0L && log.createdAt >= 0L) { "Wellness timestamp is invalid" }
            require(log.severity in 1..10) { "Wellness severity is invalid" }
        }

        data.biomarkerLogs.forEach { log ->
            requireId(log.id, "biomarker log")
            require(log.protocolId == null || log.protocolId in protocolIds) { "Biomarker references a missing protocol" }
            require(log.biomarkerName.isNotBlank() && log.unit.isNotBlank()) { "Biomarker identity is invalid" }
            require(log.value.isFinite() && log.date >= 0L) { "Biomarker value is invalid" }
        }

        data.progressPhotos.forEach { photo ->
            requireId(photo.id, "progress photo")
            val effectivePhotoUri = photoUriOverrides[photo.id] ?: photo.photoUri
            require(isPrivatePhotoUri(effectivePhotoUri)) {
                "Progress photos in a JSON backup are not portable; use a photo-inclusive PepLog backup"
            }
            require(photo.protocolId == null || photo.protocolId in protocolIds) { "Photo references a missing protocol" }
            require(photo.date >= 0L) { "Photo timestamp is invalid" }
        }

        data.calculatorPresets.forEach { preset ->
            requireId(preset.id, "calculator preset")
            require(preset.name.isNotBlank()) { "Calculator preset name is empty" }
            require(preset.peptideId == null || preset.peptideId in peptideIds) { "Calculator preset references an unknown peptide" }
            require(preset.vialStrengthMg.isFinite() && preset.vialStrengthMg > 0.0) { "Preset vial strength is invalid" }
            require(preset.bacWaterMl.isFinite() && preset.bacWaterMl > 0.0) { "Preset water volume is invalid" }
            require(preset.desiredDoseMg.isFinite() && preset.desiredDoseMg > 0.0) { "Preset dose is invalid" }
            require(preset.createdAt >= 0L) { "Preset timestamp is invalid" }
        }

        val metricKeys = mutableSetOf<String>()
        data.healthMetrics.forEach { metric ->
            requireId(metric.id, "health metric")
            require(metric.metricType.isNotBlank() && metric.value.isFinite()) { "Health metric value is invalid" }
            require(metric.timestamp >= 0L && metric.syncedAt >= 0L) { "Health metric timestamp is invalid" }
            require(metric.protocolId == null || metric.protocolId in protocolIds) { "Health metric references a missing protocol" }
            require(metricKeys.add("${metric.metricType}\u0000${metric.timestamp}")) {
                "Backup contains duplicate health metric timestamps"
            }
        }
    }

    private fun requireAllWithinLimit(data: BackupData) {
        val totalRecords = data.customPeptides.size + data.protocols.size + data.protocolCompounds.size + data.doseLogs.size +
            data.injectionSiteLogs.size + data.inventoryItems.size + data.sideEffectLogs.size +
            data.biomarkerLogs.size + data.progressPhotos.size + data.calculatorPresets.size +
            data.healthMetrics.size
        require(totalRecords <= MAX_RECORDS) { "Backup contains too many records" }
    }

    private fun ensureUnique(kind: String, ids: List<String>) {
        require(ids.size == ids.toSet().size) { "Backup contains duplicate $kind IDs" }
    }

    private fun requireId(id: String, kind: String) {
        require(
            id.isNotBlank() && id.length <= 200 &&
                '/' !in id && '\\' !in id && !id.contains('\u0000')
        ) { "Invalid $kind ID" }
    }

    private fun isPrivatePhotoUri(value: String): Boolean = runCatching {
        val uri = Uri.parse(value)
        require(uri.scheme == "file")
        val path = uri.path ?: error("Photo path is empty")
        val directory = File(context.filesDir, PHOTO_DIRECTORY_NAME).canonicalFile.toPath()
        val file = File(path).canonicalFile
        file.isFile && file.toPath().startsWith(directory)
    }.getOrDefault(false)

    private fun ProtocolBackup.toEntity() = ProtocolEntity(
        id = id,
        name = name,
        goal = goal,
        status = status.toEnumOrDefault(ProtocolStatus.ARCHIVED).name,
        startDate = startDate,
        endDate = endDate,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun ProtocolCompoundBackup.toEntity() = ProtocolCompoundEntity(
        id = id,
        protocolId = protocolId,
        peptideId = peptideId,
        doseAmount = doseAmount,
        doseUnit = doseUnit.toEnumOrDefault(DoseUnit.IU),
        frequencyType = frequencyType.toEnumOrDefault(FrequencyType.DAILY),
        frequencyDays = frequencyDays,
        timeOfDay = timeOfDay,
        adminRoute = adminRoute.toEnumOrDefault(AdminRoute.SUBQ),
        titrationEnabled = titrationEnabled,
        titrationSchedule = titrationSchedule,
        startDate = startDate,
        endDate = endDate,
        isActive = isActive,
        notes = notes
    )

    private fun DoseLogBackup.toEntity() = DoseLogEntity(
        id = id,
        protocolCompoundId = protocolCompoundId,
        scheduledTime = scheduledTime,
        actualTime = actualTime,
        doseAmount = doseAmount,
        doseUnit = doseUnit.toEnumOrDefault(DoseUnit.IU),
        status = status.toEnumOrDefault(DoseStatus.SKIPPED),
        injectionSite = injectionSite,
        injectionSide = injectionSide,
        notes = notes,
        createdAt = createdAt
    )

    private fun InjectionSiteLogBackup.toEntity() = InjectionSiteLogEntity(
        id = id,
        doseLogId = doseLogId,
        bodyArea = bodyArea,
        painLevel = painLevel,
        reactionNotes = reactionNotes,
        healingStatus = healingStatus.toEnumOrDefault(HealingStatus.OK),
        timestamp = timestamp
    )

    private fun InventoryItemBackup.toEntity() = InventoryItemEntity(
        id = id,
        peptideId = peptideId,
        vendor = vendor,
        batchNumber = batchNumber,
        acquiredDate = acquiredDate,
        vialStrengthMg = vialStrengthMg,
        quantity = quantity,
        storageLocation = storageLocation,
        isReconstituted = isReconstituted,
        reconstitutionDate = reconstitutionDate,
        bacWaterMl = bacWaterMl,
        concentrationMgMl = concentrationMgMl,
        expirationDate = expirationDate,
        remainingVolumeMl = remainingVolumeMl,
        notes = notes,
        status = status.toEnumOrDefault(InventoryStatus.EMPTY)
    )

    private fun SideEffectLogBackup.toEntity() = SideEffectLogEntity(
        id = id,
        protocolId = protocolId,
        date = date,
        type = type,
        category = category,
        severity = severity,
        notes = notes,
        createdAt = createdAt
    )

    private fun BiomarkerLogBackup.toEntity() = BiomarkerLogEntity(
        id = id,
        biomarkerName = biomarkerName,
        value = value,
        unit = unit,
        date = date,
        protocolId = protocolId,
        labName = labName,
        notes = notes
    )

    private fun ProgressPhotoBackup.toEntity(photoUri: String) = ProgressPhotoEntity(
        id = id,
        photoUri = photoUri,
        date = date,
        protocolId = protocolId,
        category = category,
        notes = notes
    )

    private fun CalculatorPresetBackup.toEntity() = CalculatorPresetEntity(
        id = id,
        name = name,
        peptideId = peptideId,
        vialStrengthMg = vialStrengthMg,
        bacWaterMl = bacWaterMl,
        desiredDoseMg = desiredDoseMg,
        createdAt = createdAt
    )

    private fun HealthMetricBackup.toEntity() = HealthMetricEntity(
        id = id,
        metricType = metricType,
        value = value,
        secondaryValue = secondaryValue,
        timestamp = timestamp,
        source = source,
        protocolId = protocolId,
        syncedAt = syncedAt
    )

    private inline fun <reified T : Enum<T>> String.toEnumOrDefault(default: T): T =
        enumValues<T>().firstOrNull { it.name.equals(trim(), ignoreCase = true) } ?: default

    private companion object {
        const val MAX_RECORDS = 100_000
        const val PHOTO_DIRECTORY_NAME = "progress_photos"
        const val CUSTOM_ID_PREFIX = "custom-"
    }
}
