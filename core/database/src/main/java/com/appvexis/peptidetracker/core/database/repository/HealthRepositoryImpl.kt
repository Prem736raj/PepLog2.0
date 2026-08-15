package com.appvexis.peptidetracker.core.database.repository

import com.appvexis.peptidetracker.core.database.dao.HealthMetricDao
import com.appvexis.peptidetracker.core.database.entity.HealthMetricEntity
import com.appvexis.peptidetracker.core.model.HealthMetricRecord
import com.appvexis.peptidetracker.core.model.HealthMetricType
import com.appvexis.peptidetracker.core.model.repository.HealthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository implementation mapping between HealthMetricEntity (Room) and HealthMetricRecord (domain).
 */
@Singleton
class HealthRepositoryImpl @Inject constructor(
    private val healthMetricDao: HealthMetricDao
) : HealthRepository {

    override fun getAllHealthMetrics(): Flow<List<HealthMetricRecord>> =
        healthMetricDao.getAllHealthMetrics().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getHealthMetricsByType(type: HealthMetricType): Flow<List<HealthMetricRecord>> =
        healthMetricDao.getHealthMetricsByType(type.name).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getHealthMetricsInRange(
        startTime: Long,
        endTime: Long
    ): Flow<List<HealthMetricRecord>> =
        healthMetricDao.getHealthMetricsInRange(startTime, endTime).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getHealthMetricsByTypeInRange(
        type: HealthMetricType,
        startTime: Long,
        endTime: Long
    ): Flow<List<HealthMetricRecord>> =
        healthMetricDao.getHealthMetricsByTypeInRange(type.name, startTime, endTime).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getLatestMetricByType(type: HealthMetricType): HealthMetricRecord? =
        healthMetricDao.getLatestMetricByType(type.name)?.toDomain()

    override suspend fun insertHealthMetric(record: HealthMetricRecord) {
        healthMetricDao.insertHealthMetric(record.toEntity())
    }

    override suspend fun insertHealthMetrics(records: List<HealthMetricRecord>) {
        healthMetricDao.insertHealthMetrics(records.map { it.toEntity() })
    }

    override suspend fun deleteHealthMetric(id: String) {
        healthMetricDao.deleteHealthMetric(id)
    }

    override suspend fun getLastSyncTimestamp(type: HealthMetricType): Long? =
        healthMetricDao.getLastSyncTimestamp(type.name)

    // --- Mapping Functions ---

    private fun HealthMetricEntity.toDomain(): HealthMetricRecord = HealthMetricRecord(
        id = id,
        metricType = try { HealthMetricType.valueOf(metricType) } catch (_: Exception) { HealthMetricType.WEIGHT },
        value = value,
        secondaryValue = secondaryValue,
        timestamp = timestamp,
        source = source,
        protocolId = protocolId
    )

    private fun HealthMetricRecord.toEntity(): HealthMetricEntity = HealthMetricEntity(
        id = id,
        metricType = metricType.name,
        value = value,
        secondaryValue = secondaryValue,
        timestamp = timestamp,
        source = source,
        protocolId = protocolId
    )
}
