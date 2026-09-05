package com.appvexis.peptidetracker.core.backup.model

import kotlinx.serialization.Serializable
import com.appvexis.peptidetracker.core.model.Peptide
import com.appvexis.peptidetracker.core.model.TitrationStep

/**
 * Complete backup payload containing all user data.
 * Serialized to JSON for the full local export.
 */
@Serializable
data class BackupData(
    val version: Int = CURRENT_VERSION,
    val createdAt: Long = System.currentTimeMillis(),
    val appVersion: String = "",
    /** User-created catalog entries; built-in encyclopedia data is seeded by the app. */
    val customPeptides: List<Peptide> = emptyList(),
    /** Names keep exports readable without duplicating the built-in catalog. */
    val peptideNames: Map<String, String> = emptyMap(),
    val protocols: List<ProtocolBackup> = emptyList(),
    val protocolCompounds: List<ProtocolCompoundBackup> = emptyList(),
    val doseLogs: List<DoseLogBackup> = emptyList(),
    val injectionSiteLogs: List<InjectionSiteLogBackup> = emptyList(),
    val inventoryItems: List<InventoryItemBackup> = emptyList(),
    val sideEffectLogs: List<SideEffectLogBackup> = emptyList(),
    val biomarkerLogs: List<BiomarkerLogBackup> = emptyList(),
    val progressPhotos: List<ProgressPhotoBackup> = emptyList(),
    val calculatorPresets: List<CalculatorPresetBackup> = emptyList(),
    val healthMetrics: List<HealthMetricBackup> = emptyList()
) {
    companion object {
        const val CURRENT_VERSION = 1
    }
}

@Serializable
data class ProtocolBackup(
    val id: String,
    val name: String,
    val goal: String? = null,
    val status: String,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val notes: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class ProtocolCompoundBackup(
    val id: String,
    val protocolId: String,
    val peptideId: String,
    val doseAmount: Double,
    val doseUnit: String,
    val frequencyType: String,
    val frequencyDays: List<Int>? = null,
    val timeOfDay: String,
    val adminRoute: String,
    val titrationEnabled: Boolean,
    val titrationSchedule: List<TitrationStep>? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val isActive: Boolean,
    val notes: String? = null
)

@Serializable
data class DoseLogBackup(
    val id: String,
    val protocolCompoundId: String,
    val scheduledTime: Long,
    val actualTime: Long? = null,
    val doseAmount: Double,
    val doseUnit: String,
    val status: String,
    val injectionSite: String? = null,
    val injectionSide: String? = null,
    val notes: String? = null,
    val createdAt: Long
)

@Serializable
data class InjectionSiteLogBackup(
    val id: String,
    val doseLogId: String? = null,
    val bodyArea: String,
    val painLevel: Int? = null,
    val reactionNotes: String? = null,
    val healingStatus: String,
    val timestamp: Long
)

@Serializable
data class InventoryItemBackup(
    val id: String,
    val peptideId: String,
    val vendor: String? = null,
    val batchNumber: String? = null,
    val purchaseDate: Long? = null,
    val vialStrengthMg: Double,
    val quantity: Int,
    val storageLocation: String? = null,
    val isReconstituted: Boolean,
    val reconstitutionDate: Long? = null,
    val bacWaterMl: Double? = null,
    val concentrationMgMl: Double? = null,
    val expirationDate: Long? = null,
    val remainingVolumeMl: Double? = null,
    val notes: String? = null,
    val status: String
)

@Serializable
data class SideEffectLogBackup(
    val id: String,
    val protocolId: String? = null,
    val date: Long,
    val type: String,
    val category: String? = null,
    val severity: Int,
    val notes: String? = null,
    val createdAt: Long
)

@Serializable
data class BiomarkerLogBackup(
    val id: String,
    val biomarkerName: String,
    val value: Double,
    val unit: String,
    val date: Long,
    val protocolId: String? = null,
    val labName: String? = null,
    val notes: String? = null
)

@Serializable
data class ProgressPhotoBackup(
    val id: String,
    val photoUri: String,
    val date: Long,
    val protocolId: String? = null,
    val category: String? = null,
    val notes: String? = null
)

@Serializable
data class CalculatorPresetBackup(
    val id: String,
    val name: String,
    val peptideId: String? = null,
    val vialStrengthMg: Double,
    val bacWaterMl: Double,
    val desiredDoseMg: Double,
    val createdAt: Long
)

@Serializable
data class HealthMetricBackup(
    val id: String,
    val metricType: String,
    val value: Double,
    val secondaryValue: Double? = null,
    val timestamp: Long,
    val source: String,
    val protocolId: String? = null,
    val syncedAt: Long
)

/**
 * Status of a backup/restore operation.
 */
sealed class BackupStatus {
    data object Idle : BackupStatus()
    data class InProgress(val progress: Float, val message: String) : BackupStatus()
    data class Success(val timestamp: Long, val sizeBytes: Long) : BackupStatus()
    data class Restored(val timestamp: Long, val recordCount: Int) : BackupStatus()
    data class Error(val message: String) : BackupStatus()
}
