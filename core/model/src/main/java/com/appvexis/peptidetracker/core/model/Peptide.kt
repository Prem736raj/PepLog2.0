package com.appvexis.peptidetracker.core.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing a single peptide.
 */
@Serializable
data class Peptide(
    val id: String,
    val name: String,
    val category: String,
    val description: String?,
    val halfLifeHours: Double?,
    val halfLifeDisplay: String?,
    val adminRoute: String?,
    val typicalFrequency: String?,
    val typicalDoseRange: String?,
    val storageInfo: String?,
    val sideEffects: List<String> = emptyList(),
    val synergies: List<String> = emptyList(),
    val contraindications: List<String> = emptyList(),
    val isBookmarked: Boolean = false,
    // New fields from encyclopedia audit
    val tags: List<String> = emptyList(),
    val aliases: List<String> = emptyList(),
    val mechanismOfAction: String? = null,
    val legalStatus: String? = null,
    val researchEvidence: String? = null,
    val peptideClass: String? = null,
    val cycleRecommendation: String? = null,
    val onsetDays: String? = null
)
