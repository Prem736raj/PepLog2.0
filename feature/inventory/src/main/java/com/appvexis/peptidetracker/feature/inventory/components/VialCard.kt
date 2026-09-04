package com.appvexis.peptidetracker.feature.inventory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appvexis.peptidetracker.core.model.InventoryStatus
import com.appvexis.peptidetracker.core.ui.components.PepLogButton
import com.appvexis.peptidetracker.core.ui.components.PepLogButtonVariant
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import com.appvexis.peptidetracker.feature.inventory.model.ExpirationStatus
import com.appvexis.peptidetracker.feature.inventory.model.VialUiModel
import java.util.Locale

/**
 * Vial inventory card displaying complete lifecycle status,
 * custom liquid vial animation, concentration metrics, batch & vendor tags, and actions.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VialCard(
    vial: VialUiModel,
    onReconstitute: () -> Unit,
    onAdjustVolume: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = PepLogTheme.colors
    val item = vial.item

    PepLogCard(
        onClick = onCardClick,
        isGlassmorphic = true,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: Peptide Name, Category, and Status Pill
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
                        fontSize = 18.sp,
                        color = colors.textPrimary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = colors.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = vial.peptideCategory,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (item.quantity > 1) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = colors.surfaceHigh
                            ) {
                                Text(
                                    text = "${item.quantity}x Vials",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textSecondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Status Chip
                StatusChip(status = item.status, isReconstituted = item.isReconstituted)
            }

            Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

            // Body: Visual Vial Gauge + Metrics Information
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Custom Canvas Vial Graphic
                VialLiquidGauge(
                    fillPercent = vial.fillPercent,
                    isReconstituted = item.isReconstituted,
                    expirationStatus = vial.expirationStatus,
                    remainingVolumeMl = item.remainingVolumeMl,
                    totalVolumeMl = item.bacWaterMl,
                    vialStrengthMg = item.vialStrengthMg,
                    width = 64.dp,
                    height = 100.dp,
                    showReadout = true
                )

                Spacer(modifier = Modifier.width(PepLogTheme.spacing.medium))

                // Detail Specs
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Strength & Volume Info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SpecItem(label = "Vial Strength", value = "${item.vialStrengthMg.toInt()} mg")
                        if (item.isReconstituted && item.bacWaterMl != null) {
                            SpecItem(
                                label = "Remaining",
                                value = "${String.format(Locale.US, "%.2f", item.remainingVolumeMl ?: item.bacWaterMl)} / ${item.bacWaterMl} mL"
                            )
                        }
                    }

                    // Concentration if mixed
                    if (item.isReconstituted && vial.concentrationDisplay != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        SpecItem(label = "Concentration", value = vial.concentrationDisplay)
                        vial.syringeUnitDoseDisplay?.let { syringeText ->
                            Text(
                                text = syringeText,
                                fontSize = 11.sp,
                                color = colors.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // 28-day Expiration Countdown
                    Spacer(modifier = Modifier.height(4.dp))
                    ExpirationCountdownBadge(
                        expirationStatus = vial.expirationStatus,
                        daysRemaining = vial.daysRemaining,
                        hoursRemaining = vial.hoursRemaining,
                        shelfLifeElapsedRatio = vial.shelfLifeElapsedRatio
                    )
                }
            }

            // Vendor, Batch & Storage Tags
            Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item.vendor?.takeIf { it.isNotBlank() }?.let { vendor ->
                    TagBadge(text = "Vendor: $vendor")
                }
                item.batchNumber?.takeIf { it.isNotBlank() }?.let { batch ->
                    TagBadge(text = "Batch #$batch")
                }
                item.storageLocation?.takeIf { it.isNotBlank() }?.let { loc ->
                    TagBadge(text = "Storage: $loc")
                }
            }

            Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Primary Action: Reconstitute (if dry) or Adjust Volume (if mixed)
                if (!item.isReconstituted) {
                    PepLogButton(
                        text = "Reconstitute Vial",
                        onClick = onReconstitute,
                        variant = PepLogButtonVariant.Primary,
                        modifier = Modifier.height(40.dp),
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Science,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PepLogButton(
                            text = "Adjust Volume",
                            onClick = onAdjustVolume,
                            variant = PepLogButtonVariant.Secondary,
                            modifier = Modifier.height(40.dp),
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }

                // Edit & Delete Action Icons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Vial",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Vial",
                            tint = colors.accent.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(
    status: InventoryStatus,
    isReconstituted: Boolean
) {
    val colors = PepLogTheme.colors
    val (bg, text, label) = when {
        status == InventoryStatus.EMPTY -> Triple(colors.surfaceHigh, colors.textSecondary, "Empty")
        status == InventoryStatus.EXPIRED -> Triple(colors.accent.copy(alpha = 0.2f), colors.accent, "Expired")
        status == InventoryStatus.IN_USE || isReconstituted -> Triple(colors.primary.copy(alpha = 0.2f), colors.primary, "In Use (Mixed)")
        else -> Triple(colors.secondary.copy(alpha = 0.2f), colors.secondary, "In Stock (Dry)")
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun SpecItem(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            fontSize = 11.sp,
            color = PepLogTheme.colors.textSecondary
        )
        Text(
            text = value,
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = PepLogTheme.colors.textPrimary
        )
    }
}

@Composable
private fun TagBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = PepLogTheme.colors.surfaceHigh
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            color = PepLogTheme.colors.textSecondary,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
