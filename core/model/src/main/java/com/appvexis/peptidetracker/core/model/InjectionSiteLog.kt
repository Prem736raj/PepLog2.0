package com.appvexis.peptidetracker.core.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing an injection site status logging record.
 */
@Serializable
data class InjectionSiteLog(
    val id: String,
    val doseLogId: String?,
    val bodyArea: String,             // E.g., 'abdomen_upper_left', 'thigh_outer_right'
    val painLevel: Int?,              // Scale of 1 to 5
    val reactionNotes: String?,
    val healingStatus: HealingStatus = HealingStatus.OK,
    val timestamp: Long
)
