package com.appvexis.peptidetracker.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appvexis.peptidetracker.core.model.SideEffectLog

/**
 * Room Database Entity representing logged side effects and subjective scores.
 */
@Entity(
    tableName = "side_effect_log",
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
data class SideEffectLogEntity(
    @PrimaryKey 
    val id: String,
    
    @ColumnInfo(name = "protocol_id") 
    val protocolId: String?,
    
    val date: Long,
    val type: String,
    val category: String?,
    val severity: Int,
    val notes: String?,
    
    @ColumnInfo(name = "created_at") 
    val createdAt: Long
)

fun SideEffectLogEntity.toDomain() = SideEffectLog(
    id = id,
    protocolId = protocolId,
    date = date,
    type = type,
    category = category,
    severity = severity,
    notes = notes,
    createdAt = createdAt
)

fun SideEffectLog.toEntity() = SideEffectLogEntity(
    id = id,
    protocolId = protocolId,
    date = date,
    type = type,
    category = category,
    severity = severity,
    notes = notes,
    createdAt = createdAt
)
