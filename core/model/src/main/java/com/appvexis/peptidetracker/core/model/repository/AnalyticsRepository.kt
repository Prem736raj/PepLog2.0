package com.appvexis.peptidetracker.core.model.repository

import com.appvexis.peptidetracker.core.model.DailyAnalyticsSummary
import com.appvexis.peptidetracker.core.model.ProtocolAnalyticsSummary
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for retrieving precomputed metrics dashboards.
 */
interface AnalyticsRepository {
    fun getDailySummaries(protocolId: String): Flow<List<DailyAnalyticsSummary>>
    fun getAllDailySummaries(): Flow<List<DailyAnalyticsSummary>>
    fun getProtocolSummary(protocolId: String): Flow<ProtocolAnalyticsSummary?>
    fun getAllProtocolSummaries(): Flow<List<ProtocolAnalyticsSummary>>
    suspend fun updateDailySummary(summary: DailyAnalyticsSummary)
    suspend fun updateProtocolSummary(summary: ProtocolAnalyticsSummary)
    suspend fun recomputeAnalyticsForProtocol(protocolId: String)
    suspend fun recomputeAllAnalytics()
}

