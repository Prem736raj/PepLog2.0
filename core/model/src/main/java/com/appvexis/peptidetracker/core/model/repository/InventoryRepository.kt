package com.appvexis.peptidetracker.core.model.repository

import com.appvexis.peptidetracker.core.model.InventoryItem
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface defining operations on peptide stock/inventory.
 */
interface InventoryRepository {
    fun getAllInventoryItems(): Flow<List<InventoryItem>>
    fun getInventoryItemById(id: String): Flow<InventoryItem?>
    fun getInUseItems(): Flow<List<InventoryItem>>
    fun getInventoryItemsByPeptide(peptideId: String): Flow<List<InventoryItem>>
    suspend fun insertInventoryItem(item: InventoryItem)
    suspend fun updateInventoryItem(item: InventoryItem)
    suspend fun deleteInventoryItem(id: String)
    suspend fun deductVolume(id: String, amountMl: Double)
    suspend fun reconstituteVial(id: String, bacWaterMl: Double, reconstitutionDate: Long = System.currentTimeMillis())
    suspend fun deductVolumeForPeptide(peptideId: String, doseAmountMg: Double)
}
