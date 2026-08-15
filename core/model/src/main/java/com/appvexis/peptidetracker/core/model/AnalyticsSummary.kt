package com.appvexis.peptidetracker.core.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing precomputed daily metrics for a protocol stack.
 */
@Serializable
data class DailyAnalyticsSummary(
    val id: String,
    val date: Long,
    val protocolId: String,
    val totalDosesScheduled: Int,
    val totalDosesTaken: Int,
    val totalDosesMissed: Int,
    val adherencePercentage: Double,
    val avgMood: Double?,
    val avgEnergy: Double?,
    val avgSleepQuality: Double?,
    val avgPainLevel: Double?,
    val sideEffectCount: Int,
    val weight: Double?,
    val updatedAt: Long
)

/**
 * Domain model representing overall aggregated analytics for an entire protocol stack cycle.
 */
@Serializable
data class ProtocolAnalyticsSummary(
    val id: String,
    val protocolId: String,
    val totalDays: Int,
    val totalDosesScheduled: Int,
    val totalDosesTaken: Int,
    val overallAdherence: Double,
    val mostCommonSideEffect: String?,
    val avgSideEffectSeverity: Double?,
    val biomarkerChanges: Map<String, Double>?,     // E.g., {"IGF-1": +86.0, "Testosterone": -12.0}
    val subjectiveTrends: Map<String, Double>?,     // E.g., {"mood": +1.5, "energy": +2.1}
    val updatedAt: Long
)
