package com.appvexis.peptidetracker.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.appvexis.peptidetracker.core.database.entity.PeptideEntity
import kotlinx.coroutines.flow.Flow

/**
 * Database access object for the Peptide encyclopedia.
 */
@Dao
interface PeptideDao {
    @Query("SELECT * FROM peptide ORDER BY name ASC")
    fun getAllPeptides(): Flow<List<PeptideEntity>>

    @Query("SELECT * FROM peptide WHERE category = :category ORDER BY name ASC")
    fun getPeptidesByCategory(category: String): Flow<List<PeptideEntity>>

    @Query("SELECT * FROM peptide WHERE is_bookmarked = 1 ORDER BY name ASC")
    fun getBookmarkedPeptides(): Flow<List<PeptideEntity>>

    @Query("SELECT * FROM peptide WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchPeptides(query: String): Flow<List<PeptideEntity>>

    @Query("SELECT * FROM peptide WHERE id = :id LIMIT 1")
    fun getPeptideById(id: String): Flow<PeptideEntity?>

    @Query("UPDATE peptide SET is_bookmarked = :isBookmarked WHERE id = :id")
    suspend fun setBookmarked(id: String, isBookmarked: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(peptides: List<PeptideEntity>)

    /** Removes only user-created catalog entries; built-in reference data stays seeded. */
    @Query("DELETE FROM peptide WHERE id LIKE 'custom-%'")
    suspend fun deleteCustomPeptides()

    /** Synchronous snapshot used when validating a portable restore. */
    @Query("SELECT * FROM peptide ORDER BY name ASC")
    suspend fun getAllPeptidesSync(): List<PeptideEntity>
}
