package com.appvexis.peptidetracker.core.model.repository

import com.appvexis.peptidetracker.core.model.Peptide
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for retrieving educational peptide encyclopedia details.
 */
interface PeptideRepository {
    fun getAllPeptides(): Flow<List<Peptide>>
    fun getPeptidesByCategory(category: String): Flow<List<Peptide>>
    fun getBookmarkedPeptides(): Flow<List<Peptide>>
    fun searchPeptides(query: String): Flow<List<Peptide>>
    fun getPeptideById(id: String): Flow<Peptide?>
    suspend fun setBookmarked(id: String, isBookmarked: Boolean)
    suspend fun seedPeptides(peptides: List<Peptide>)
}
