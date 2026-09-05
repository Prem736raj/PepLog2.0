package com.appvexis.peptidetracker.core.database.converters

import androidx.room.TypeConverter
import com.appvexis.peptidetracker.core.model.AdminRoute
import com.appvexis.peptidetracker.core.model.DoseStatus
import com.appvexis.peptidetracker.core.model.DoseUnit
import com.appvexis.peptidetracker.core.model.FrequencyType
import com.appvexis.peptidetracker.core.model.InventoryStatus
import com.appvexis.peptidetracker.core.model.TitrationStep
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Custom Room TypeConverters for collections and complex models.
 */
class DatabaseConverters {

    @TypeConverter
    fun fromIntList(value: List<Int>?): String? {
        return value?.let { Json.encodeToString(it) }
    }

    @TypeConverter
    fun toIntList(value: String?): List<Int>? {
        return value?.let { encoded ->
            runCatching { Json.decodeFromString<List<Int>>(encoded) }.getOrNull()
        }
    }

    @TypeConverter
    fun fromTitrationStepList(value: List<TitrationStep>?): String? {
        return value?.let { Json.encodeToString(it) }
    }

    @TypeConverter
    fun toTitrationStepList(value: String?): List<TitrationStep>? {
        return value?.let { encoded ->
            runCatching { Json.decodeFromString<List<TitrationStep>>(encoded) }.getOrNull()
        }
    }

    @TypeConverter
    fun fromStringDoubleMap(value: Map<String, Double>?): String? {
        return value?.let { Json.encodeToString(it) }
    }

    @TypeConverter
    fun toStringDoubleMap(value: String?): Map<String, Double>? {
        return value?.let { encoded ->
            runCatching { Json.decodeFromString<Map<String, Double>>(encoded) }.getOrNull()
        }
    }

    // Room's default enum adapter calls Enum.valueOf directly. A damaged or
    // older row should not take the entire app down, so persisted enums use
    // conservative fallbacks that never turn an unknown dose into TAKEN.
    @TypeConverter
    fun fromDoseUnit(value: DoseUnit): String = value.name

    @TypeConverter
    fun toDoseUnit(value: String?): DoseUnit = value.toEnumOrDefault(DoseUnit.IU)

    @TypeConverter
    fun fromFrequencyType(value: FrequencyType): String = value.name

    @TypeConverter
    fun toFrequencyType(value: String?): FrequencyType = value.toEnumOrDefault(FrequencyType.DAILY)

    @TypeConverter
    fun fromAdminRoute(value: AdminRoute): String = value.name

    @TypeConverter
    fun toAdminRoute(value: String?): AdminRoute = value.toEnumOrDefault(AdminRoute.SUBQ)

    @TypeConverter
    fun fromDoseStatus(value: DoseStatus): String = value.name

    @TypeConverter
    fun toDoseStatus(value: String?): DoseStatus = value.toEnumOrDefault(DoseStatus.SKIPPED)

    @TypeConverter
    fun fromInventoryStatus(value: InventoryStatus): String = value.name

    @TypeConverter
    fun toInventoryStatus(value: String?): InventoryStatus = value.toEnumOrDefault(InventoryStatus.EMPTY)

    private inline fun <reified T : Enum<T>> String?.toEnumOrDefault(default: T): T =
        this?.let { encoded ->
            enumValues<T>().firstOrNull { it.name.equals(encoded.trim(), ignoreCase = true) }
        } ?: default
}
