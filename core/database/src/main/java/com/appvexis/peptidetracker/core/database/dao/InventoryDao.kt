package com.appvexis.peptidetracker.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.appvexis.peptidetracker.core.database.entity.InventoryItemEntity
import kotlinx.coroutines.flow.Flow

/**
 * Database access object for inventory stock and reconstitution updates.
 */
@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_item ORDER BY acquired_date DESC")
    fun getAllInventoryItems(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_item WHERE id = :id LIMIT 1")
    fun getInventoryItemById(id: String): Flow<InventoryItemEntity?>

    @Query("SELECT * FROM inventory_item WHERE status = 'IN_USE' ORDER BY reconstitution_date DESC")
    fun getInUseItems(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_item WHERE peptide_id = :peptideId ORDER BY acquired_date DESC")
    fun getInventoryItemsByPeptide(peptideId: String): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_item WHERE id = :id LIMIT 1")
    suspend fun getInventoryItemByIdSync(id: String): InventoryItemEntity?

    @Query("SELECT * FROM inventory_item WHERE peptide_id = :peptideId AND status = 'IN_USE' AND remaining_volume_ml > 0 ORDER BY reconstitution_date DESC LIMIT 1")
    suspend fun getActiveInUseVialForPeptide(peptideId: String): InventoryItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItems(items: List<InventoryItemEntity>)

    @Update
    suspend fun updateInventoryItem(item: InventoryItemEntity)

    @Query("DELETE FROM inventory_item WHERE id = :id")
    suspend fun deleteInventoryItem(id: String)

    @Query("DELETE FROM inventory_item")
    suspend fun deleteAllInventoryItems()

    @Query("UPDATE inventory_item SET remaining_volume_ml = :volume WHERE id = :id")
    suspend fun updateRemainingVolume(id: String, volume: Double)

    @Query("UPDATE inventory_item SET remaining_volume_ml = MAX(0.0, COALESCE(remaining_volume_ml, bac_water_ml, 0.0) - :amountMl) WHERE id = :id")
    suspend fun deductRemainingVolume(id: String, amountMl: Double): Int

    @Query("UPDATE inventory_item SET status = :status WHERE id = :id")
    suspend fun updateInventoryStatus(id: String, status: String)

    // Sync query for backup export
    @Query("SELECT * FROM inventory_item ORDER BY acquired_date DESC")
    suspend fun getAllItemsSync(): List<InventoryItemEntity>
}
