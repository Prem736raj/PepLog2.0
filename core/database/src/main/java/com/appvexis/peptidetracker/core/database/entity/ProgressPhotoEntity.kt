package com.appvexis.peptidetracker.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appvexis.peptidetracker.core.model.ProgressPhoto

/**
 * Room Database Entity representing comparison progress photos.
 */
@Entity(
    tableName = "progress_photo",
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
data class ProgressPhotoEntity(
    @PrimaryKey 
    val id: String,
    
    @ColumnInfo(name = "photo_uri") 
    val photoUri: String,
    
    val date: Long,
    
    @ColumnInfo(name = "protocol_id") 
    val protocolId: String?,
    
    val category: String?,
    val notes: String?
)

fun ProgressPhotoEntity.toDomain() = ProgressPhoto(
    id = id,
    photoUri = photoUri,
    date = date,
    protocolId = protocolId,
    category = category,
    notes = notes
)

fun ProgressPhoto.toEntity() = ProgressPhotoEntity(
    id = id,
    photoUri = photoUri,
    date = date,
    protocolId = protocolId,
    category = category,
    notes = notes
)
