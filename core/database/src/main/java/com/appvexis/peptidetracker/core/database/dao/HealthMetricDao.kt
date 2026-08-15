package com.appvexis.peptidetracker.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.appvexis.peptidetracker.core.database.entity.HealthMetricEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data access object for health metric records synced from Health Connect.
 */
@Dao
interface HealthMetricDao {

    @Query("SELECT * FROM health_metric ORDER BY timestamp DESC")
    fun getAllHealthMetrics(): Flow<List<HealthMetricEntity>>

    @Query("SELECT * FROM health_metric WHERE metric_type = :metricType ORDER BY timestamp DESC")
    fun getHealthMetricsByType(metricType: String): Flow<List<HealthMetricEntity>>

    @Query("SELECT * FROM health_metric WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp ASC")
    fun getHealthMetricsInRange(startTime: Long, endTime: Long): Flow<List<HealthMetricEntity>>

    @Query("SELECT * FROM health_metric WHERE metric_type = :metricType AND timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp ASC")
    fun getHealthMetricsByTypeInRange(metricType: String, startTime: Long, endTime: Long): Flow<List<HealthMetricEntity>>

    @Query("SELECT * FROM health_metric WHERE metric_type = :metricType ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestMetricByType(metricType: String): HealthMetricEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthMetric(record: HealthMetricEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthMetrics(records: List<HealthMetricEntity>)

    @Query("DELETE FROM health_metric WHERE id = :id")
    suspend fun deleteHealthMetric(id: String)

    @Query("SELECT MAX(synced_at) FROM health_metric WHERE metric_type = :metricType")
    suspend fun getLastSyncTimestamp(metricType: String): Long?

    @Query("SELECT COUNT(*) FROM health_metric WHERE metric_type = :metricType")
    suspend fun getCountByType(metricType: String): Int

    // Sync query for backup export
    @Query("SELECT * FROM health_metric ORDER BY timestamp DESC")
    suspend fun getAllMetricsSync(): List<HealthMetricEntity>
}
