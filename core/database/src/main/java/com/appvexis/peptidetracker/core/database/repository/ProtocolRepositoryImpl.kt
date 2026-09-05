package com.appvexis.peptidetracker.core.database.repository

import androidx.room.withTransaction
import com.appvexis.peptidetracker.core.database.PepLogDatabase
import com.appvexis.peptidetracker.core.database.dao.ProtocolDao
import com.appvexis.peptidetracker.core.database.entity.toDomain
import com.appvexis.peptidetracker.core.database.entity.toEntity
import com.appvexis.peptidetracker.core.model.Protocol
import com.appvexis.peptidetracker.core.model.ProtocolCompound
import com.appvexis.peptidetracker.core.model.ProtocolWithCompounds
import com.appvexis.peptidetracker.core.model.DoseLog
import com.appvexis.peptidetracker.core.model.DoseStatus
import com.appvexis.peptidetracker.core.model.FrequencyType
import com.appvexis.peptidetracker.core.model.repository.AnalyticsRepository
import com.appvexis.peptidetracker.core.model.repository.ProtocolRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Database implementation of [ProtocolRepository] mapping database operations to Domain.
 * Triggers precomputed analytics updates on relevant modification events.
 */
@Singleton
class ProtocolRepositoryImpl @Inject constructor(
    private val protocolDao: ProtocolDao,
    private val analyticsRepository: AnalyticsRepository,
    private val database: PepLogDatabase
) : ProtocolRepository {

    override fun getAllProtocols(): Flow<List<Protocol>> {
        return protocolDao.getAllProtocols().map { list -> list.map { it.toDomain() } }
    }

    override fun getAllCompounds(): Flow<List<ProtocolCompound>> {
        return protocolDao.getAllCompounds().map { list -> list.map { it.toDomain() } }
    }

    override fun getActiveProtocols(): Flow<List<ProtocolWithCompounds>> {
        return protocolDao.getActiveProtocols().map { list -> list.map { it.toDomain() } }
    }

    override fun getProtocolById(id: String): Flow<ProtocolWithCompounds?> {
        return protocolDao.getProtocolById(id).map { it?.toDomain() }
    }

    override suspend fun insertProtocol(protocol: Protocol) {
        validateProtocol(protocol)
        protocolDao.insertProtocol(protocol.toEntity())
        // Initialize or update analytics for the new protocol
        analyticsRepository.recomputeAnalyticsForProtocol(protocol.id)
    }

    override suspend fun updateProtocol(protocol: Protocol) {
        validateProtocol(protocol)
        protocolDao.updateProtocol(protocol.toEntity())
        // Protocol status or dates might have changed, triggering analytics recalculation
        analyticsRepository.recomputeAnalyticsForProtocol(protocol.id)
    }

    override suspend fun deleteProtocol(id: String) {
        // "Delete" is intentionally an archive operation. Hard-deleting the
        // parent would cascade through dose_log and erase completed history.
        protocolDao.archiveProtocol(id, System.currentTimeMillis())
    }

    override suspend fun insertCompound(compound: ProtocolCompound) {
        validateCompound(compound)
        protocolDao.insertCompound(compound.toEntity())
        analyticsRepository.recomputeAnalyticsForProtocol(compound.protocolId)
    }

    override suspend fun insertCompoundWithDoseSchedule(compound: ProtocolCompound, doseLogs: List<DoseLog>) {
        validateCompound(compound)
        require(doseLogs.map { it.id }.distinct().size == doseLogs.size) {
            "Generated dose rows must have unique IDs"
        }
        require(doseLogs.all { it.protocolCompoundId == compound.id }) {
            "Generated dose rows must reference the new compound"
        }
        doseLogs.forEach { doseLog ->
            require(doseLog.status == DoseStatus.PENDING && doseLog.actualTime == null) {
                "A new schedule can only contain pending doses"
            }
            require(doseLog.id.isNotBlank()) { "A generated dose must have an ID" }
            require(doseLog.scheduledTime >= 0L && doseLog.createdAt >= 0L) {
                "Generated dose timestamps are invalid"
            }
            require(doseLog.doseAmount.isFinite() && doseLog.doseAmount > 0.0) {
                "Generated dose amount is invalid"
            }
        }
        database.withTransaction {
            protocolDao.insertCompound(compound.toEntity())
            if (doseLogs.isNotEmpty()) {
                database.logDao().insertDoseLogs(doseLogs.map { it.toEntity() })
            }
        }
        analyticsRepository.recomputeAnalyticsForProtocol(compound.protocolId)
    }

    override suspend fun updateCompound(compound: ProtocolCompound) {
        validateCompound(compound)
        protocolDao.updateCompound(compound.toEntity())
        analyticsRepository.recomputeAnalyticsForProtocol(compound.protocolId)
    }

    private fun validateCompound(compound: ProtocolCompound) {
        require(compound.protocolId.isNotBlank()) { "A compound must reference a protocol" }
        require(compound.peptideId.isNotBlank()) { "A compound must reference a peptide" }
        require(compound.timeOfDay.isNotBlank()) { "A compound must include a dose time" }
        require(compound.doseAmount.isFinite() && compound.doseAmount > 0.0) {
            "Dose amount must be a finite value greater than zero"
        }
        val frequencyDays = compound.frequencyDays
        when (compound.frequencyType) {
            FrequencyType.CUSTOM -> require(
                frequencyDays?.isNotEmpty() == true &&
                    frequencyDays.all { it in 1..7 }
            ) {
                "Custom frequency requires at least one day from Sunday=1 through Saturday=7"
            }
            FrequencyType.CYCLE -> {
                val cycleDays = frequencyDays.orEmpty()
                require(
                    cycleDays.getOrNull(0)?.let { it in 1..365 } == true &&
                        cycleDays.getOrNull(1)?.let { it in 0..365 } == true
                ) {
                    "Cycle frequency requires valid on/off day counts"
                }
            }
            else -> require(frequencyDays.orEmpty().all { it in 1..7 }) {
                "Frequency days must use the Sunday=1 through Saturday=7 convention"
            }
        }
        if (compound.titrationEnabled) {
            val steps = compound.titrationSchedule.orEmpty()
            require(steps.isNotEmpty()) { "Titration requires at least one step" }
            require(steps.map { it.week }.distinct().size == steps.size && steps.any { it.week == 1 }) {
                "Titration weeks must be unique and start at week 1"
            }
            require(steps.all { it.week in 1..52 && it.doseAmount.isFinite() && it.doseAmount > 0.0 }) {
                "Titration steps must use valid weeks and doses"
            }
        }
    }

    override suspend fun deleteCompound(id: String) {
        val compound = protocolDao.getCompoundById(id)
        // Keep the parent row so completed dose logs remain valid and visible
        // in history. The relation mapper hides inactive compounds from the
        // active protocol screen.
        protocolDao.deactivateCompound(id)
        compound?.let {
            analyticsRepository.recomputeAnalyticsForProtocol(it.protocolId)
        }
    }

    private fun validateProtocol(protocol: Protocol) {
        require(protocol.id.isNotBlank()) { "Protocol ID is required" }
        require(protocol.name.isNotBlank() && protocol.name.length <= 100) {
            "Protocol name must be between 1 and 100 characters"
        }
        require(protocol.createdAt >= 0L && protocol.updatedAt >= 0L) {
            "Protocol timestamps are invalid"
        }
    }
}
