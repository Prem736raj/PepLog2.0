package com.appvexis.peptidetracker.core.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing logged side effects or subjective health metrics (mood, sleep, pain, etc.).
 */
@Serializable
data class SideEffectLog(
    val id: String,
    val protocolId: String?,
    val date: Long,
    val type: String,                 // 'side_effect', 'mood', 'energy', 'sleep', 'pain', 'libido'
    val category: String?,             // E.g., 'nausea', 'headache', 'fatigue'
    val severity: Int,                 // Scale from 1 to 10
    val notes: String?,
    val createdAt: Long
)
