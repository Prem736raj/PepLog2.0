package com.appvexis.peptidetracker.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appvexis.peptidetracker.core.model.BiomarkerLog

/**
 * Room Database Entity representing blood lab biomarker measurements.
 */
@Entity(
    tableName = "biomarker_log",
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
data class BiomarkerLogEntity(
    @PrimaryKey 
    val id: String,
    
    @ColumnInfo(name = "biomarker_name") 
    val biomarkerName: String,
    
    val value: Double,
    val unit: String,
    val date: Long,
    
    @ColumnInfo(name = "protocol_id") 
    val protocolId: String?,
    
    @ColumnInfo(name = "lab_name") 
    val labName: String?,
    
    val notes: String?
)

fun BiomarkerLogEntity.toDomain() = BiomarkerLog(
    id = id,
    biomarkerName = biomarkerName,
    value = value,
    unit = unit,
    date = date,
    protocolId = protocolId,
    labName = labName,
    notes = notes
)

fun BiomarkerLog.toEntity() = BiomarkerLogEntity(
    id = id,
    biomarkerName = biomarkerName,
    value = value,
    unit = unit,
    date = date,
    protocolId = protocolId,
    labName = labName,
    notes = notes
)
