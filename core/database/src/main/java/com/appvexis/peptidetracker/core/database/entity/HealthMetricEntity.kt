package com.appvexis.peptidetracker.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity storing individual health metric readings synced from Health Connect.
 * Indexed on metric_type + timestamp for efficient range queries and deduplication.
 */
@Entity(
    tableName = "health_metric",
    indices = [
        Index(value = ["metric_type", "timestamp"], unique = true),
        Index(value = ["metric_type"]),
        Index(value = ["timestamp"])
    ]
)
data class HealthMetricEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "metric_type")
    val metricType: String,

    @ColumnInfo(name = "value")
    val value: Double,

    @ColumnInfo(name = "secondary_value")
    val secondaryValue: Double? = null,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "source")
    val source: String = "Health Connect",

    @ColumnInfo(name = "protocol_id")
    val protocolId: String? = null,

    @ColumnInfo(name = "synced_at")
    val syncedAt: Long = System.currentTimeMillis()
)
