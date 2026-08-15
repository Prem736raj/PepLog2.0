package com.appvexis.peptidetracker.core.backup.export

import com.appvexis.peptidetracker.core.backup.BackupConfig
import com.appvexis.peptidetracker.core.backup.model.BackupData
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Exports backup data to CSV files for user-facing export.
 * Generates individual CSV files for dose logs, protocols, biomarkers,
 * inventory items, injection site history, and health metrics.
 */
@Singleton
class CsvExporter @Inject constructor() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    /**
     * Exports all data categories to CSV files in the given directory.
     * Returns a list of generated [File] objects.
     */
    fun exportAll(data: BackupData, outputDir: File): List<File> {
        Timber.d("CsvExporter: exporting all data to ${outputDir.absolutePath}")
        outputDir.mkdirs()

        val files = mutableListOf<File>()

        files += exportDoseLogs(data, outputDir)
        files += exportProtocols(data, outputDir)
        files += exportBiomarkers(data, outputDir)
        files += exportInventory(data, outputDir)
        files += exportInjectionSites(data, outputDir)
        files += exportHealthMetrics(data, outputDir)

        Timber.d("CsvExporter: exported ${files.size} CSV files")
        return files
    }

    private fun exportDoseLogs(data: BackupData, outputDir: File): File {
        val file = File(outputDir, BackupConfig.EXPORT_DOSE_LOGS)
        file.bufferedWriter().use { writer ->
            writer.appendLine("ID,Protocol Compound ID,Scheduled Time,Actual Time,Dose Amount,Dose Unit,Status,Injection Site,Injection Side,Notes,Created At")
            data.doseLogs.forEach { log ->
                writer.appendLine(
                    listOf(
                        log.id,
                        log.protocolCompoundId,
                        formatTimestamp(log.scheduledTime),
                        log.actualTime?.let { formatTimestamp(it) } ?: "",
                        log.doseAmount,
                        log.doseUnit.csvEscape(),
                        log.status.csvEscape(),
                        log.injectionSite?.csvEscape() ?: "",
                        log.injectionSide?.csvEscape() ?: "",
                        log.notes?.csvEscape() ?: "",
                        formatTimestamp(log.createdAt)
                    ).joinToString(",")
                )
            }
        }
        return file
    }

    private fun exportProtocols(data: BackupData, outputDir: File): File {
        val file = File(outputDir, BackupConfig.EXPORT_PROTOCOLS)
        file.bufferedWriter().use { writer ->
            writer.appendLine("ID,Name,Goal,Status,Start Date,End Date,Notes,Created At,Updated At")
            data.protocols.forEach { p ->
                writer.appendLine(
                    listOf(
                        p.id,
                        p.name.csvEscape(),
                        p.goal?.csvEscape() ?: "",
                        p.status.csvEscape(),
                        p.startDate?.let { formatTimestamp(it) } ?: "",
                        p.endDate?.let { formatTimestamp(it) } ?: "",
                        p.notes?.csvEscape() ?: "",
                        formatTimestamp(p.createdAt),
                        formatTimestamp(p.updatedAt)
                    ).joinToString(",")
                )
            }
            // Also export compounds
            writer.appendLine()
            writer.appendLine("--- Protocol Compounds ---")
            writer.appendLine("ID,Protocol ID,Peptide ID,Dose Amount,Dose Unit,Frequency Type,Time of Day,Admin Route,Titration Enabled,Is Active,Notes")
            data.protocolCompounds.forEach { c ->
                writer.appendLine(
                    listOf(
                        c.id,
                        c.protocolId,
                        c.peptideId,
                        c.doseAmount,
                        c.doseUnit.csvEscape(),
                        c.frequencyType.csvEscape(),
                        c.timeOfDay.csvEscape(),
                        c.adminRoute.csvEscape(),
                        c.titrationEnabled,
                        c.isActive,
                        c.notes?.csvEscape() ?: ""
                    ).joinToString(",")
                )
            }
        }
        return file
    }

    private fun exportBiomarkers(data: BackupData, outputDir: File): File {
        val file = File(outputDir, BackupConfig.EXPORT_BIOMARKERS)
        file.bufferedWriter().use { writer ->
            writer.appendLine("ID,Biomarker Name,Value,Unit,Date,Protocol ID,Lab Name,Notes")
            data.biomarkerLogs.forEach { b ->
                writer.appendLine(
                    listOf(
                        b.id,
                        b.biomarkerName.csvEscape(),
                        b.value,
                        b.unit.csvEscape(),
                        formatTimestamp(b.date),
                        b.protocolId ?: "",
                        b.labName?.csvEscape() ?: "",
                        b.notes?.csvEscape() ?: ""
                    ).joinToString(",")
                )
            }
        }
        return file
    }

    private fun exportInventory(data: BackupData, outputDir: File): File {
        val file = File(outputDir, BackupConfig.EXPORT_INVENTORY)
        file.bufferedWriter().use { writer ->
            writer.appendLine("ID,Peptide ID,Vendor,Batch Number,Vial Strength (mg),Quantity,Storage Location,Reconstituted,Reconstitution Date,BAC Water (mL),Concentration (mg/mL),Expiration Date,Remaining Volume (mL),Status,Notes")
            data.inventoryItems.forEach { item ->
                writer.appendLine(
                    listOf(
                        item.id,
                        item.peptideId,
                        item.vendor?.csvEscape() ?: "",
                        item.batchNumber?.csvEscape() ?: "",
                        item.vialStrengthMg,
                        item.quantity,
                        item.storageLocation?.csvEscape() ?: "",
                        item.isReconstituted,
                        item.reconstitutionDate?.let { formatTimestamp(it) } ?: "",
                        item.bacWaterMl ?: "",
                        item.concentrationMgMl ?: "",
                        item.expirationDate?.let { formatTimestamp(it) } ?: "",
                        item.remainingVolumeMl ?: "",
                        item.status.csvEscape(),
                        item.notes?.csvEscape() ?: ""
                    ).joinToString(",")
                )
            }
        }
        return file
    }

    private fun exportInjectionSites(data: BackupData, outputDir: File): File {
        val file = File(outputDir, BackupConfig.EXPORT_INJECTION_SITES)
        file.bufferedWriter().use { writer ->
            writer.appendLine("ID,Dose Log ID,Body Area,Pain Level,Reaction Notes,Healing Status,Timestamp")
            data.injectionSiteLogs.forEach { log ->
                writer.appendLine(
                    listOf(
                        log.id,
                        log.doseLogId ?: "",
                        log.bodyArea.csvEscape(),
                        log.painLevel ?: "",
                        log.reactionNotes?.csvEscape() ?: "",
                        log.healingStatus.csvEscape(),
                        formatTimestamp(log.timestamp)
                    ).joinToString(",")
                )
            }
        }
        return file
    }

    private fun exportHealthMetrics(data: BackupData, outputDir: File): File {
        val file = File(outputDir, BackupConfig.EXPORT_HEALTH_METRICS)
        file.bufferedWriter().use { writer ->
            writer.appendLine("ID,Metric Type,Value,Secondary Value,Source,Timestamp,Synced At")
            data.healthMetrics.forEach { m ->
                writer.appendLine(
                    listOf(
                        m.id,
                        m.metricType.csvEscape(),
                        m.value,
                        m.secondaryValue ?: "",
                        m.source.csvEscape(),
                        formatTimestamp(m.timestamp),
                        formatTimestamp(m.syncedAt)
                    ).joinToString(",")
                )
            }
        }
        return file
    }

    private fun formatTimestamp(millis: Long): String {
        return dateFormat.format(Date(millis))
    }

    private fun String.csvEscape(): String {
        return if (contains(",") || contains("\"") || contains("\n")) {
            "\"${replace("\"", "\"\"")}\""
        } else this
    }
}
