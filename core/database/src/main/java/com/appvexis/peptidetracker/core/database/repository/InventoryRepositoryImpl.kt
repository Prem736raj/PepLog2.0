package com.appvexis.peptidetracker.core.database.repository

import com.appvexis.peptidetracker.core.database.dao.InventoryDao
import com.appvexis.peptidetracker.core.database.entity.toDomain
import com.appvexis.peptidetracker.core.database.entity.toEntity
import com.appvexis.peptidetracker.core.model.InventoryItem
import com.appvexis.peptidetracker.core.model.InventoryStatus
import com.appvexis.peptidetracker.core.model.repository.InventoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Database implementation of [InventoryRepository] mapping database operations to Domain.
 */
@Singleton
class InventoryRepositoryImpl @Inject constructor(
    private val inventoryDao: InventoryDao
) : InventoryRepository {

    override fun getAllInventoryItems(): Flow<List<InventoryItem>> {
        return inventoryDao.getAllInventoryItems().map { list -> list.map { it.toDomain() } }
    }

    override fun getInventoryItemById(id: String): Flow<InventoryItem?> {
        return inventoryDao.getInventoryItemById(id).map { it?.toDomain() }
    }

    override fun getInUseItems(): Flow<List<InventoryItem>> {
        return inventoryDao.getInUseItems().map { list -> list.map { it.toDomain() } }
    }

    override fun getInventoryItemsByPeptide(peptideId: String): Flow<List<InventoryItem>> {
        return inventoryDao.getInventoryItemsByPeptide(peptideId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertInventoryItem(item: InventoryItem) {
        inventoryDao.insertInventoryItem(item.toEntity())
    }

    override suspend fun updateInventoryItem(item: InventoryItem) {
        inventoryDao.updateInventoryItem(item.toEntity())
    }

    override suspend fun deleteInventoryItem(id: String) {
        inventoryDao.deleteInventoryItem(id)
    }

    override suspend fun deductVolume(id: String, amountMl: Double) {
        val entity = inventoryDao.getInventoryItemByIdSync(id) ?: return
        val currentVolume = entity.remainingVolumeMl ?: 0.0
        val newVolume = maxOf(0.0, currentVolume - amountMl)
        inventoryDao.updateRemainingVolume(id, newVolume)
        if (newVolume <= 0.001) {
            inventoryDao.updateInventoryStatus(id, InventoryStatus.EMPTY.name)
        }
    }

    override suspend fun reconstituteVial(id: String, bacWaterMl: Double, reconstitutionDate: Long) {
        val entity = inventoryDao.getInventoryItemByIdSync(id) ?: return
        val concentration = if (bacWaterMl > 0) entity.vialStrengthMg / bacWaterMl else 0.0
        val expirationDate = reconstitutionDate + TimeUnit.DAYS.toMillis(28)

        val updated = entity.copy(
            isReconstituted = true,
            reconstitutionDate = reconstitutionDate,
            bacWaterMl = bacWaterMl,
            concentrationMgMl = concentration,
            expirationDate = expirationDate,
            remainingVolumeMl = bacWaterMl,
            status = InventoryStatus.IN_USE
        )
        inventoryDao.updateInventoryItem(updated)
    }

    override suspend fun deductVolumeForPeptide(peptideId: String, doseAmountMg: Double) {
        val activeVial = inventoryDao.getActiveInUseVialForPeptide(peptideId) ?: return
        val concentration = activeVial.concentrationMgMl ?: return
        if (concentration <= 0) return

        val volumeToDeduct = doseAmountMg / concentration
        val currentVolume = activeVial.remainingVolumeMl ?: activeVial.bacWaterMl ?: 0.0
        val newVolume = maxOf(0.0, currentVolume - volumeToDeduct)

        inventoryDao.updateRemainingVolume(activeVial.id, newVolume)
        if (newVolume <= 0.001) {
            inventoryDao.updateInventoryStatus(activeVial.id, InventoryStatus.EMPTY.name)
        }
    }
}
