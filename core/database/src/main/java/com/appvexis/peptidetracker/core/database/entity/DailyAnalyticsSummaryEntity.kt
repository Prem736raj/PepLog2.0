package com.appvexis.peptidetracker.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appvexis.peptidetracker.core.model.DailyAnalyticsSummary

/**
 * Room Database Entity representing precomputed daily analytics summary details.
 */
@Entity(
    tableName = "analytics_daily_summary",
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
data class DailyAnalyticsSummaryEntity(
    @PrimaryKey 
    val id: String,
    val date: Long,
    
    @ColumnInfo(name = "protocol_id") 
    val protocolId: String,
    
    @ColumnInfo(name = "total_doses_scheduled") 
    val totalDosesScheduled: Int,
    
    @ColumnInfo(name = "total_doses_taken") 
    val totalDosesTaken: Int,
    
    @ColumnInfo(name = "total_doses_missed") 
    val totalDosesMissed: Int,
    
    @ColumnInfo(name = "adherence_percentage") 
    val adherencePercentage: Double,
    
    @ColumnInfo(name = "avg_mood") 
    val avgMood: Double?,
    
    @ColumnInfo(name = "avg_energy") 
    val avgEnergy: Double?,
    
    @ColumnInfo(name = "avg_sleep_quality") 
    val avgSleepQuality: Double?,
    
    @ColumnInfo(name = "avg_pain_level") 
    val avgPainLevel: Double?,
    
    @ColumnInfo(name = "side_effect_count") 
    val sideEffectCount: Int,
    
    val weight: Double?,
    
    @ColumnInfo(name = "updated_at") 
    val updatedAt: Long
)

fun DailyAnalyticsSummaryEntity.toDomain() = DailyAnalyticsSummary(
    id = id,
    date = date,
    protocolId = protocolId,
    totalDosesScheduled = totalDosesScheduled,
    totalDosesTaken = totalDosesTaken,
    totalDosesMissed = totalDosesMissed,
    adherencePercentage = adherencePercentage,
    avgMood = avgMood,
    avgEnergy = avgEnergy,
    avgSleepQuality = avgSleepQuality,
    avgPainLevel = avgPainLevel,
    sideEffectCount = sideEffectCount,
    weight = weight,
    updatedAt = updatedAt
)

fun DailyAnalyticsSummary.toEntity() = DailyAnalyticsSummaryEntity(
    id = id,
    date = date,
    protocolId = protocolId,
    totalDosesScheduled = totalDosesScheduled,
    totalDosesTaken = totalDosesTaken,
    totalDosesMissed = totalDosesMissed,
    adherencePercentage = adherencePercentage,
    avgMood = avgMood,
    avgEnergy = avgEnergy,
    avgSleepQuality = avgSleepQuality,
    avgPainLevel = avgPainLevel,
    sideEffectCount = sideEffectCount,
    weight = weight,
    updatedAt = updatedAt
)
