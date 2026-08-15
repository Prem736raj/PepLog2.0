package com.appvexis.peptidetracker.core.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing a peptide vial tracked in user inventory.
 */
@Serializable
data class InventoryItem(
    val id: String,
    val peptideId: String,
    val vendor: String?,
    val batchNumber: String?,
    val purchaseDate: Long?,
    val vialStrengthMg: Double,
    val quantity: Int = 1,
    val storageLocation: String?,
    val isReconstituted: Boolean = false,
    val reconstitutionDate: Long?,
    val bacWaterMl: Double?,
    val concentrationMgMl: Double?,
    val expirationDate: Long?,            // Typically 28 days after reconstitution
    val remainingVolumeMl: Double?,
    val notes: String?,
    val status: InventoryStatus = InventoryStatus.IN_STOCK
)
