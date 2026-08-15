package com.appvexis.peptidetracker.core.model

import kotlinx.serialization.Serializable

/**
 * Supported health metric types from Health Connect.
 */
enum class HealthMetricType(val displayName: String, val unit: String, val icon: String) {
    WEIGHT("Weight", "kg", "scale"),
    SLEEP_DURATION("Sleep Duration", "hrs", "bedtime"),
    HEART_RATE("Heart Rate", "bpm", "favorite"),
    BLOOD_PRESSURE_SYSTOLIC("Systolic BP", "mmHg", "monitor_heart"),
    BLOOD_PRESSURE_DIASTOLIC("Diastolic BP", "mmHg", "monitor_heart"),
    STEPS("Steps", "steps", "directions_walk"),
    BODY_FAT("Body Fat", "%", "fitness_center"),
    RESTING_HEART_RATE("Resting HR", "bpm", "heart_check")
}

/**
 * Domain model representing a single health metric reading synced from Health Connect.
 */
@Serializable
data class HealthMetricRecord(
    val id: String,
    val metricType: HealthMetricType,
    val value: Double,
    val secondaryValue: Double? = null, // e.g., diastolic BP when systolic is primary
    val timestamp: Long,
    val source: String = "Health Connect",
    val protocolId: String? = null // optional link to active protocol at time of recording
)

/**
 * Aggregated health metric point for charting.
 */
@Serializable
data class HealthMetricDataPoint(
    val timestamp: Long,
    val value: Double,
    val metricType: HealthMetricType
)

/**
 * Detected pattern/insight from health + dose correlation analysis.
 */
data class HealthPattern(
    val title: String,
    val description: String,
    val metricType: HealthMetricType,
    val changePercent: Double,
    val isPositive: Boolean,
    val protocolName: String?,
    val confidence: Float // 0.0 to 1.0
)
