package com.appvexis.peptidetracker.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appvexis.peptidetracker.core.model.AdminRoute
import com.appvexis.peptidetracker.core.model.DoseUnit
import com.appvexis.peptidetracker.core.model.FrequencyType
import com.appvexis.peptidetracker.core.model.ProtocolCompound
import com.appvexis.peptidetracker.core.model.TitrationStep

/**
 * Room Database Entity representing a peptide compound mapping inside a cycle protocol stack.
 */
@Entity(
    tableName = "protocol_compound",
    foreignKeys = [
        ForeignKey(
            entity = ProtocolEntity::class,
            parentColumns = ["id"],
            childColumns = ["protocol_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PeptideEntity::class,
            parentColumns = ["id"],
            childColumns = ["peptide_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["protocol_id"]),
        Index(value = ["peptide_id"])
    ]
)
data class ProtocolCompoundEntity(
    @PrimaryKey 
    val id: String,
    
    @ColumnInfo(name = "protocol_id") 
    val protocolId: String,
    
    @ColumnInfo(name = "peptide_id") 
    val peptideId: String,
    
    @ColumnInfo(name = "dose_amount") 
    val doseAmount: Double,
    
    @ColumnInfo(name = "dose_unit") 
    val doseUnit: DoseUnit,
    
    @ColumnInfo(name = "frequency_type") 
    val frequencyType: FrequencyType,
    
    @ColumnInfo(name = "frequency_days") 
    val frequencyDays: List<Int>?,
    
    @ColumnInfo(name = "time_of_day") 
    val timeOfDay: String,
    
    @ColumnInfo(name = "admin_route") 
    val adminRoute: AdminRoute,
    
    @ColumnInfo(name = "titration_enabled") 
    val titrationEnabled: Boolean,
    
    @ColumnInfo(name = "titration_schedule") 
    val titrationSchedule: List<TitrationStep>?,
    
    @ColumnInfo(name = "start_date") 
    val startDate: Long?,
    
    @ColumnInfo(name = "end_date") 
    val endDate: Long?,
    
    @ColumnInfo(name = "is_active") 
    val isActive: Boolean,
    
    val notes: String?
)

fun ProtocolCompoundEntity.toDomain() = ProtocolCompound(
    id = id,
    protocolId = protocolId,
    peptideId = peptideId,
    doseAmount = doseAmount,
    doseUnit = doseUnit,
    frequencyType = frequencyType,
    frequencyDays = frequencyDays,
    timeOfDay = timeOfDay,
    adminRoute = adminRoute,
    titrationEnabled = titrationEnabled,
    titrationSchedule = titrationSchedule,
    startDate = startDate,
    endDate = endDate,
    isActive = isActive,
    notes = notes
)

fun ProtocolCompound.toEntity() = ProtocolCompoundEntity(
    id = id,
    protocolId = protocolId,
    peptideId = peptideId,
    doseAmount = doseAmount,
    doseUnit = doseUnit,
    frequencyType = frequencyType,
    frequencyDays = frequencyDays,
    timeOfDay = timeOfDay,
    adminRoute = adminRoute,
    titrationEnabled = titrationEnabled,
    titrationSchedule = titrationSchedule,
    startDate = startDate,
    endDate = endDate,
    isActive = isActive,
    notes = notes
)
