package com.appvexis.peptidetracker.core.model.repository

import com.appvexis.peptidetracker.core.model.Protocol
import com.appvexis.peptidetracker.core.model.ProtocolCompound
import com.appvexis.peptidetracker.core.model.ProtocolWithCompounds
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface defining operations on cycle stacks and compounds.
 */
interface ProtocolRepository {
    fun getAllProtocols(): Flow<List<Protocol>>
    fun getActiveProtocols(): Flow<List<ProtocolWithCompounds>>
    fun getProtocolById(id: String): Flow<ProtocolWithCompounds?>
    suspend fun insertProtocol(protocol: Protocol)
    suspend fun updateProtocol(protocol: Protocol)
    suspend fun deleteProtocol(id: String)
    suspend fun insertCompound(compound: ProtocolCompound)
    suspend fun updateCompound(compound: ProtocolCompound)
    suspend fun deleteCompound(id: String)
}
