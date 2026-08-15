package com.appvexis.peptidetracker.feature.health.data

import com.appvexis.peptidetracker.core.model.DoseLog
import com.appvexis.peptidetracker.core.model.DoseStatus
import com.appvexis.peptidetracker.core.model.HealthMetricRecord
import com.appvexis.peptidetracker.core.model.HealthMetricType
import com.appvexis.peptidetracker.core.model.HealthPattern
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Detects patterns and correlations between health metrics and dose timelines.
 * Generates human-readable insight cards like:
 * "Sleep quality improved 23% during CJC-1295 protocol"
 */
@Singleton
class HealthPatternDetector @Inject constructor() {

    /**
     * Analyze all metric types against the dose timeline and return detected patterns.
     *
     * @param healthMetrics All health metric records in the analysis window.
     * @param doseLogs All dose logs in the analysis window.
     * @param protocolName Name of the active protocol being analyzed.
     */
    fun detectPatterns(
        healthMetrics: List<HealthMetricRecord>,
        doseLogs: List<DoseLog>,
        protocolName: String?
    ): List<HealthPattern> {
        if (healthMetrics.isEmpty() || doseLogs.isEmpty()) return emptyList()

        val patterns = mutableListOf<HealthPattern>()
        val takenDoses = doseLogs.filter { it.status == DoseStatus.TAKEN }
        if (takenDoses.isEmpty()) return emptyList()

        // Get the date range of the protocol usage
        val protocolStart = takenDoses.minOf { it.actualTime ?: it.scheduledTime }
        val protocolEnd = takenDoses.maxOf { it.actualTime ?: it.scheduledTime }

        // For each metric type, compare pre-protocol vs during-protocol averages
        for (metricType in HealthMetricType.entries) {
            val metricsOfType = healthMetrics.filter { it.metricType == metricType }
            if (metricsOfType.size < 3) continue // Need minimum data points

            val pattern = analyzeMetricChange(
                metricsOfType = metricsOfType,
                protocolStart = protocolStart,
                protocolEnd = protocolEnd,
                metricType = metricType,
                protocolName = protocolName
            )
            if (pattern != null) {
                patterns.add(pattern)
            }
        }

        // Additional: detect dose-timing correlations
        patterns.addAll(detectTimingCorrelations(healthMetrics, takenDoses, protocolName))

        return patterns.sortedByDescending { it.confidence }
    }

    /**
     * Compare average metric values before and during the protocol.
     */
    private fun analyzeMetricChange(
        metricsOfType: List<HealthMetricRecord>,
        protocolStart: Long,
        protocolEnd: Long,
        metricType: HealthMetricType,
        protocolName: String?
    ): HealthPattern? {
        val preProtocol = metricsOfType.filter { it.timestamp < protocolStart }
        val duringProtocol = metricsOfType.filter {
            it.timestamp in protocolStart..protocolEnd
        }

        if (preProtocol.size < 2 || duringProtocol.size < 2) return null

        val preAvg = preProtocol.map { it.value }.average()
        val duringAvg = duringProtocol.map { it.value }.average()

        if (preAvg == 0.0) return null

        val changePercent = ((duringAvg - preAvg) / preAvg) * 100

        // Only report significant changes (>5%)
        if (abs(changePercent) < 5.0) return null

        val isPositive = isChangePositive(metricType, changePercent)
        val confidence = calculateConfidence(preProtocol.size, duringProtocol.size, abs(changePercent))

        val directionWord = if (changePercent > 0) "increased" else "decreased"
        val description = "${metricType.displayName} $directionWord by ${String.format("%.1f", abs(changePercent))}% " +
                "during ${protocolName ?: "your protocol"} " +
                "(${String.format("%.1f", preAvg)} → ${String.format("%.1f", duringAvg)} ${metricType.unit})"

        val title = when {
            isPositive -> "${metricType.displayName} Improved ↑"
            else -> "${metricType.displayName} Changed ↓"
        }

        return HealthPattern(
            title = title,
            description = description,
            metricType = metricType,
            changePercent = changePercent,
            isPositive = isPositive,
            protocolName = protocolName,
            confidence = confidence
        )
    }

    /**
     * Detect correlations based on dose timing (e.g., sleep improvement on dosing days).
     */
    private fun detectTimingCorrelations(
        healthMetrics: List<HealthMetricRecord>,
        takenDoses: List<DoseLog>,
        protocolName: String?
    ): List<HealthPattern> {
        val patterns = mutableListOf<HealthPattern>()

        // Group doses by day (ms -> day boundary)
        val doseDays = takenDoses.map { dayOf(it.actualTime ?: it.scheduledTime) }.toSet()

        // Analyze sleep on dose days vs non-dose days
        val sleepMetrics = healthMetrics.filter { it.metricType == HealthMetricType.SLEEP_DURATION }
        if (sleepMetrics.size >= 5) {
            val onDoseDays = sleepMetrics.filter { dayOf(it.timestamp) in doseDays }
            val offDoseDays = sleepMetrics.filter { dayOf(it.timestamp) !in doseDays }

            if (onDoseDays.size >= 2 && offDoseDays.size >= 2) {
                val onAvg = onDoseDays.map { it.value }.average()
                val offAvg = offDoseDays.map { it.value }.average()
                val diff = ((onAvg - offAvg) / offAvg) * 100

                if (abs(diff) > 10.0) {
                    val better = if (diff > 0) "more" else "less"
                    patterns.add(
                        HealthPattern(
                            title = "Sleep on Dosing Days",
                            description = "You sleep ${String.format("%.1f", abs(diff))}% $better on dosing days " +
                                    "(${String.format("%.1f", onAvg)}h vs ${String.format("%.1f", offAvg)}h)",
                            metricType = HealthMetricType.SLEEP_DURATION,
                            changePercent = diff,
                            isPositive = diff > 0,
                            protocolName = protocolName,
                            confidence = calculateConfidence(offDoseDays.size, onDoseDays.size, abs(diff))
                        )
                    )
                }
            }
        }

        // Analyze heart rate variability around dose times
        val hrMetrics = healthMetrics.filter { it.metricType == HealthMetricType.RESTING_HEART_RATE }
        if (hrMetrics.size >= 5) {
            val onDoseDays = hrMetrics.filter { dayOf(it.timestamp) in doseDays }
            val offDoseDays = hrMetrics.filter { dayOf(it.timestamp) !in doseDays }

            if (onDoseDays.size >= 2 && offDoseDays.size >= 2) {
                val onAvg = onDoseDays.map { it.value }.average()
                val offAvg = offDoseDays.map { it.value }.average()
                val diff = ((onAvg - offAvg) / offAvg) * 100

                if (abs(diff) > 3.0) {
                    val direction = if (diff < 0) "lower" else "higher"
                    patterns.add(
                        HealthPattern(
                            title = "Resting HR on Dosing Days",
                            description = "Resting heart rate is ${String.format("%.1f", abs(diff))}% $direction on dosing days " +
                                    "(${String.format("%.0f", onAvg)} vs ${String.format("%.0f", offAvg)} bpm)",
                            metricType = HealthMetricType.RESTING_HEART_RATE,
                            changePercent = diff,
                            isPositive = diff < 0, // Lower resting HR is positive
                            protocolName = protocolName,
                            confidence = calculateConfidence(offDoseDays.size, onDoseDays.size, abs(diff))
                        )
                    )
                }
            }
        }

        return patterns
    }

    /**
     * Determine if a change in a metric is positive (health-improving).
     */
    private fun isChangePositive(metricType: HealthMetricType, changePercent: Double): Boolean {
        return when (metricType) {
            HealthMetricType.SLEEP_DURATION -> changePercent > 0    // More sleep = good
            HealthMetricType.STEPS -> changePercent > 0              // More steps = good
            HealthMetricType.WEIGHT -> false                         // Neutral — depends on context
            HealthMetricType.HEART_RATE -> changePercent < 0         // Lower = generally good
            HealthMetricType.RESTING_HEART_RATE -> changePercent < 0 // Lower = good
            HealthMetricType.BLOOD_PRESSURE_SYSTOLIC -> changePercent < 0  // Lower = good
            HealthMetricType.BLOOD_PRESSURE_DIASTOLIC -> changePercent < 0 // Lower = good
            HealthMetricType.BODY_FAT -> changePercent < 0           // Lower = good
        }
    }

    /**
     * Calculate confidence score based on sample sizes and effect size.
     */
    private fun calculateConfidence(preCount: Int, duringCount: Int, effectSize: Double): Float {
        val sampleScore = minOf((preCount + duringCount) / 20f, 1f)  // More samples = higher confidence
        val effectScore = minOf(effectSize.toFloat() / 30f, 1f)       // Larger effect = higher confidence
        return (sampleScore * 0.6f + effectScore * 0.4f).coerceIn(0.1f, 0.95f)
    }

    /**
     * Normalize timestamp to day boundary for day-level comparisons.
     */
    private fun dayOf(timestamp: Long): Long {
        return timestamp / (24 * 60 * 60 * 1000) * (24 * 60 * 60 * 1000)
    }
}
