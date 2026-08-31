package com.appvexis.peptidetracker.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.appvexis.peptidetracker.core.database.entity.ProtocolCompoundEntity
import com.appvexis.peptidetracker.core.database.entity.ProtocolEntity
import com.appvexis.peptidetracker.core.database.entity.ProtocolWithCompoundsEntity
import kotlinx.coroutines.flow.Flow

/**
 * Database access object for managing protocols and compounds in cycles.
 */
@Dao
interface ProtocolDao {
    @Query("SELECT * FROM protocol ORDER BY created_at DESC")
    fun getAllProtocols(): Flow<List<ProtocolEntity>>

    @Query("SELECT * FROM protocol_compound")
    fun getAllCompounds(): Flow<List<ProtocolCompoundEntity>>

    @Transaction
    @Query("SELECT * FROM protocol WHERE status = 'ACTIVE' ORDER BY created_at DESC")
    fun getActiveProtocols(): Flow<List<ProtocolWithCompoundsEntity>>

    @Transaction
    @Query("SELECT * FROM protocol WHERE id = :id LIMIT 1")
    fun getProtocolById(id: String): Flow<ProtocolWithCompoundsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProtocol(protocol: ProtocolEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProtocols(protocols: List<ProtocolEntity>)

    @Update
    suspend fun updateProtocol(protocol: ProtocolEntity)

    @Query("DELETE FROM protocol WHERE id = :id")
    suspend fun deleteProtocol(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompound(compound: ProtocolCompoundEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompounds(compounds: List<ProtocolCompoundEntity>)

    @Update
    suspend fun updateCompound(compound: ProtocolCompoundEntity)

    @Query("SELECT * FROM protocol_compound WHERE id = :id LIMIT 1")
    suspend fun getCompoundById(id: String): ProtocolCompoundEntity?

    @Query("DELETE FROM protocol_compound WHERE id = :id")
    suspend fun deleteCompound(id: String)

    @Query("DELETE FROM protocol_compound")
    suspend fun deleteAllCompounds()

    @Query("DELETE FROM protocol")
    suspend fun deleteAllProtocols()

    // Sync queries for backup export
    @Query("SELECT * FROM protocol ORDER BY created_at DESC")
    suspend fun getAllProtocolsSync(): List<ProtocolEntity>

    @Query("SELECT * FROM protocol_compound")
    suspend fun getAllCompoundsSync(): List<ProtocolCompoundEntity>
}
