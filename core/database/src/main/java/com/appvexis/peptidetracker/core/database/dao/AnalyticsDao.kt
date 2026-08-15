package com.appvexis.peptidetracker.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.appvexis.peptidetracker.core.database.entity.DailyAnalyticsSummaryEntity
import com.appvexis.peptidetracker.core.database.entity.ProtocolAnalyticsSummaryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Database access object for aggregated analytics cache.
 */
@Dao
interface AnalyticsDao {
    @Query("SELECT * FROM analytics_daily_summary WHERE protocol_id = :protocolId ORDER BY date ASC")
    fun getDailySummaries(protocolId: String): Flow<List<DailyAnalyticsSummaryEntity>>

    @Query("SELECT * FROM analytics_daily_summary ORDER BY date ASC")
    fun getAllDailySummaries(): Flow<List<DailyAnalyticsSummaryEntity>>

    @Query("SELECT * FROM analytics_protocol_summary WHERE protocol_id = :protocolId LIMIT 1")
    fun getProtocolSummary(protocolId: String): Flow<ProtocolAnalyticsSummaryEntity?>

    @Query("SELECT * FROM analytics_protocol_summary ORDER BY updated_at DESC")
    fun getAllProtocolSummaries(): Flow<List<ProtocolAnalyticsSummaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailySummary(summary: DailyAnalyticsSummaryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProtocolSummary(summary: ProtocolAnalyticsSummaryEntity)

    @Query("DELETE FROM analytics_daily_summary WHERE protocol_id = :protocolId")
    suspend fun deleteDailySummariesForProtocol(protocolId: String)

    @Query("DELETE FROM analytics_protocol_summary WHERE protocol_id = :protocolId")
    suspend fun deleteProtocolSummaryForProtocol(protocolId: String)
}

