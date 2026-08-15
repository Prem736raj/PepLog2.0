package com.appvexis.peptidetracker.core.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing logged biomarker test results (e.g. IGF-1 levels, HbA1c).
 */
@Serializable
data class BiomarkerLog(
    val id: String,
    val biomarkerName: String,
    val value: Double,
    val unit: String,
    val date: Long,
    val protocolId: String?,
    val labName: String?,
    val notes: String?
)
