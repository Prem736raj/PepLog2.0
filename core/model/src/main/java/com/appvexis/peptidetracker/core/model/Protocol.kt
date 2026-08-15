package com.appvexis.peptidetracker.core.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing a user's peptide cycle protocol stack.
 */
@Serializable
data class Protocol(
    val id: String,
    val name: String,
    val goal: String?,
    val status: ProtocolStatus = ProtocolStatus.ACTIVE,
    val startDate: Long?,
    val endDate: Long?,
    val notes: String?,
    val createdAt: Long,
    val updatedAt: Long
)
