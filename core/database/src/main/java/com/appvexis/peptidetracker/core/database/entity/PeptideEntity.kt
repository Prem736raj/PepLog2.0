package com.appvexis.peptidetracker.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.appvexis.peptidetracker.core.model.Peptide

/**
 * Room Database Entity representing the Peptide encyclopedia reference.
 */
@Entity(tableName = "peptide")
data class PeptideEntity(
    @PrimaryKey 
    val id: String,
    val name: String,
    val category: String,
    val description: String?,
    
    @ColumnInfo(name = "half_life_hours") 
    val halfLifeHours: Double?,
    
    @ColumnInfo(name = "half_life_display") 
    val halfLifeDisplay: String?,
    
    @ColumnInfo(name = "admin_route") 
    val adminRoute: String?,
    
    @ColumnInfo(name = "typical_frequency") 
    val typicalFrequency: String?,
    
    @ColumnInfo(name = "typical_dose_range") 
    val typicalDoseRange: String?,
    
    @ColumnInfo(name = "storage_info") 
    val storageInfo: String?,
    
    // Stored as comma-separated strings in Room for simplicity
    @ColumnInfo(name = "side_effects") 
    val sideEffects: String?,
    
    val synergies: String?,
    val contraindications: String?,
    
    @ColumnInfo(name = "is_bookmarked") 
    val isBookmarked: Boolean,

    // New fields from encyclopedia audit
    val tags: String?,
    val aliases: String?,
    
    @ColumnInfo(name = "mechanism_of_action")
    val mechanismOfAction: String?,
    
    @ColumnInfo(name = "legal_status")
    val legalStatus: String?,
    
    @ColumnInfo(name = "research_evidence")
    val researchEvidence: String?,
    
    @ColumnInfo(name = "peptide_class")
    val peptideClass: String?,
    
    @ColumnInfo(name = "cycle_recommendation")
    val cycleRecommendation: String?,
    
    @ColumnInfo(name = "onset_days")
    val onsetDays: String?
)

private const val SEPARATOR = "|||"

private fun List<String>.toDbString(): String? {
    return if (isEmpty()) null else joinToString(SEPARATOR)
}

private fun String?.toStringList(): List<String> {
    if (this.isNullOrBlank()) return emptyList()
    return split(SEPARATOR).map { it.trim() }.filter { it.isNotEmpty() }
}

fun PeptideEntity.toDomain() = Peptide(
    id = id,
    name = name,
    category = category,
    description = description,
    halfLifeHours = halfLifeHours,
    halfLifeDisplay = halfLifeDisplay,
    adminRoute = adminRoute,
    typicalFrequency = typicalFrequency,
    typicalDoseRange = typicalDoseRange,
    storageInfo = storageInfo,
    sideEffects = sideEffects.toStringList(),
    synergies = synergies.toStringList(),
    contraindications = contraindications.toStringList(),
    isBookmarked = isBookmarked,
    tags = tags.toStringList(),
    aliases = aliases.toStringList(),
    mechanismOfAction = mechanismOfAction,
    legalStatus = legalStatus,
    researchEvidence = researchEvidence,
    peptideClass = peptideClass,
    cycleRecommendation = cycleRecommendation,
    onsetDays = onsetDays
)

fun Peptide.toEntity() = PeptideEntity(
    id = id,
    name = name,
    category = category,
    description = description,
    halfLifeHours = halfLifeHours,
    halfLifeDisplay = halfLifeDisplay,
    adminRoute = adminRoute,
    typicalFrequency = typicalFrequency,
    typicalDoseRange = typicalDoseRange,
    storageInfo = storageInfo,
    sideEffects = sideEffects.toDbString(),
    synergies = synergies.toDbString(),
    contraindications = contraindications.toDbString(),
    isBookmarked = isBookmarked,
    tags = tags.toDbString(),
    aliases = aliases.toDbString(),
    mechanismOfAction = mechanismOfAction,
    legalStatus = legalStatus,
    researchEvidence = researchEvidence,
    peptideClass = peptideClass,
    cycleRecommendation = cycleRecommendation,
    onsetDays = onsetDays
)
