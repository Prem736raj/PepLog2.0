package com.appvexis.peptidetracker.core.backup.export

import com.appvexis.peptidetracker.core.backup.BackupConfig
import com.appvexis.peptidetracker.core.backup.model.BackupData
import timber.log.Timber
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/** Exports user data as correctly escaped, spreadsheet-compatible CSV files. */
@Singleton
class CsvExporter @Inject constructor() {
    private val dateFormat = DateTimeFormatter
        .ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US)
        .withZone(ZoneId.systemDefault())

    fun exportAll(data: BackupData, outputDir: File): List<File> {
        Timber.d("CsvExporter: exporting all data")
        require(outputDir.exists() || outputDir.mkdirs()) { "Could not create export directory" }
        require(outputDir.isDirectory) { "Export path is not a directory" }

        return listOf(
            write(outputDir, BackupConfig.EXPORT_DOSE_LOGS,
                "ID,Protocol Compound ID,Scheduled Time,Actual Time,Dose Amount,Dose Unit,Status,Injection Site,Injection Side,Notes,Created At",
                data.doseLogs.map { log ->
                    row(log.id, log.protocolCompoundId, time(log.scheduledTime), log.actualTime?.let(::time), log.doseAmount, log.doseUnit, log.status, log.injectionSite, log.injectionSide, log.notes, time(log.createdAt))
                }),
            write(outputDir, BackupConfig.EXPORT_PROTOCOLS,
                "ID,Name,Goal,Status,Start Date,End Date,Notes,Created At,Updated At",
                data.protocols.map { protocol ->
                    row(protocol.id, protocol.name, protocol.goal, protocol.status, protocol.startDate?.let(::time), protocol.endDate?.let(::time), protocol.notes, time(protocol.createdAt), time(protocol.updatedAt))
                } + listOf("", "--- Protocol Compounds ---", "ID,Protocol ID,Peptide ID,Dose Amount,Dose Unit,Frequency Type,Time of Day,Admin Route,Titration Enabled,Is Active,Notes") + data.protocolCompounds.map { compound ->
                    row(compound.id, compound.protocolId, compound.peptideId, compound.doseAmount, compound.doseUnit, compound.frequencyType, compound.timeOfDay, compound.adminRoute, compound.titrationEnabled, compound.isActive, compound.notes)
                }),
            write(outputDir, BackupConfig.EXPORT_BIOMARKERS,
                "ID,Biomarker Name,Value,Unit,Date,Protocol ID,Lab Name,Notes",
                data.biomarkerLogs.map { log ->
                    row(log.id, log.biomarkerName, log.value, log.unit, time(log.date), log.protocolId, log.labName, log.notes)
                }),
            write(outputDir, BackupConfig.EXPORT_INVENTORY,
                "ID,Peptide ID,Vendor,Batch Number,Vial Strength (mg),Quantity,Storage Location,Reconstituted,Reconstitution Date,BAC Water (mL),Concentration (mg/mL),Expiration Date,Remaining Volume (mL),Status,Notes",
                data.inventoryItems.map { item ->
                    row(item.id, item.peptideId, item.vendor, item.batchNumber, item.vialStrengthMg, item.quantity, item.storageLocation, item.isReconstituted, item.reconstitutionDate?.let(::time), item.bacWaterMl, item.concentrationMgMl, item.expirationDate?.let(::time), item.remainingVolumeMl, item.status, item.notes)
                }),
            write(outputDir, BackupConfig.EXPORT_INJECTION_SITES,
                "ID,Dose Log ID,Body Area,Pain Level,Reaction Notes,Healing Status,Timestamp",
                data.injectionSiteLogs.map { log ->
                    row(log.id, log.doseLogId, log.bodyArea, log.painLevel, log.reactionNotes, log.healingStatus, time(log.timestamp))
                }),
            write(outputDir, BackupConfig.EXPORT_HEALTH_METRICS,
                "ID,Metric Type,Value,Secondary Value,Source,Timestamp,Synced At",
                data.healthMetrics.map { metric ->
                    row(metric.id, metric.metricType, metric.value, metric.secondaryValue, metric.source, time(metric.timestamp), time(metric.syncedAt))
                })
        )
    }

    private fun write(outputDir: File, name: String, header: String, rows: List<String>): File {
        val file = File(outputDir, name)
        file.bufferedWriter().use { writer ->
            writer.appendLine(header)
            rows.forEach(writer::appendLine)
        }
        return file
    }

    private fun row(vararg values: Any?): String = values.joinToString(",") { value ->
        value?.toString().orEmpty().csvEscape()
    }

    private fun time(millis: Long): String = dateFormat.format(Instant.ofEpochMilli(millis))

    private fun String.csvEscape(): String =
        if (contains(",") || contains('"') || contains('\n') || contains('\r')) {
            "\"${replace("\"", "\"\"")}\""
        } else this
}
