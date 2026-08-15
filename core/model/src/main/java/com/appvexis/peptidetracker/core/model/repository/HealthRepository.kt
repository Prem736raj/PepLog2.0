package com.appvexis.peptidetracker.core.model.repository

import com.appvexis.peptidetracker.core.model.HealthMetricRecord
import com.appvexis.peptidetracker.core.model.HealthMetricType
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for health metric data synced from Health Connect.
 */
interface HealthRepository {
    /**
     * Observe all health metric records, ordered by timestamp descending.
     */
    fun getAllHealthMetrics(): Flow<List<HealthMetricRecord>>

    /**
     * Observe health metric records filtered by type.
     */
    fun getHealthMetricsByType(type: HealthMetricType): Flow<List<HealthMetricRecord>>

    /**
     * Observe health metric records within a date range.
     */
    fun getHealthMetricsInRange(startTime: Long, endTime: Long): Flow<List<HealthMetricRecord>>

    /**
     * Observe health metric records of a specific type within a date range.
     */
    fun getHealthMetricsByTypeInRange(
        type: HealthMetricType,
        startTime: Long,
        endTime: Long
    ): Flow<List<HealthMetricRecord>>

    /**
     * Get the most recent metric of each type for dashboard summaries.
     */
    suspend fun getLatestMetricByType(type: HealthMetricType): HealthMetricRecord?

    /**
     * Insert a single health metric record.
     */
    suspend fun insertHealthMetric(record: HealthMetricRecord)

    /**
     * Insert multiple health metric records (batch sync from Health Connect).
     */
    suspend fun insertHealthMetrics(records: List<HealthMetricRecord>)

    /**
     * Delete a health metric record.
     */
    suspend fun deleteHealthMetric(id: String)

    /**
     * Get timestamp of the last sync for a given metric type.
     */
    suspend fun getLastSyncTimestamp(type: HealthMetricType): Long?
}
