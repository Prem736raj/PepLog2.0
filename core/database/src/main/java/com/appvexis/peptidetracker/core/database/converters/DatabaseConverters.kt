package com.appvexis.peptidetracker.core.database.converters

import androidx.room.TypeConverter
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
        return value?.let { Json.decodeFromString<List<Int>>(it) }
    }

    @TypeConverter
    fun fromTitrationStepList(value: List<TitrationStep>?): String? {
        return value?.let { Json.encodeToString(it) }
    }

    @TypeConverter
    fun toTitrationStepList(value: String?): List<TitrationStep>? {
        return value?.let { Json.decodeFromString<List<TitrationStep>>(it) }
    }

    @TypeConverter
    fun fromStringDoubleMap(value: Map<String, Double>?): String? {
        return value?.let { Json.encodeToString(it) }
    }

    @TypeConverter
    fun toStringDoubleMap(value: String?): Map<String, Double>? {
        return value?.let { Json.decodeFromString<Map<String, Double>>(it) }
    }
}
