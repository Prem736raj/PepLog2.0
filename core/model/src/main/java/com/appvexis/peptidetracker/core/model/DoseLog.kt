package com.appvexis.peptidetracker.core.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing a log entry for a scheduled or as-needed dose.
 */
@Serializable
data class DoseLog(
    val id: String,
    val protocolCompoundId: String,
    val scheduledTime: Long,
    val actualTime: Long?,
    val doseAmount: Double,
    val doseUnit: DoseUnit,
    val status: DoseStatus = DoseStatus.PENDING,
    val injectionSite: String?,
    val injectionSide: String?, // 'left', 'right', or null
    val notes: String?,
    val createdAt: Long
)
