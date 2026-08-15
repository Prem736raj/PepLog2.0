package com.appvexis.peptidetracker.core.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing a progress comparison photo.
 */
@Serializable
data class ProgressPhoto(
    val id: String,
    val photoUri: String,
    val date: Long,
    val protocolId: String?,
    val category: String?,            // 'front', 'side', 'back', or 'other'
    val notes: String?
)
