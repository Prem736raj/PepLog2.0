package com.appvexis.peptidetracker.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appvexis.peptidetracker.core.model.HealingStatus
import com.appvexis.peptidetracker.core.model.InjectionSiteLog

/**
 * Room Database Entity representing the state of an injection site.
 */
@Entity(
    tableName = "injection_site_log",
    foreignKeys = [
        ForeignKey(
            entity = DoseLogEntity::class,
            parentColumns = ["id"],
            childColumns = ["dose_log_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["dose_log_id"])
    ]
)
data class InjectionSiteLogEntity(
    @PrimaryKey 
    val id: String,
    
    @ColumnInfo(name = "dose_log_id") 
    val doseLogId: String?,
    
    @ColumnInfo(name = "body_area") 
    val bodyArea: String,
    
    @ColumnInfo(name = "pain_level") 
    val painLevel: Int?,
    
    @ColumnInfo(name = "reaction_notes") 
    val reactionNotes: String?,
    
    @ColumnInfo(name = "healing_status") 
    val healingStatus: HealingStatus,
    
    val timestamp: Long
)

fun InjectionSiteLogEntity.toDomain() = InjectionSiteLog(
    id = id,
    doseLogId = doseLogId,
    bodyArea = bodyArea,
    painLevel = painLevel,
    reactionNotes = reactionNotes,
    healingStatus = healingStatus,
    timestamp = timestamp
)

fun InjectionSiteLog.toEntity() = InjectionSiteLogEntity(
    id = id,
    doseLogId = doseLogId,
    bodyArea = bodyArea,
    painLevel = painLevel,
    reactionNotes = reactionNotes,
    healingStatus = healingStatus,
    timestamp = timestamp
)
