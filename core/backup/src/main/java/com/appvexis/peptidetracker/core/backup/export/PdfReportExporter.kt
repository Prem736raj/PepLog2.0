package com.appvexis.peptidetracker.core.backup.export

import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.appvexis.peptidetracker.core.backup.model.BackupData
import com.appvexis.peptidetracker.core.backup.model.DoseLogBackup
import java.io.File
import java.io.FileOutputStream
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Creates a compact, human-readable summary for a clinician. It intentionally
 * contains records and observations, not dosing recommendations or diagnoses.
 */
@Singleton
class PdfReportExporter @Inject constructor() {

    fun export(data: BackupData, outputFile: File): File {
        outputFile.parentFile?.mkdirs()
        val document = PdfDocument()
        val writer = PageWriter(document)
        try {
            writer.heading("PepLog health record")
            writer.subheading("Personal summary · generated ${formatDate(System.currentTimeMillis())}")
            writer.paragraph("For clinician review only. This document records user-entered information and imported health data. It is not a diagnosis, treatment plan, or dosing recommendation.")

            writer.section("Protocols")
            if (data.protocols.isEmpty()) {
                writer.paragraph("No protocols recorded.")
            } else {
                data.protocols.forEach { protocol ->
                    writer.item("${protocol.name} · ${protocol.status}")
                    protocol.goal?.takeIf { it.isNotBlank() }?.let { writer.detail("Goal: $it") }
                    protocol.startDate?.let { writer.detail("Started: ${formatDate(it)}") }
                    protocol.endDate?.let { writer.detail("Ended: ${formatDate(it)}") }
                    protocol.notes?.takeIf { it.isNotBlank() }?.let { writer.detail("Notes: $it") }
                }
            }

            writer.section("Compounds and schedules")
            if (data.protocolCompounds.isEmpty()) {
                writer.paragraph("No compounds recorded.")
            } else {
                data.protocolCompounds.forEach { compound ->
                    val name = data.peptideNames[compound.peptideId] ?: compound.peptideId
                    val schedule = listOfNotNull(
                        "${compound.doseAmount} ${compound.doseUnit}",
                        compound.frequencyType,
                        compound.timeOfDay,
                        compound.adminRoute,
                    ).joinToString(" · ")
                    writer.item("$name · $schedule")
                    writer.detail("Dose records: ${data.doseLogs.count { it.protocolCompoundId == compound.id }}${if (!compound.isActive) " · inactive" else ""}")
                    if (compound.titrationEnabled) {
                        writer.detail("Titration: ${compound.titrationSchedule.orEmpty().joinToString { "week ${it.week} = ${it.doseAmount}" }}")
                    }
                }
            }

            writer.section("Dose history")
            if (data.doseLogs.isEmpty()) {
                writer.paragraph("No dose records recorded.")
            } else {
                val counts = data.doseLogs.groupingBy { it.status }.eachCount()
                writer.paragraph(counts.entries.sortedBy { it.key }.joinToString(" · ") { "${it.key}: ${it.value}" })
                data.doseLogs.sortedByDescending { it.scheduledTime }.take(MAX_DOSES_IN_REPORT).forEach { dose ->
                    writer.item(formatDose(dose))
                }
                if (data.doseLogs.size > MAX_DOSES_IN_REPORT) {
                    writer.detail("Showing the most recent $MAX_DOSES_IN_REPORT of ${data.doseLogs.size} dose records.")
                }
            }

            writer.section("Symptoms and biomarkers")
            data.sideEffectLogs.sortedByDescending { it.date }.take(MAX_LOGS_PER_SECTION).forEach { log ->
                writer.item("${formatDate(log.date)} · ${log.type}${log.category?.let { " · $it" } ?: ""} · severity ${log.severity}/10")
                log.notes?.takeIf { it.isNotBlank() }?.let { writer.detail(it) }
            }
            data.biomarkerLogs.sortedByDescending { it.date }.take(MAX_LOGS_PER_SECTION).forEach { log ->
                writer.item("${formatDate(log.date)} · ${log.biomarkerName}: ${log.value} ${log.unit}")
                log.notes?.takeIf { it.isNotBlank() }?.let { writer.detail(it) }
            }
            if (data.sideEffectLogs.isEmpty() && data.biomarkerLogs.isEmpty()) {
                writer.paragraph("No symptom or biomarker records recorded.")
            }

            writer.section("Imported health metrics")
            data.healthMetrics.sortedByDescending { it.timestamp }.take(MAX_LOGS_PER_SECTION).forEach { metric ->
                writer.item("${formatDate(metric.timestamp)} · ${metric.metricType}: ${metric.value}${metric.secondaryValue?.let { " / $it" } ?: ""} · ${metric.source}")
            }
            if (data.healthMetrics.isEmpty()) writer.paragraph("No imported health metrics recorded.")

            writer.section("Record contents")
            writer.paragraph(
                "Protocols: ${data.protocols.size} · compounds: ${data.protocolCompounds.size} · doses: ${data.doseLogs.size} · " +
                    "symptom logs: ${data.sideEffectLogs.size} · biomarkers: ${data.biomarkerLogs.size} · health metrics: ${data.healthMetrics.size}"
            )
            writer.paragraph("Progress photos are not embedded in this report. Keep the original PepLog backup private.")
            writer.finish()
            FileOutputStream(outputFile).use(document::writeTo)
            return outputFile
        } finally {
            document.close()
        }
    }

    private class PageWriter(private val document: PdfDocument) {
        private var pageNumber = 0
        private var page = document.startPage(pageInfo())
        private var canvas = page.canvas
        private var y = TOP_MARGIN

        fun heading(text: String) {
            drawWrapped(text, headingPaint, 26f)
            y += 2f
        }

        fun subheading(text: String) = drawWrapped(text, subheadingPaint, 18f)

        fun section(text: String) {
            ensureRoom(28f)
            y += 8f
            drawWrapped(text.uppercase(Locale.getDefault()), sectionPaint, 20f)
            y += 2f
        }

        fun item(text: String) = drawWrapped("• $text", itemPaint, 17f)

        fun detail(text: String) = drawWrapped(text, detailPaint, 15f, left = DETAIL_LEFT)

        fun paragraph(text: String) = drawWrapped(text, bodyPaint, 17f)

        fun finish() {
            drawFooter()
            document.finishPage(page)
        }

        private fun drawWrapped(text: String, paint: Paint, lineHeight: Float, left: Float = CONTENT_LEFT) {
            wrap(text).forEach { line ->
                ensureRoom(lineHeight)
                canvas.drawText(line, left, y, paint)
                y += lineHeight
            }
        }

        private fun wrap(text: String): List<String> {
            val words = text.trim().split(Regex("\\s+")).filter(String::isNotBlank)
            if (words.isEmpty()) return listOf("")
            val lines = mutableListOf<String>()
            var current = StringBuilder()
            words.forEach { word ->
                if (current.isNotEmpty() && current.length + word.length + 1 > MAX_CHARS_PER_LINE) {
                    lines += current.toString()
                    current = StringBuilder()
                }
                if (current.isNotEmpty()) current.append(' ')
                current.append(word)
            }
            if (current.isNotEmpty()) lines += current.toString()
            return lines
        }

        private fun ensureRoom(lineHeight: Float) {
            if (y + lineHeight <= BOTTOM_MARGIN) return
            drawFooter()
            document.finishPage(page)
            pageNumber++
            page = document.startPage(pageInfo())
            canvas = page.canvas
            y = TOP_MARGIN
        }

        private fun drawFooter() {
            val footerPaint = Paint(bodyPaint).apply { textAlign = Paint.Align.CENTER }
            canvas.drawText("PepLog · page ${pageNumber + 1}", PAGE_WIDTH / 2f, PAGE_HEIGHT - 24f, footerPaint)
        }

        private fun pageInfo() = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber + 1).create()

        companion object {
            private const val PAGE_WIDTH = 595
            private const val PAGE_HEIGHT = 842
            private const val CONTENT_LEFT = 42f
            private const val DETAIL_LEFT = 58f
            private const val TOP_MARGIN = 48f
            private const val BOTTOM_MARGIN = 790f
            private const val MAX_CHARS_PER_LINE = 86
            private val headingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF21483D.toInt()
                textSize = 24f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            private val subheadingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF63736D.toInt()
                textSize = 11f
            }
            private val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFB5654A.toInt()
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            private val itemPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF26352F.toInt()
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF3F4B46.toInt()
                textSize = 10f
            }
            private val detailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF63736D.toInt()
                textSize = 9f
            }
        }
    }

    private companion object {
        const val MAX_DOSES_IN_REPORT = 120
        const val MAX_LOGS_PER_SECTION = 80

        fun formatDate(timestamp: Long): String =
            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault())
                .format(Date(timestamp))

        fun formatDose(dose: DoseLogBackup): String = buildString {
            append(formatDate(dose.actualTime ?: dose.scheduledTime))
            append(" · ")
            append(dose.doseAmount)
            append(' ')
            append(dose.doseUnit)
            append(" · ")
            append(dose.status)
            dose.injectionSite?.takeIf { it.isNotBlank() }?.let { append(" · $it") }
        }
    }
}
