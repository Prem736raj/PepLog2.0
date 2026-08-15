package com.appvexis.peptidetracker.core.database.repository

import android.content.Context
import com.appvexis.peptidetracker.core.database.dao.PeptideDao
import com.appvexis.peptidetracker.core.database.entity.toDomain
import com.appvexis.peptidetracker.core.database.entity.toEntity
import com.appvexis.peptidetracker.core.model.Peptide
import com.appvexis.peptidetracker.core.model.repository.PeptideRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Database implementation of [PeptideRepository] mapping database operations to Domain.
 */
@Singleton
class PeptideRepositoryImpl @Inject constructor(
    private val peptideDao: PeptideDao,
    @ApplicationContext private val context: Context
) : PeptideRepository {

    init {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val existing = peptideDao.getAllPeptides().first()
                if (existing.isEmpty()) {
                    val jsonString = context.assets.open("peptides.json").bufferedReader().use { it.readText() }
                    val json = Json { ignoreUnknownKeys = true }
                    val seededPeptides = json.decodeFromString<List<Peptide>>(jsonString)
                    seedPeptides(seededPeptides)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun getAllPeptides(): Flow<List<Peptide>> {
        return peptideDao.getAllPeptides().map { list -> list.map { it.toDomain() } }
    }

    override fun getPeptidesByCategory(category: String): Flow<List<Peptide>> {
        return peptideDao.getPeptidesByCategory(category).map { list -> list.map { it.toDomain() } }
    }

    override fun getBookmarkedPeptides(): Flow<List<Peptide>> {
        return peptideDao.getBookmarkedPeptides().map { list -> list.map { it.toDomain() } }
    }

    override fun searchPeptides(query: String): Flow<List<Peptide>> {
        return peptideDao.searchPeptides(query).map { list -> list.map { it.toDomain() } }
    }

    override fun getPeptideById(id: String): Flow<Peptide?> {
        return peptideDao.getPeptideById(id).map { it?.toDomain() }
    }

    override suspend fun setBookmarked(id: String, isBookmarked: Boolean) {
        peptideDao.setBookmarked(id, isBookmarked)
    }

    override suspend fun seedPeptides(peptides: List<Peptide>) {
        peptideDao.insertAll(peptides.map { it.toEntity() })
    }
}
