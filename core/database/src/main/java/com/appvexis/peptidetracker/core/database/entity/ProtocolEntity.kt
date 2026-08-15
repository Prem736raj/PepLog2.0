package com.appvexis.peptidetracker.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.appvexis.peptidetracker.core.model.Protocol
import com.appvexis.peptidetracker.core.model.ProtocolStatus

/**
 * Room Database Entity representing the user's stack protocol.
 */
@Entity(tableName = "protocol")
data class ProtocolEntity(
    @PrimaryKey 
    val id: String,
    val name: String,
    val goal: String?,
    val status: String, // Maps to ProtocolStatus enum name
    
    @ColumnInfo(name = "start_date") 
    val startDate: Long?,
    
    @ColumnInfo(name = "end_date") 
    val endDate: Long?,
    
    val notes: String?,
    
    @ColumnInfo(name = "created_at") 
    val createdAt: Long,
    
    @ColumnInfo(name = "updated_at") 
    val updatedAt: Long
)

fun ProtocolEntity.toDomain() = Protocol(
    id = id,
    name = name,
    goal = goal,
    status = ProtocolStatus.valueOf(status),
    startDate = startDate,
    endDate = endDate,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Protocol.toEntity() = ProtocolEntity(
    id = id,
    name = name,
    goal = goal,
    status = status.name,
    startDate = startDate,
    endDate = endDate,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt
)
