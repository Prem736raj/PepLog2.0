package com.appvexis.peptidetracker.core.database.repository

import com.appvexis.peptidetracker.core.database.dao.ProtocolDao
import com.appvexis.peptidetracker.core.database.entity.toDomain
import com.appvexis.peptidetracker.core.database.entity.toEntity
import com.appvexis.peptidetracker.core.model.Protocol
import com.appvexis.peptidetracker.core.model.ProtocolCompound
import com.appvexis.peptidetracker.core.model.ProtocolWithCompounds
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
    private val analyticsRepository: AnalyticsRepository
) : ProtocolRepository {

    override fun getAllProtocols(): Flow<List<Protocol>> {
        return protocolDao.getAllProtocols().map { list -> list.map { it.toDomain() } }
    }

    override fun getActiveProtocols(): Flow<List<ProtocolWithCompounds>> {
        return protocolDao.getActiveProtocols().map { list -> list.map { it.toDomain() } }
    }

    override fun getProtocolById(id: String): Flow<ProtocolWithCompounds?> {
        return protocolDao.getProtocolById(id).map { it?.toDomain() }
    }

    override suspend fun insertProtocol(protocol: Protocol) {
        protocolDao.insertProtocol(protocol.toEntity())
        // Initialize or update analytics for the new protocol
        analyticsRepository.recomputeAnalyticsForProtocol(protocol.id)
    }

    override suspend fun updateProtocol(protocol: Protocol) {
        protocolDao.updateProtocol(protocol.toEntity())
        // Protocol status or dates might have changed, triggering analytics recalculation
        analyticsRepository.recomputeAnalyticsForProtocol(protocol.id)
    }

    override suspend fun deleteProtocol(id: String) {
        protocolDao.deleteProtocol(id)
        // Cascade deletes will clean up the db tables, but we can clean summaries too
    }

    override suspend fun insertCompound(compound: ProtocolCompound) {
        protocolDao.insertCompound(compound.toEntity())
        analyticsRepository.recomputeAnalyticsForProtocol(compound.protocolId)
    }

    override suspend fun updateCompound(compound: ProtocolCompound) {
        protocolDao.updateCompound(compound.toEntity())
        analyticsRepository.recomputeAnalyticsForProtocol(compound.protocolId)
    }

    override suspend fun deleteCompound(id: String) {
        val compound = protocolDao.getCompoundById(id)
        protocolDao.deleteCompound(id)
        compound?.let {
            analyticsRepository.recomputeAnalyticsForProtocol(it.protocolId)
        }
    }
}
