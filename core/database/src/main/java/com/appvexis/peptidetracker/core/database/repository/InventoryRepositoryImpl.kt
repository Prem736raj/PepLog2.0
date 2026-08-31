package com.appvexis.peptidetracker.core.database.repository

import com.appvexis.peptidetracker.core.database.dao.InventoryDao
import com.appvexis.peptidetracker.core.database.entity.toDomain
import com.appvexis.peptidetracker.core.database.entity.toEntity
import com.appvexis.peptidetracker.core.model.InventoryItem
import com.appvexis.peptidetracker.core.model.InventoryStatus
import com.appvexis.peptidetracker.core.model.repository.InventoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
        validate(item)
        inventoryDao.insertInventoryItem(item.toEntity())
    }

    override suspend fun updateInventoryItem(item: InventoryItem) {
        validate(item)
        inventoryDao.updateInventoryItem(item.toEntity())
    }

    override suspend fun deleteInventoryItem(id: String) {
        inventoryDao.deleteInventoryItem(id)
    }

    override suspend fun deductVolume(id: String, amountMl: Double) {
        require(amountMl.isFinite() && amountMl > 0.0) { "Deduction must be a finite value greater than zero" }
        val updatedRows = inventoryDao.deductRemainingVolume(id, amountMl)
        val newVolume = inventoryDao.getInventoryItemByIdSync(id)?.remainingVolumeMl
        if (updatedRows > 0 && newVolume != null && newVolume <= 0.001) {
            inventoryDao.updateInventoryStatus(id, InventoryStatus.EMPTY.name)
        }
    }

    override suspend fun reconstituteVial(id: String, bacWaterMl: Double, reconstitutionDate: Long) {
        val entity = inventoryDao.getInventoryItemByIdSync(id) ?: return
        require(bacWaterMl.isFinite() && bacWaterMl > 0.0) {
            "Bacteriostatic water volume must be a finite value greater than zero"
        }
        require(entity.vialStrengthMg.isFinite() && entity.vialStrengthMg > 0.0) {
            "Vial strength must be a finite value greater than zero"
        }
        val concentration = entity.vialStrengthMg / bacWaterMl
        require(concentration.isFinite() && concentration > 0.0) { "Invalid reconstitution concentration" }
        val expirationDate = java.util.Calendar.getInstance().apply {
            timeInMillis = reconstitutionDate
            add(java.util.Calendar.DAY_OF_YEAR, 28)
        }.timeInMillis

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
        require(doseAmountMg.isFinite() && doseAmountMg > 0.0) {
            "Dose amount must be a finite value greater than zero"
        }
        val activeVial = inventoryDao.getActiveInUseVialForPeptide(peptideId) ?: return
        val concentration = activeVial.concentrationMgMl ?: return
        if (!concentration.isFinite() || concentration <= 0) return

        val volumeToDeduct = doseAmountMg / concentration
        if (!volumeToDeduct.isFinite() || volumeToDeduct <= 0.0) return
        val updatedRows = inventoryDao.deductRemainingVolume(activeVial.id, volumeToDeduct)
        val newVolume = inventoryDao.getInventoryItemByIdSync(activeVial.id)?.remainingVolumeMl
        if (updatedRows > 0 && newVolume != null && newVolume <= 0.001) {
            inventoryDao.updateInventoryStatus(activeVial.id, InventoryStatus.EMPTY.name)
        }
    }

    private fun validate(item: InventoryItem) {
        require(item.id.isNotBlank()) { "Inventory item ID is required" }
        require(item.peptideId.isNotBlank()) { "Inventory item must reference a peptide" }
        require(item.vialStrengthMg.isFinite() && item.vialStrengthMg > 0.0) {
            "Vial strength must be a finite value greater than zero"
        }
        require(item.quantity > 0) { "Inventory quantity must be greater than zero" }
        val bacWaterMl = item.bacWaterMl
        val concentrationMgMl = item.concentrationMgMl
        val remainingVolumeMl = item.remainingVolumeMl
        require(bacWaterMl == null || (bacWaterMl.isFinite() && bacWaterMl > 0.0)) {
            "Bacteriostatic water volume is invalid"
        }
        require(concentrationMgMl == null || (concentrationMgMl.isFinite() && concentrationMgMl > 0.0)) {
            "Inventory concentration is invalid"
        }
        require(remainingVolumeMl == null || (remainingVolumeMl.isFinite() && remainingVolumeMl >= 0.0)) {
            "Remaining volume is invalid"
        }
    }
}
