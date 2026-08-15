package com.appvexis.peptidetracker.feature.health.model

import com.appvexis.peptidetracker.core.model.DoseLog
import com.appvexis.peptidetracker.core.model.HealthMetricRecord
import com.appvexis.peptidetracker.core.model.HealthMetricType
import com.appvexis.peptidetracker.core.model.HealthPattern

/**
 * Health Connect availability status.
 */
enum class HealthConnectStatus {
    AVAILABLE,
    NOT_INSTALLED,
    NOT_SUPPORTED,
    CHECKING
}

/**
 * Time range filter for health data charts.
 */
enum class HealthTimeRange(val label: String, val days: Int) {
    WEEK("7D", 7),
    MONTH("30D", 30),
    THREE_MONTHS("90D", 90),
    ALL("All", 365)
}

/**
 * Card summarizing a single metric's latest value and trend.
 */
data class HealthMetricSummary(
    val metricType: HealthMetricType,
    val latestValue: Double,
    val previousValue: Double?,
    val changePercent: Double?,
    val dataPointCount: Int
)

/**
 * Chart data point combining health metric with optional dose marker.
 */
data class CorrelationDataPoint(
    val timestamp: Long,
    val healthValue: Double,
    val hasDose: Boolean = false
)

/**
 * Full UI state for the Health Connect screen.
 */
data class HealthScreenUiState(
    val healthConnectStatus: HealthConnectStatus = HealthConnectStatus.CHECKING,
    val permissionsGranted: Boolean = false,
    val isSyncing: Boolean = false,
    val lastSyncTime: Long? = null,
    val selectedMetricType: HealthMetricType = HealthMetricType.WEIGHT,
    val selectedTimeRange: HealthTimeRange = HealthTimeRange.MONTH,
    val metricSummaries: List<HealthMetricSummary> = emptyList(),
    val chartData: List<CorrelationDataPoint> = emptyList(),
    val doseMarkers: List<Long> = emptyList(), // timestamps of taken doses for overlay
    val patterns: List<HealthPattern> = emptyList(),
    val allMetrics: List<HealthMetricRecord> = emptyList(),
    val doseLogs: List<DoseLog> = emptyList(),
    val error: String? = null
)
