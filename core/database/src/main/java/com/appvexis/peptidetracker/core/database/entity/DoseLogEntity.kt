package com.appvexis.peptidetracker.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appvexis.peptidetracker.core.model.DoseLog
import com.appvexis.peptidetracker.core.model.DoseStatus
import com.appvexis.peptidetracker.core.model.DoseUnit

/**
 * Room Database Entity representing a log entry for a scheduled or as-needed dose.
 */
@Entity(
    tableName = "dose_log",
    foreignKeys = [
        ForeignKey(
            entity = ProtocolCompoundEntity::class,
            parentColumns = ["id"],
            childColumns = ["protocol_compound_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["protocol_compound_id"])
    ]
)
data class DoseLogEntity(
    @PrimaryKey 
    val id: String,
    
    @ColumnInfo(name = "protocol_compound_id") 
    val protocolCompoundId: String,
    
    @ColumnInfo(name = "scheduled_time") 
    val scheduledTime: Long,
    
    @ColumnInfo(name = "actual_time") 
    val actualTime: Long?,
    
    @ColumnInfo(name = "dose_amount") 
    val doseAmount: Double,
    
    @ColumnInfo(name = "dose_unit") 
    val doseUnit: DoseUnit,
    
    val status: DoseStatus,
    
    @ColumnInfo(name = "injection_site") 
    val injectionSite: String?,
    
    @ColumnInfo(name = "injection_side") 
    val injectionSide: String?,
    
    val notes: String?,
    
    @ColumnInfo(name = "created_at") 
    val createdAt: Long
)

fun DoseLogEntity.toDomain() = DoseLog(
    id = id,
    protocolCompoundId = protocolCompoundId,
    scheduledTime = scheduledTime,
    actualTime = actualTime,
    doseAmount = doseAmount,
    doseUnit = doseUnit,
    status = status,
    injectionSite = injectionSite,
    injectionSide = injectionSide,
    notes = notes,
    createdAt = createdAt
)

fun DoseLog.toEntity() = DoseLogEntity(
    id = id,
    protocolCompoundId = protocolCompoundId,
    scheduledTime = scheduledTime,
    actualTime = actualTime,
    doseAmount = doseAmount,
    doseUnit = doseUnit,
    status = status,
    injectionSite = injectionSite,
    injectionSide = injectionSide,
    notes = notes,
    createdAt = createdAt
)
