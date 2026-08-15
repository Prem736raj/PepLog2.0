package com.appvexis.peptidetracker.feature.health.data

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.appvexis.peptidetracker.core.model.HealthMetricRecord
import com.appvexis.peptidetracker.core.model.HealthMetricType
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central manager for Health Connect integration.
 * Handles availability checks, permission definitions, and data reading from Health Connect.
 */
@Singleton
class HealthConnectManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Required Health Connect permissions for this app.
     */
    val requiredPermissions: Set<String> = setOf(
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(BloodPressureRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(BodyFatRecord::class),
        HealthPermission.getReadPermission(RestingHeartRateRecord::class)
    )

    /**
     * Check if Health Connect is available on this device.
     */
    fun isAvailable(): Boolean {
        val status = HealthConnectClient.getSdkStatus(context)
        return status == HealthConnectClient.SDK_AVAILABLE
    }

    /**
     * Check if Health Connect needs to be installed.
     */
    fun needsInstall(): Boolean {
        val status = HealthConnectClient.getSdkStatus(context)
        return status == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED
    }

    /**
     * Get the Health Connect client instance. Call only when [isAvailable] returns true.
     */
    private fun getClient(): HealthConnectClient = HealthConnectClient.getOrCreate(context)

    /**
     * Check which permissions are currently granted.
     */
    suspend fun getGrantedPermissions(): Set<String> {
        return try {
            if (!isAvailable()) return emptySet()
            getClient().permissionController.getGrantedPermissions()
        } catch (e: Exception) {
            Timber.e(e, "Failed to get granted Health Connect permissions")
            emptySet()
        }
    }

    /**
     * Check if all required permissions are granted.
     */
    suspend fun hasAllPermissions(): Boolean {
        val granted = getGrantedPermissions()
        return requiredPermissions.all { it in granted }
    }

    // ========= Data Reading Functions =========

    /**
     * Read weight records from Health Connect within the given time range.
     */
    suspend fun readWeightRecords(start: Instant, end: Instant): List<HealthMetricRecord> {
        if (!isAvailable()) return emptyList()
        return try {
            val response = getClient().readRecords(
                ReadRecordsRequest(
                    recordType = WeightRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )
            response.records.map { record ->
                HealthMetricRecord(
                    id = record.metadata.id ?: UUID.randomUUID().toString(),
                    metricType = HealthMetricType.WEIGHT,
                    value = record.weight.inKilograms,
                    timestamp = record.time.toEpochMilli(),
                    source = record.metadata.dataOrigin.packageName
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to read weight records")
            emptyList()
        }
    }

    /**
     * Read sleep session records and convert to total hours.
     */
    suspend fun readSleepRecords(start: Instant, end: Instant): List<HealthMetricRecord> {
        if (!isAvailable()) return emptyList()
        return try {
            val response = getClient().readRecords(
                ReadRecordsRequest(
                    recordType = SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )
            response.records.map { record ->
                val durationHours = java.time.Duration.between(
                    record.startTime,
                    record.endTime
                ).toMinutes() / 60.0
                HealthMetricRecord(
                    id = record.metadata.id ?: UUID.randomUUID().toString(),
                    metricType = HealthMetricType.SLEEP_DURATION,
                    value = durationHours,
                    timestamp = record.startTime.toEpochMilli(),
                    source = record.metadata.dataOrigin.packageName
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to read sleep records")
            emptyList()
        }
    }

    /**
     * Read heart rate records, averaging samples within each record.
     */
    suspend fun readHeartRateRecords(start: Instant, end: Instant): List<HealthMetricRecord> {
        if (!isAvailable()) return emptyList()
        return try {
            val response = getClient().readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )
            response.records.flatMap { record ->
                record.samples.map { sample ->
                    HealthMetricRecord(
                        id = "${record.metadata.id}_${sample.time.toEpochMilli()}",
                        metricType = HealthMetricType.HEART_RATE,
                        value = sample.beatsPerMinute.toDouble(),
                        timestamp = sample.time.toEpochMilli(),
                        source = record.metadata.dataOrigin.packageName
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to read heart rate records")
            emptyList()
        }
    }

    /**
     * Read blood pressure records, storing systolic as primary and diastolic as secondary.
     */
    suspend fun readBloodPressureRecords(start: Instant, end: Instant): List<HealthMetricRecord> {
        if (!isAvailable()) return emptyList()
        return try {
            val response = getClient().readRecords(
                ReadRecordsRequest(
                    recordType = BloodPressureRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )
            response.records.flatMap { record ->
                listOf(
                    HealthMetricRecord(
                        id = "${record.metadata.id}_systolic",
                        metricType = HealthMetricType.BLOOD_PRESSURE_SYSTOLIC,
                        value = record.systolic.inMillimetersOfMercury,
                        secondaryValue = record.diastolic.inMillimetersOfMercury,
                        timestamp = record.time.toEpochMilli(),
                        source = record.metadata.dataOrigin.packageName
                    ),
                    HealthMetricRecord(
                        id = "${record.metadata.id}_diastolic",
                        metricType = HealthMetricType.BLOOD_PRESSURE_DIASTOLIC,
                        value = record.diastolic.inMillimetersOfMercury,
                        secondaryValue = record.systolic.inMillimetersOfMercury,
                        timestamp = record.time.toEpochMilli(),
                        source = record.metadata.dataOrigin.packageName
                    )
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to read blood pressure records")
            emptyList()
        }
    }

    /**
     * Read step count records.
     */
    suspend fun readStepsRecords(start: Instant, end: Instant): List<HealthMetricRecord> {
        if (!isAvailable()) return emptyList()
        return try {
            val response = getClient().readRecords(
                ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )
            response.records.map { record ->
                HealthMetricRecord(
                    id = record.metadata.id ?: UUID.randomUUID().toString(),
                    metricType = HealthMetricType.STEPS,
                    value = record.count.toDouble(),
                    timestamp = record.startTime.toEpochMilli(),
                    source = record.metadata.dataOrigin.packageName
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to read steps records")
            emptyList()
        }
    }

    /**
     * Read body fat percentage records.
     */
    suspend fun readBodyFatRecords(start: Instant, end: Instant): List<HealthMetricRecord> {
        if (!isAvailable()) return emptyList()
        return try {
            val response = getClient().readRecords(
                ReadRecordsRequest(
                    recordType = BodyFatRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )
            response.records.map { record ->
                HealthMetricRecord(
                    id = record.metadata.id ?: UUID.randomUUID().toString(),
                    metricType = HealthMetricType.BODY_FAT,
                    value = record.percentage.value,
                    timestamp = record.time.toEpochMilli(),
                    source = record.metadata.dataOrigin.packageName
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to read body fat records")
            emptyList()
        }
    }

    /**
     * Read resting heart rate records.
     */
    suspend fun readRestingHeartRateRecords(start: Instant, end: Instant): List<HealthMetricRecord> {
        if (!isAvailable()) return emptyList()
        return try {
            val response = getClient().readRecords(
                ReadRecordsRequest(
                    recordType = RestingHeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )
            response.records.map { record ->
                HealthMetricRecord(
                    id = record.metadata.id ?: UUID.randomUUID().toString(),
                    metricType = HealthMetricType.RESTING_HEART_RATE,
                    value = record.beatsPerMinute.toDouble(),
                    timestamp = record.time.toEpochMilli(),
                    source = record.metadata.dataOrigin.packageName
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to read resting heart rate records")
            emptyList()
        }
    }

    /**
     * Read all metric types and return a unified list.
     */
    suspend fun readAllMetrics(start: Instant, end: Instant): List<HealthMetricRecord> {
        val results = mutableListOf<HealthMetricRecord>()
        results.addAll(readWeightRecords(start, end))
        results.addAll(readSleepRecords(start, end))
        results.addAll(readHeartRateRecords(start, end))
        results.addAll(readBloodPressureRecords(start, end))
        results.addAll(readStepsRecords(start, end))
        results.addAll(readBodyFatRecords(start, end))
        results.addAll(readRestingHeartRateRecords(start, end))
        return results
    }
}
