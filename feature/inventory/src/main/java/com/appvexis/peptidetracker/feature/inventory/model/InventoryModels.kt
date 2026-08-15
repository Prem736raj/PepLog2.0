package com.appvexis.peptidetracker.feature.inventory.model

import com.appvexis.peptidetracker.core.model.InventoryItem
import com.appvexis.peptidetracker.core.model.InventoryStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Filter tabs for inventory management list view.
 */
enum class InventoryFilter(val displayName: String) {
    ALL("All Vials"),
    IN_USE("In Use"),
    IN_STOCK("In Stock (Unmixed)"),
    EXPIRING_OR_LOW("Alerts & Low"),
    EXPIRED("Expired"),
    EMPTY("Finished / Empty")
}

/**
 * Expiration status based on 28-day countdown from reconstitution date.
 */
enum class ExpirationStatus {
    UNRECONSTITUTED,
    FRESH,           // > 7 days remaining
    EXPIRING_SOON,   // 4..7 days remaining
    CRITICAL,        // 1..3 days remaining
    EXPIRED          // <= 0 days remaining
}

/**
 * Enriched UI model for displaying a peptide vial in the inventory dashboard.
 */
data class VialUiModel(
    val item: InventoryItem,
    val peptideName: String,
    val peptideCategory: String,
    val expirationStatus: ExpirationStatus,
    val daysRemaining: Int?,
    val hoursRemaining: Int?,
    val shelfLifeElapsedRatio: Float?, // 0.0f (freshly mixed) to 1.0f (expired)
    val fillPercent: Float,            // 0.0f to 1.0f
    val isLowVolume: Boolean,
    val concentrationDisplay: String?,
    val syringeUnitDoseDisplay: String?,
    val formattedPurchaseDate: String,
    val formattedReconstitutionDate: String?,
    val formattedExpirationDate: String?
)

/**
 * Helper to compute UI presentation metrics from an [InventoryItem].
 */
fun InventoryItem.toUiModel(peptideName: String, peptideCategory: String): VialUiModel {
    val now = System.currentTimeMillis()
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    val formattedPurchase = purchaseDate?.let { dateFormat.format(Date(it)) } ?: "Not specified"
    val formattedRecon = reconstitutionDate?.let { dateFormat.format(Date(it)) }
    val formattedExp = expirationDate?.let { dateFormat.format(Date(it)) }

    // Expiration calculations
    val expDate = expirationDate
    val reconDate = reconstitutionDate
    val (status, daysLeft, hoursLeft, shelfLifeRatio) = if (isReconstituted && expDate != null && reconDate != null) {
        val totalShelfLifeMs = expDate - reconDate
        val msRemaining = expDate - now
        val msElapsed = now - reconDate

        val days = TimeUnit.MILLISECONDS.toDays(msRemaining).toInt()
        val hours = (TimeUnit.MILLISECONDS.toHours(msRemaining) % 24).toInt()

        val ratio = if (totalShelfLifeMs > 0) {
            (msElapsed.toFloat() / totalShelfLifeMs.toFloat()).coerceIn(0.0f, 1.0f)
        } else 1.0f

        val expStatus = when {
            msRemaining <= 0 -> ExpirationStatus.EXPIRED
            days <= 3 -> ExpirationStatus.CRITICAL
            days <= 7 -> ExpirationStatus.EXPIRING_SOON
            else -> ExpirationStatus.FRESH
        }

        Tuple4(expStatus, days, hours, ratio)
    } else {
        Tuple4(ExpirationStatus.UNRECONSTITUTED, null, null, null)
    }

    // Volume fill calculation
    val initialVol = bacWaterMl ?: 0.0
    val currentVol = remainingVolumeMl ?: initialVol
    val fill = if (initialVol > 0.0) {
        (currentVol / initialVol).toFloat().coerceIn(0.0f, 1.0f)
    } else if (!isReconstituted) {
        1.0f
    } else {
        0.0f
    }

    val isLow = isReconstituted && ((currentVol <= 0.5) || (fill <= 0.2f && currentVol > 0.0))

    // Concentration strings
    val concentrationStr = concentrationMgMl?.let {
        String.format(Locale.US, "%.2f mg/mL", it)
    }

    val syringeDoseStr = concentrationMgMl?.let { conc ->
        if (conc > 0) {
            // In a U-100 1mL insulin syringe: 1 unit = 0.01 mL
            // mcg per unit = conc (mg/mL) * 1000 (mcg/mg) * 0.01 (mL/unit) = conc * 10
            val mcgPerUnit = conc * 10.0
            String.format(Locale.US, "%.1f mcg / unit (U-100)", mcgPerUnit)
        } else null
    }

    return VialUiModel(
        item = this,
        peptideName = peptideName,
        peptideCategory = peptideCategory,
        expirationStatus = if (status == ExpirationStatus.EXPIRED || this.status == InventoryStatus.EXPIRED) ExpirationStatus.EXPIRED else status,
        daysRemaining = daysLeft,
        hoursRemaining = hoursLeft,
        shelfLifeElapsedRatio = shelfLifeRatio,
        fillPercent = fill,
        isLowVolume = isLow,
        concentrationDisplay = concentrationStr,
        syringeUnitDoseDisplay = syringeDoseStr,
        formattedPurchaseDate = formattedPurchase,
        formattedReconstitutionDate = formattedRecon,
        formattedExpirationDate = formattedExp
    )
}

private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
