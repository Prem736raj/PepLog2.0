package com.appvexis.peptidetracker.core.analytics

import com.appvexis.peptidetracker.core.model.DailyAnalyticsSummary
import com.appvexis.peptidetracker.core.model.Protocol
import com.appvexis.peptidetracker.core.model.ProtocolAnalyticsSummary
import com.appvexis.peptidetracker.core.model.repository.AnalyticsRepository
import com.appvexis.peptidetracker.core.model.repository.ProtocolRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Domain-level engine that manages precomputation and comparison of analytics.
 */
@Singleton
class AnalyticsEngine @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
    private val protocolRepository: ProtocolRepository
) {
    /**
     * Observes daily summaries for a specific protocol stack.
     */
    fun getDailySummaries(protocolId: String): Flow<List<DailyAnalyticsSummary>> {
        return analyticsRepository.getDailySummaries(protocolId)
    }

    /**
     * Observes daily summaries across all protocols.
     */
    fun getAllDailySummaries(): Flow<List<DailyAnalyticsSummary>> {
        return analyticsRepository.getAllDailySummaries()
    }

    /**
     * Observes overall cycle summary for a specific protocol.
     */
    fun getProtocolSummary(protocolId: String): Flow<ProtocolAnalyticsSummary?> {
        return analyticsRepository.getProtocolSummary(protocolId)
    }

    /**
     * Observes all protocol summaries.
     */
    fun getAllProtocolSummaries(): Flow<List<ProtocolAnalyticsSummary>> {
        return analyticsRepository.getAllProtocolSummaries()
    }

    /**
     * Force recomputation of analytics for a given protocol.
     */
    suspend fun recomputeProtocol(protocolId: String) {
        try {
            analyticsRepository.recomputeAnalyticsForProtocol(protocolId)
            Timber.d("Successfully recomputed analytics for protocol $protocolId")
        } catch (e: Exception) {
            Timber.e(e, "Error recomputing analytics for protocol $protocolId")
        }
    }

    /**
     * Force full recomputation for all protocols in the database.
     */
    suspend fun recomputeAll() {
        try {
            analyticsRepository.recomputeAllAnalytics()
            Timber.d("Successfully recomputed all protocol analytics")
        } catch (e: Exception) {
            Timber.e(e, "Error during full analytics recomputation")
        }
    }
}
