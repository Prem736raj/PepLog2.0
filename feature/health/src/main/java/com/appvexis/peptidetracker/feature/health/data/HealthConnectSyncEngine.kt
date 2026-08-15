package com.appvexis.peptidetracker.feature.health.data

import com.appvexis.peptidetracker.core.model.HealthMetricType
import com.appvexis.peptidetracker.core.model.repository.HealthRepository
import timber.log.Timber
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Engine responsible for syncing Health Connect data into Room.
 * Implements incremental sync — only fetches data newer than the last sync timestamp.
 */
@Singleton
class HealthConnectSyncEngine @Inject constructor(
    private val healthConnectManager: HealthConnectManager,
    private val healthRepository: HealthRepository
) {
    companion object {
        /** Default lookback window for initial sync (30 days). */
        private const val DEFAULT_LOOKBACK_DAYS = 30L
    }

    /**
     * Perform an incremental sync for all metric types.
     * @return The number of new records synced.
     */
    suspend fun syncAll(): Int {
        if (!healthConnectManager.isAvailable()) {
            Timber.w("Health Connect not available, skipping sync")
            return 0
        }

        if (!healthConnectManager.hasAllPermissions()) {
            Timber.w("Health Connect permissions not granted, skipping sync")
            return 0
        }

        var totalSynced = 0
        val now = Instant.now()

        for (metricType in HealthMetricType.entries) {
            try {
                totalSynced += syncMetricType(metricType, now)
            } catch (e: Exception) {
                Timber.e(e, "Error syncing metric type: ${metricType.name}")
            }
        }

        Timber.i("Health Connect sync completed: $totalSynced new records")
        return totalSynced
    }

    /**
     * Sync a single metric type from Health Connect into Room.
     */
    private suspend fun syncMetricType(metricType: HealthMetricType, now: Instant): Int {
        val lastSync = healthRepository.getLastSyncTimestamp(metricType)
        val startTime = if (lastSync != null) {
            Instant.ofEpochMilli(lastSync)
        } else {
            // Initial sync: look back DEFAULT_LOOKBACK_DAYS
            now.minus(DEFAULT_LOOKBACK_DAYS, ChronoUnit.DAYS)
        }

        val records = when (metricType) {
            HealthMetricType.WEIGHT -> healthConnectManager.readWeightRecords(startTime, now)
            HealthMetricType.SLEEP_DURATION -> healthConnectManager.readSleepRecords(startTime, now)
            HealthMetricType.HEART_RATE -> healthConnectManager.readHeartRateRecords(startTime, now)
            HealthMetricType.BLOOD_PRESSURE_SYSTOLIC -> healthConnectManager.readBloodPressureRecords(startTime, now)
                .filter { it.metricType == HealthMetricType.BLOOD_PRESSURE_SYSTOLIC }
            HealthMetricType.BLOOD_PRESSURE_DIASTOLIC -> healthConnectManager.readBloodPressureRecords(startTime, now)
                .filter { it.metricType == HealthMetricType.BLOOD_PRESSURE_DIASTOLIC }
            HealthMetricType.STEPS -> healthConnectManager.readStepsRecords(startTime, now)
            HealthMetricType.BODY_FAT -> healthConnectManager.readBodyFatRecords(startTime, now)
            HealthMetricType.RESTING_HEART_RATE -> healthConnectManager.readRestingHeartRateRecords(startTime, now)
        }

        if (records.isNotEmpty()) {
            healthRepository.insertHealthMetrics(records)
            Timber.d("Synced ${records.size} ${metricType.displayName} records")
        }

        return records.size
    }
}
