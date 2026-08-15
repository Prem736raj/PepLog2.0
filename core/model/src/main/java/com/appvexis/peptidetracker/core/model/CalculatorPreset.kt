package com.appvexis.peptidetracker.core.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing a saved reconstitution calculator preset.
 */
@Serializable
data class CalculatorPreset(
    val id: String,
    val name: String,
    val peptideId: String?,
    val vialStrengthMg: Double,
    val bacWaterMl: Double,
    val desiredDoseMg: Double,
    val createdAt: Long
)
