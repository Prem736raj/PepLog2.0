package com.appvexis.peptidetracker.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appvexis.peptidetracker.core.model.ProtocolAnalyticsSummary

/**
 * Room Database Entity representing precomputed protocol aggregate statistics.
 */
@Entity(
    tableName = "analytics_protocol_summary",
    foreignKeys = [
        ForeignKey(
            entity = ProtocolEntity::class,
            parentColumns = ["id"],
            childColumns = ["protocol_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["protocol_id"])
    ]
)
data class ProtocolAnalyticsSummaryEntity(
    @PrimaryKey 
    val id: String,
    
    @ColumnInfo(name = "protocol_id") 
    val protocolId: String,
    
    @ColumnInfo(name = "total_days") 
    val totalDays: Int,
    
    @ColumnInfo(name = "total_doses_scheduled") 
    val totalDosesScheduled: Int,
    
    @ColumnInfo(name = "total_doses_taken") 
    val totalDosesTaken: Int,
    
    @ColumnInfo(name = "overall_adherence") 
    val overallAdherence: Double,
    
    @ColumnInfo(name = "most_common_side_effect") 
    val mostCommonSideEffect: String?,
    
    @ColumnInfo(name = "avg_side_effect_severity") 
    val avgSideEffectSeverity: Double?,
    
    @ColumnInfo(name = "biomarker_changes") 
    val biomarkerChanges: Map<String, Double>?,
    
    @ColumnInfo(name = "subjective_trends") 
    val subjectiveTrends: Map<String, Double>?,
    
    @ColumnInfo(name = "updated_at") 
    val updatedAt: Long
)

fun ProtocolAnalyticsSummaryEntity.toDomain() = ProtocolAnalyticsSummary(
    id = id,
    protocolId = protocolId,
    totalDays = totalDays,
    totalDosesScheduled = totalDosesScheduled,
    totalDosesTaken = totalDosesTaken,
    overallAdherence = overallAdherence,
    mostCommonSideEffect = mostCommonSideEffect,
    avgSideEffectSeverity = avgSideEffectSeverity,
    biomarkerChanges = biomarkerChanges,
    subjectiveTrends = subjectiveTrends,
    updatedAt = updatedAt
)

fun ProtocolAnalyticsSummary.toEntity() = ProtocolAnalyticsSummaryEntity(
    id = id,
    protocolId = protocolId,
    totalDays = totalDays,
    totalDosesScheduled = totalDosesScheduled,
    totalDosesTaken = totalDosesTaken,
    overallAdherence = overallAdherence,
    mostCommonSideEffect = mostCommonSideEffect,
    avgSideEffectSeverity = avgSideEffectSeverity,
    biomarkerChanges = biomarkerChanges,
    subjectiveTrends = subjectiveTrends,
    updatedAt = updatedAt
)
