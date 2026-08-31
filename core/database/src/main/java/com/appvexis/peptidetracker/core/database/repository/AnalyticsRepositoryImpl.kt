package com.appvexis.peptidetracker.core.database.repository

import com.appvexis.peptidetracker.core.database.dao.AnalyticsDao
import com.appvexis.peptidetracker.core.database.dao.LogDao
import com.appvexis.peptidetracker.core.database.dao.ProtocolDao
import com.appvexis.peptidetracker.core.database.entity.DailyAnalyticsSummaryEntity
import com.appvexis.peptidetracker.core.database.entity.ProtocolAnalyticsSummaryEntity
import com.appvexis.peptidetracker.core.database.entity.toDomain
import com.appvexis.peptidetracker.core.database.entity.toEntity
import com.appvexis.peptidetracker.core.model.DailyAnalyticsSummary
import com.appvexis.peptidetracker.core.model.DoseStatus
import com.appvexis.peptidetracker.core.model.ProtocolAnalyticsSummary
import com.appvexis.peptidetracker.core.model.repository.AnalyticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Database implementation of [AnalyticsRepository] mapping precomputed summaries to Domain.
 * Orchestrates full recomputations of daily averages and stack aggregates on data changes.
 */
@Singleton
class AnalyticsRepositoryImpl @Inject constructor(
    private val analyticsDao: AnalyticsDao,
    private val logDao: LogDao,
    private val protocolDao: ProtocolDao
) : AnalyticsRepository {

    override fun getDailySummaries(protocolId: String): Flow<List<DailyAnalyticsSummary>> {
        return analyticsDao.getDailySummaries(protocolId).map { list -> list.map { it.toDomain() } }
    }

    override fun getAllDailySummaries(): Flow<List<DailyAnalyticsSummary>> {
        return analyticsDao.getAllDailySummaries().map { list -> list.map { it.toDomain() } }
    }

    override fun getProtocolSummary(protocolId: String): Flow<ProtocolAnalyticsSummary?> {
        return analyticsDao.getProtocolSummary(protocolId).map { it?.toDomain() }
    }

    override fun getAllProtocolSummaries(): Flow<List<ProtocolAnalyticsSummary>> {
        return analyticsDao.getAllProtocolSummaries().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun updateDailySummary(summary: DailyAnalyticsSummary) {
        analyticsDao.insertDailySummary(summary.toEntity())
    }

    override suspend fun updateProtocolSummary(summary: ProtocolAnalyticsSummary) {
        analyticsDao.insertProtocolSummary(summary.toEntity())
    }

    override suspend fun recomputeAllAnalytics() {
        val protocols = protocolDao.getAllProtocols().first()
        for (protocol in protocols) {
            recomputeAnalyticsForProtocol(protocol.id)
        }
    }

    override suspend fun recomputeAnalyticsForProtocol(protocolId: String) {
        // 1. Fetch protocol to ensure it exists
        val protocolWithCompounds = protocolDao.getProtocolById(protocolId).first() ?: return

        // 2. Fetch logs for calculation
        val doseLogs = logDao.getDoseLogsForProtocol(protocolId).first()
        val analysisNow = System.currentTimeMillis()
        // Scheduled rows are generated ahead of time. Future rows are not adherence
        // opportunities yet and should not create empty future report days.
        val doseLogsForAnalytics = doseLogs.filter { it.scheduledTime <= analysisNow }
        val sideEffectLogs = logDao.getSideEffectLogsForProtocol(protocolId).first()
        val biomarkerLogs = logDao.getBiomarkerLogsForProtocol(protocolId).first()
        val siteLogs = logDao.getInjectionSiteLogs().first()

        // Cache compound to protocol mapping to filter site logs
        val compounds = protocolWithCompounds.compounds
        val compoundIds = compounds.map { it.id }.toSet()
        val doseIdToCompoundId = doseLogs.associate { it.id to it.protocolCompoundId }
        val siteLogsForProtocol = siteLogs.filter { siteLog ->
            siteLog.doseLogId?.let { doseIdToCompoundId[it] in compoundIds } ?: false
        }

        // 3. Find all unique local dates represented by logs
        val zoneId = ZoneId.systemDefault()
        val allDays = (doseLogsForAnalytics.map { Instant.ofEpochMilli(it.scheduledTime).atZone(zoneId).toLocalDate() } +
                sideEffectLogs.map { Instant.ofEpochMilli(it.date).atZone(zoneId).toLocalDate() } +
                biomarkerLogs.map { Instant.ofEpochMilli(it.date).atZone(zoneId).toLocalDate() } +
                siteLogsForProtocol.map { Instant.ofEpochMilli(it.timestamp).atZone(zoneId).toLocalDate() }).toSet()

        // Clean existing summaries first to avoid stale entries
        analyticsDao.deleteDailySummariesForProtocol(protocolId)
        analyticsDao.deleteProtocolSummaryForProtocol(protocolId)

        if (allDays.isEmpty()) return

        val dailySummaries = mutableListOf<DailyAnalyticsSummaryEntity>()

        // 4. Calculate Daily summaries
        for (day in allDays.sorted()) {
            val dayStartMillis = day.atStartOfDay(zoneId).toInstant().toEpochMilli()

            val dosesOnDay = doseLogsForAnalytics.filter { Instant.ofEpochMilli(it.scheduledTime).atZone(zoneId).toLocalDate() == day }
            val sideEffectsOnDay = sideEffectLogs.filter { Instant.ofEpochMilli(it.date).atZone(zoneId).toLocalDate() == day }
            val biomarkersOnDay = biomarkerLogs.filter { Instant.ofEpochMilli(it.date).atZone(zoneId).toLocalDate() == day }
            val sitesOnDay = siteLogsForProtocol.filter { Instant.ofEpochMilli(it.timestamp).atZone(zoneId).toLocalDate() == day }

            val totalDosesScheduled = dosesOnDay.count { it.status != DoseStatus.SKIPPED }
            val totalDosesTaken = dosesOnDay.count { it.status == DoseStatus.TAKEN }
            val totalDosesMissed = dosesOnDay.count {
                it.status == DoseStatus.MISSED ||
                    (it.status == DoseStatus.PENDING && it.scheduledTime < analysisNow)
            }
            val adherencePercentage = if (totalDosesScheduled > 0) {
                (totalDosesTaken.toDouble() / totalDosesScheduled.toDouble()) * 100.0
            } else {
                0.0
            }

            val moodLogs = sideEffectsOnDay.filter { it.type == "mood" }
            val avgMood = if (moodLogs.isNotEmpty()) moodLogs.map { it.severity.toDouble() }.average() else null

            val energyLogs = sideEffectsOnDay.filter { it.type == "energy" }
            val avgEnergy = if (energyLogs.isNotEmpty()) energyLogs.map { it.severity.toDouble() }.average() else null

            val sleepLogs = sideEffectsOnDay.filter { it.type == "sleep" }
            val avgSleepQuality = if (sleepLogs.isNotEmpty()) sleepLogs.map { it.severity.toDouble() }.average() else null

            val painLogs = sideEffectsOnDay.filter { it.type == "pain" }
            val painLevels = painLogs.map { it.severity.toDouble() } + sitesOnDay.mapNotNull { it.painLevel?.toDouble() }
            val avgPainLevel = if (painLevels.isNotEmpty()) painLevels.average() else null

            val sideEffectCount = sideEffectsOnDay.count { it.type == "side_effect" }

            val weightLog = biomarkersOnDay
                .filter {
                    it.biomarkerName.equals("weight", ignoreCase = true) ||
                        it.biomarkerName.equals("BODY_WEIGHT", ignoreCase = true)
                }
                .maxByOrNull { it.date }
            val weight = weightLog?.value

            val dailyEntity = DailyAnalyticsSummaryEntity(
                id = "${protocolId}_$dayStartMillis",
                date = dayStartMillis,
                protocolId = protocolId,
                totalDosesScheduled = totalDosesScheduled,
                totalDosesTaken = totalDosesTaken,
                totalDosesMissed = totalDosesMissed,
                adherencePercentage = adherencePercentage,
                avgMood = avgMood,
                avgEnergy = avgEnergy,
                avgSleepQuality = avgSleepQuality,
                avgPainLevel = avgPainLevel,
                sideEffectCount = sideEffectCount,
                weight = weight,
                updatedAt = System.currentTimeMillis()
            )
            dailySummaries.add(dailyEntity)
            analyticsDao.insertDailySummary(dailyEntity)
        }

        // 5. Calculate Protocol-level summary
        val startDateVal = protocolWithCompounds.protocol.startDate
        val endDateVal = protocolWithCompounds.protocol.endDate ?: System.currentTimeMillis()
        val totalDays = if (startDateVal != null) {
            val startLocalDate = Instant.ofEpochMilli(startDateVal).atZone(zoneId).toLocalDate()
            val endLocalDate = Instant.ofEpochMilli(endDateVal).atZone(zoneId).toLocalDate()
            val days = java.time.temporal.ChronoUnit.DAYS.between(startLocalDate, endLocalDate) + 1
            maxOf(1, days.toInt())
        } else {
            maxOf(1, allDays.size)
        }

        val totalDosesScheduled = dailySummaries.sumOf { it.totalDosesScheduled }
        val totalDosesTaken = dailySummaries.sumOf { it.totalDosesTaken }
        val overallAdherence = if (totalDosesScheduled > 0) {
            (totalDosesTaken.toDouble() / totalDosesScheduled.toDouble()) * 100.0
        } else {
            0.0
        }

        val sideEffectsOnly = sideEffectLogs.filter { it.type == "side_effect" && it.category != null }
        val mostCommonSideEffect = sideEffectsOnly
            .groupBy { it.category }
            .maxByOrNull { it.value.size }?.key

        val avgSideEffectSeverity = if (sideEffectsOnly.isNotEmpty()) {
            sideEffectsOnly.map { it.severity.toDouble() }.average()
        } else {
            null
        }

        val biomarkerChanges = biomarkerLogs
            .groupBy { it.biomarkerName }
            .mapValues { (_, logs) ->
                val sorted = logs.sortedBy { it.date }
                val earliest = sorted.firstOrNull()?.value ?: 0.0
                val latest = sorted.lastOrNull()?.value ?: 0.0
                latest - earliest
            }

        val subjectiveTypes = listOf("mood", "energy", "sleep", "pain", "libido")
        val subjectiveTrends = mutableMapOf<String, Double>()
        for (type in subjectiveTypes) {
            val logsOfType = sideEffectLogs.filter { it.type == type }.sortedBy { it.date }
            if (logsOfType.isNotEmpty()) {
                val minDate = logsOfType.first().date
                val maxDate = logsOfType.last().date
                val oneWeekMs = 7 * 24 * 60 * 60 * 1000L

                val firstWeekLogs = logsOfType.filter { it.date <= minDate + oneWeekMs }
                val lastWeekLogs = logsOfType.filter { it.date >= maxDate - oneWeekMs }

                val firstWeekAvg = if (firstWeekLogs.isNotEmpty()) {
                    firstWeekLogs.map { it.severity.toDouble() }.average()
                } else {
                    logsOfType.first().severity.toDouble()
                }

                val lastWeekAvg = if (lastWeekLogs.isNotEmpty()) {
                    lastWeekLogs.map { it.severity.toDouble() }.average()
                } else {
                    logsOfType.last().severity.toDouble()
                }

                subjectiveTrends[type] = lastWeekAvg - firstWeekAvg
            }
        }

        val protocolSummary = ProtocolAnalyticsSummaryEntity(
            id = protocolId,
            protocolId = protocolId,
            totalDays = totalDays,
            totalDosesScheduled = totalDosesScheduled,
            totalDosesTaken = totalDosesTaken,
            overallAdherence = overallAdherence,
            mostCommonSideEffect = mostCommonSideEffect,
            avgSideEffectSeverity = avgSideEffectSeverity,
            biomarkerChanges = biomarkerChanges,
            subjectiveTrends = subjectiveTrends,
            updatedAt = System.currentTimeMillis()
        )
        analyticsDao.insertProtocolSummary(protocolSummary)
    }
}
