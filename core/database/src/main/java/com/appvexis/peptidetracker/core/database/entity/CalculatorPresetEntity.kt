package com.appvexis.peptidetracker.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appvexis.peptidetracker.core.model.CalculatorPreset

/**
 * Room Database Entity representing a saved calculator preset.
 */
@Entity(
    tableName = "calculator_preset",
    foreignKeys = [
        ForeignKey(
            entity = PeptideEntity::class,
            parentColumns = ["id"],
            childColumns = ["peptide_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["peptide_id"])
    ]
)
data class CalculatorPresetEntity(
    @PrimaryKey 
    val id: String,
    val name: String,
    
    @ColumnInfo(name = "peptide_id") 
    val peptideId: String?,
    
    @ColumnInfo(name = "vial_strength_mg") 
    val vialStrengthMg: Double,
    
    @ColumnInfo(name = "bac_water_ml") 
    val bacWaterMl: Double,
    
    @ColumnInfo(name = "desired_dose_mg") 
    val desiredDoseMg: Double,
    
    @ColumnInfo(name = "created_at") 
    val createdAt: Long
)

fun CalculatorPresetEntity.toDomain() = CalculatorPreset(
    id = id,
    name = name,
    peptideId = peptideId,
    vialStrengthMg = vialStrengthMg,
    bacWaterMl = bacWaterMl,
    desiredDoseMg = desiredDoseMg,
    createdAt = createdAt
)

fun CalculatorPreset.toEntity() = CalculatorPresetEntity(
    id = id,
    name = name,
    peptideId = peptideId,
    vialStrengthMg = vialStrengthMg,
    bacWaterMl = bacWaterMl,
    desiredDoseMg = desiredDoseMg,
    createdAt = createdAt
)
