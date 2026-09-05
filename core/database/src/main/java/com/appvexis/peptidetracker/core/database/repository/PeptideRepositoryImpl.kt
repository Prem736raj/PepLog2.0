package com.appvexis.peptidetracker.core.database.repository

import android.content.Context
import com.appvexis.peptidetracker.core.database.dao.PeptideDao
import com.appvexis.peptidetracker.core.database.entity.toDomain
import com.appvexis.peptidetracker.core.database.entity.toEntity
import com.appvexis.peptidetracker.core.model.Peptide
import com.appvexis.peptidetracker.core.model.repository.PeptideRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import timber.log.Timber
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

    private val seedMutex = Mutex()
    private var seedComplete = false

    /**
     * Seeding is part of the first collection now, rather than an unmanaged
     * fire-and-forget job that can race the first screen and emit an empty
     * encyclopedia.
     */
    private suspend fun ensureSeeded() {
        if (seedComplete) return
        seedMutex.withLock {
            if (seedComplete) return
            val existing = peptideDao.getAllPeptides().first()
            val jsonString = context.assets.open("peptides.json").bufferedReader().use { it.readText() }
            val json = Json { ignoreUnknownKeys = true }
            val seededPeptides = json.decodeFromString<List<Peptide>>(jsonString)
            val existingBookmarks = existing.associate { it.id to it.isBookmarked }
            // Refresh built-in reference content on upgrades while preserving
            // the only user-owned catalog field: bookmarks. Custom entries
            // use a separate ID namespace and are left untouched.
            peptideDao.insertAll(
                seededPeptides.map { peptide ->
                    peptide.copy(isBookmarked = existingBookmarks[peptide.id] ?: peptide.isBookmarked)
                        .toEntity()
                }
            )
            Timber.i("Refreshed ${seededPeptides.size} encyclopedia entries")
            seedComplete = true
        }
    }

    override fun getAllPeptides(): Flow<List<Peptide>> {
        return seededFlow { peptideDao.getAllPeptides() }
    }

    override fun getPeptidesByCategory(category: String): Flow<List<Peptide>> {
        return seededFlow { peptideDao.getPeptidesByCategory(category) }
    }

    override fun getBookmarkedPeptides(): Flow<List<Peptide>> {
        return seededFlow { peptideDao.getBookmarkedPeptides() }
    }

    override fun searchPeptides(query: String): Flow<List<Peptide>> {
        return seededFlow { peptideDao.searchPeptides(query) }
    }

    override fun getPeptideById(id: String): Flow<Peptide?> {
        return flow {
            ensureSeeded()
            emitAll(peptideDao.getPeptideById(id).map { it?.toDomain() })
        }
    }

    override suspend fun setBookmarked(id: String, isBookmarked: Boolean) {
        ensureSeeded()
        peptideDao.setBookmarked(id, isBookmarked)
    }

    override suspend fun seedPeptides(peptides: List<Peptide>) {
        peptideDao.insertAll(peptides.map { it.toEntity() })
        seedMutex.withLock { seedComplete = true }
    }

    override suspend fun addCustomPeptide(peptide: Peptide) {
        ensureSeeded()
        require(peptide.id.startsWith(CUSTOM_ID_PREFIX)) { "Custom peptide IDs must use the custom namespace" }
        require(peptide.name.isNotBlank() && peptide.name.length <= 80) {
            "Custom peptide name must be between 1 and 80 characters"
        }
        require(peptide.category.isNotBlank() && peptide.category.length <= 40) {
            "Custom peptide category must be between 1 and 40 characters"
        }
        val halfLifeHours = peptide.halfLifeHours
        require(halfLifeHours == null ||
            (halfLifeHours.isFinite() && halfLifeHours > 0.0)
        ) { "Custom peptide half-life is invalid" }
        peptideDao.insertAll(listOf(peptide.toEntity()))
    }

    private fun seededFlow(
        source: () -> Flow<List<com.appvexis.peptidetracker.core.database.entity.PeptideEntity>>
    ): Flow<List<Peptide>> = flow {
        ensureSeeded()
        emitAll(source().map { list -> list.map { it.toDomain() } })
    }

    private companion object {
        const val CUSTOM_ID_PREFIX = "custom-"
    }
}
