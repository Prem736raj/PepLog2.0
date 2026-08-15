package com.appvexis.peptidetracker.feature.inventory.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.appvexis.peptidetracker.core.ui.components.PepLogButton
import com.appvexis.peptidetracker.core.ui.components.PepLogButtonVariant
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import com.appvexis.peptidetracker.feature.inventory.components.ExpirationCountdownBadge
import com.appvexis.peptidetracker.feature.inventory.components.VialLiquidGauge
import com.appvexis.peptidetracker.feature.inventory.model.VialUiModel
import java.util.Locale

/**
 * Detailed vial inspection dialog displaying comprehensive metadata,
 * batch traceability, concentration breakdown, and reconstitution status.
 */
@Composable
fun VialDetailDialog(
    vial: VialUiModel,
    onDismiss: () -> Unit,
    onReconstitute: () -> Unit,
    onAdjustVolume: () -> Unit,
    onEdit: () -> Unit
) {
    val colors = PepLogTheme.colors
    val item = vial.item

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        PepLogCard(
            isGlassmorphic = true,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = vial.peptideName,
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "${vial.peptideCategory} • ${item.status.name.replace('_', ' ')}",
                            fontSize = 13.sp,
                            color = colors.primary
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

                // Hero Row: Big Vial Gauge + Primary Metrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VialLiquidGauge(
                        fillPercent = vial.fillPercent,
                        isReconstituted = item.isReconstituted,
                        expirationStatus = vial.expirationStatus,
                        remainingVolumeMl = item.remainingVolumeMl,
                        totalVolumeMl = item.bacWaterMl,
                        vialStrengthMg = item.vialStrengthMg,
                        width = 80.dp,
                        height = 124.dp,
                        showReadout = true
                    )

                    Spacer(modifier = Modifier.width(PepLogTheme.spacing.medium))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DetailItem(label = "Vial Strength", value = "${item.vialStrengthMg.toInt()} mg")
                        DetailItem(label = "Stock Quantity", value = "${item.quantity} vial(s)")

                        if (item.isReconstituted) {
                            DetailItem(
                                label = "Volume Remaining",
                                value = "${String.format(Locale.US, "%.2f", item.remainingVolumeMl ?: 0.0)} / ${item.bacWaterMl ?: 0.0} mL"
                            )
                            vial.concentrationDisplay?.let { DetailItem(label = "Concentration", value = it) }
                            vial.syringeUnitDoseDisplay?.let { DetailItem(label = "Syringe (U-100)", value = it) }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

                // Expiration Countdown Section
                if (item.isReconstituted) {
                    Text(
                        text = "28-Day Expiration Tracking",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ExpirationCountdownBadge(
                        expirationStatus = vial.expirationStatus,
                        daysRemaining = vial.daysRemaining,
                        hoursRemaining = vial.hoursRemaining,
                        shelfLifeElapsedRatio = vial.shelfLifeElapsedRatio
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    vial.formattedReconstitutionDate?.let {
                        DetailItem(label = "Reconstituted On", value = it)
                    }
                    vial.formattedExpirationDate?.let {
                        DetailItem(label = "Expires On", value = it)
                    }
                    Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))
                }

                HorizontalDivider(color = colors.surfaceHigh)
                Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

                // Batch & Vendor Metadata
                Text(
                    text = "Batch & Sourcing Details",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))

                DetailItem(label = "Vendor / Supplier", value = item.vendor ?: "Not specified")
                DetailItem(label = "Batch / Lot Number", value = item.batchNumber ?: "Not specified")
                DetailItem(label = "Purchase Date", value = vial.formattedPurchaseDate)
                DetailItem(label = "Storage Location", value = item.storageLocation ?: "Not specified")

                item.notes?.takeIf { it.isNotBlank() }?.let { notesText ->
                    Spacer(modifier = Modifier.height(6.dp))
                    DetailItem(label = "Notes", value = notesText)
                }

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.large))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!item.isReconstituted) {
                        PepLogButton(
                            text = "Reconstitute",
                            onClick = onReconstitute,
                            variant = PepLogButtonVariant.Primary,
                            modifier = Modifier.weight(1f),
                            icon = { Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    } else {
                        PepLogButton(
                            text = "Adjust Vol",
                            onClick = onAdjustVolume,
                            variant = PepLogButtonVariant.Secondary,
                            modifier = Modifier.weight(1f),
                            icon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }

                    PepLogButton(
                        text = "Edit",
                        onClick = onEdit,
                        variant = PepLogButtonVariant.Outlined,
                        modifier = Modifier.weight(1f),
                        icon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = PepLogTheme.colors.textSecondary
        )
        Text(
            text = value,
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = PepLogTheme.colors.textPrimary
        )
    }
}
