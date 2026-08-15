package com.appvexis.peptidetracker.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appvexis.peptidetracker.core.model.InventoryItem
import com.appvexis.peptidetracker.core.model.InventoryStatus

/**
 * Room Database Entity representing a peptide vial tracked in user inventory.
 */
@Entity(
    tableName = "inventory_item",
    foreignKeys = [
        ForeignKey(
            entity = PeptideEntity::class,
            parentColumns = ["id"],
            childColumns = ["peptide_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["peptide_id"])
    ]
)
data class InventoryItemEntity(
    @PrimaryKey 
    val id: String,
    
    @ColumnInfo(name = "peptide_id") 
    val peptideId: String,
    
    val vendor: String?,
    
    @ColumnInfo(name = "batch_number") 
    val batchNumber: String?,
    
    @ColumnInfo(name = "purchase_date") 
    val purchaseDate: Long?,
    
    @ColumnInfo(name = "vial_strength_mg") 
    val vialStrengthMg: Double,
    
    val quantity: Int,
    
    @ColumnInfo(name = "storage_location") 
    val storageLocation: String?,
    
    @ColumnInfo(name = "is_reconstituted") 
    val isReconstituted: Boolean,
    
    @ColumnInfo(name = "reconstitution_date") 
    val reconstitutionDate: Long?,
    
    @ColumnInfo(name = "bac_water_ml") 
    val bacWaterMl: Double?,
    
    @ColumnInfo(name = "concentration_mg_ml") 
    val concentrationMgMl: Double?,
    
    @ColumnInfo(name = "expiration_date") 
    val expirationDate: Long?,
    
    @ColumnInfo(name = "remaining_volume_ml") 
    val remainingVolumeMl: Double?,
    
    val notes: String?,
    val status: InventoryStatus
)

fun InventoryItemEntity.toDomain() = InventoryItem(
    id = id,
    peptideId = peptideId,
    vendor = vendor,
    batchNumber = batchNumber,
    purchaseDate = purchaseDate,
    vialStrengthMg = vialStrengthMg,
    quantity = quantity,
    storageLocation = storageLocation,
    isReconstituted = isReconstituted,
    reconstitutionDate = reconstitutionDate,
    bacWaterMl = bacWaterMl,
    concentrationMgMl = concentrationMgMl,
    expirationDate = expirationDate,
    remainingVolumeMl = remainingVolumeMl,
    notes = notes,
    status = status
)

fun InventoryItem.toEntity() = InventoryItemEntity(
    id = id,
    peptideId = peptideId,
    vendor = vendor,
    batchNumber = batchNumber,
    purchaseDate = purchaseDate,
    vialStrengthMg = vialStrengthMg,
    quantity = quantity,
    storageLocation = storageLocation,
    isReconstituted = isReconstituted,
    reconstitutionDate = reconstitutionDate,
    bacWaterMl = bacWaterMl,
    concentrationMgMl = concentrationMgMl,
    expirationDate = expirationDate,
    remainingVolumeMl = remainingVolumeMl,
    notes = notes,
    status = status
)
