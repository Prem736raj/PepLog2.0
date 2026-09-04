package com.appvexis.peptidetracker.core.backup.export

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Exports all Room database data to a serializable [BackupData] object.
 * This is the canonical way to convert the full database into a portable format
 * for the local JSON export.
 */
@Singleton
class DatabaseExporter @Inject constructor(
    private val database: PepLogDatabase
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Reads all tables and produces a complete [BackupData] snapshot.
     */
    suspend fun exportToBackupData(): BackupData = withContext(Dispatchers.IO) {
        Timber.d("Starting database export...")

        val protocols = database.protocolDao().getAllProtocolsSync().map { entity ->
            ProtocolBackup(
                id = entity.id,
                name = entity.name,
                goal = entity.goal,
                status = entity.status,
                startDate = entity.startDate,
                endDate = entity.endDate,
                notes = entity.notes,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt
            )
        }

        val compounds = database.protocolDao().getAllCompoundsSync().map { entity ->
            ProtocolCompoundBackup(
                id = entity.id,
                protocolId = entity.protocolId,
                peptideId = entity.peptideId,
                doseAmount = entity.doseAmount,
                doseUnit = entity.doseUnit.name,
                frequencyType = entity.frequencyType.name,
                frequencyDays = entity.frequencyDays,
                timeOfDay = entity.timeOfDay,
                adminRoute = entity.adminRoute.name,
                titrationEnabled = entity.titrationEnabled,
                titrationSchedule = entity.titrationSchedule,
                startDate = entity.startDate,
                endDate = entity.endDate,
                isActive = entity.isActive,
                notes = entity.notes
            )
        }

        val doseLogs = database.logDao().getAllDoseLogsSync().map { entity ->
            DoseLogBackup(
                id = entity.id,
                protocolCompoundId = entity.protocolCompoundId,
                scheduledTime = entity.scheduledTime,
                actualTime = entity.actualTime,
                doseAmount = entity.doseAmount,
                doseUnit = entity.doseUnit.name,
                status = entity.status.name,
                injectionSite = entity.injectionSite,
                injectionSide = entity.injectionSide,
                notes = entity.notes,
                createdAt = entity.createdAt
            )
        }

        val injectionLogs = database.logDao().getAllInjectionSiteLogsSync().map { entity ->
            InjectionSiteLogBackup(
                id = entity.id,
                doseLogId = entity.doseLogId,
                bodyArea = entity.bodyArea,
                painLevel = entity.painLevel,
                reactionNotes = entity.reactionNotes,
                healingStatus = entity.healingStatus.name,
                timestamp = entity.timestamp
            )
        }

        val inventoryItems = database.inventoryDao().getAllItemsSync().map { entity ->
            InventoryItemBackup(
                id = entity.id,
                peptideId = entity.peptideId,
                vendor = entity.vendor,
                batchNumber = entity.batchNumber,
                purchaseDate = entity.purchaseDate,
                vialStrengthMg = entity.vialStrengthMg,
                quantity = entity.quantity,
                storageLocation = entity.storageLocation,
                isReconstituted = entity.isReconstituted,
                reconstitutionDate = entity.reconstitutionDate,
                bacWaterMl = entity.bacWaterMl,
                concentrationMgMl = entity.concentrationMgMl,
                expirationDate = entity.expirationDate,
                remainingVolumeMl = entity.remainingVolumeMl,
                notes = entity.notes,
                status = entity.status.name
            )
        }

        val sideEffects = database.logDao().getAllSideEffectLogsSync().map { entity ->
            SideEffectLogBackup(
                id = entity.id,
                protocolId = entity.protocolId,
                date = entity.date,
                type = entity.type,
                category = entity.category,
                severity = entity.severity,
                notes = entity.notes,
                createdAt = entity.createdAt
            )
        }

        val biomarkers = database.logDao().getAllBiomarkerLogsSync().map { entity ->
            BiomarkerLogBackup(
                id = entity.id,
                biomarkerName = entity.biomarkerName,
                value = entity.value,
                unit = entity.unit,
                date = entity.date,
                protocolId = entity.protocolId,
                labName = entity.labName,
                notes = entity.notes
            )
        }

        val photos = database.logDao().getAllProgressPhotosSync().map { entity ->
            ProgressPhotoBackup(
                id = entity.id,
                photoUri = entity.photoUri,
                date = entity.date,
                protocolId = entity.protocolId,
                category = entity.category,
                notes = entity.notes
            )
        }

        val presets = database.logDao().getAllCalculatorPresetsSync().map { entity ->
            CalculatorPresetBackup(
                id = entity.id,
                name = entity.name,
                peptideId = entity.peptideId,
                vialStrengthMg = entity.vialStrengthMg,
                bacWaterMl = entity.bacWaterMl,
                desiredDoseMg = entity.desiredDoseMg,
                createdAt = entity.createdAt
            )
        }

        val healthMetrics = database.healthMetricDao().getAllMetricsSync().map { entity ->
            HealthMetricBackup(
                id = entity.id,
                metricType = entity.metricType,
                value = entity.value,
                secondaryValue = entity.secondaryValue,
                timestamp = entity.timestamp,
                source = entity.source,
                protocolId = entity.protocolId,
                syncedAt = entity.syncedAt
            )
        }

        val backupData = BackupData(
            protocols = protocols,
            protocolCompounds = compounds,
            doseLogs = doseLogs,
            injectionSiteLogs = injectionLogs,
            inventoryItems = inventoryItems,
            sideEffectLogs = sideEffects,
            biomarkerLogs = biomarkers,
            progressPhotos = photos,
            calculatorPresets = presets,
            healthMetrics = healthMetrics
        )

        Timber.d("Database export complete: ${protocols.size} protocols, ${doseLogs.size} dose logs, ${biomarkers.size} biomarkers")
        backupData
    }

    /**
     * Serializes [BackupData] to a JSON string.
     */
    fun toJson(data: BackupData): String {
        return json.encodeToString(data)
    }

    /**
     * Deserializes a JSON string to [BackupData].
     */
    fun fromJson(jsonString: String): BackupData {
        require(jsonString.length <= MAX_BACKUP_JSON_CHARS) { "Backup file is too large" }
        return json.decodeFromString(jsonString)
    }

    private companion object {
        const val MAX_BACKUP_JSON_CHARS = 10_000_000
    }
}
