package com.appvexis.peptidetracker.feature.inventory.dialogs

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.appvexis.peptidetracker.core.ui.components.PepLogButton
import com.appvexis.peptidetracker.core.ui.components.PepLogButtonVariant
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.components.PepLogChip
import com.appvexis.peptidetracker.core.ui.components.PepLogTextField
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import com.appvexis.peptidetracker.feature.inventory.model.VialUiModel
import java.util.Locale

/**
 * Dialog for adjusting remaining volume, logging priming waste / spills,
 * or marking a vial as empty.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdjustVolumeDialog(
    vial: VialUiModel,
    onDismiss: () -> Unit,
    onSaveVolume: (newVolumeMl: Double) -> Unit,
    onMarkEmpty: () -> Unit
) {
    val colors = PepLogTheme.colors
    val currentVol = vial.item.remainingVolumeMl ?: vial.item.bacWaterMl ?: 0.0
    var volumeInput by remember { mutableStateOf(String.format(Locale.US, "%.2f", currentVol)) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        PepLogCard(
            isGlassmorphic = true,
            modifier = Modifier
                .fillMaxWidth(0.92f)
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.secondary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = colors.secondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(PepLogTheme.spacing.small))
                        Column {
                            Text(
                                text = "Adjust Volume",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = colors.textPrimary
                            )
                            Text(
                                text = vial.peptideName,
                                fontSize = 13.sp,
                                color = colors.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
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

                Text(
                    text = "Remaining Liquid Volume (mL)",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))

                PepLogTextField(
                    value = volumeInput,
                    onValueChange = { volumeInput = it },
                    label = "Remaining Volume (mL)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                // Quick deduction / adjustment chips
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Quick Adjustments:",
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val currentVal = volumeInput.toDoubleOrNull() ?: currentVol

                    PepLogChip(
                        text = "-0.05 mL (Prime/Loss)",
                        selected = false,
                        onClick = {
                            val next = maxOf(0.0, currentVal - 0.05)
                            volumeInput = String.format(Locale.US, "%.2f", next)
                        }
                    )

                    PepLogChip(
                        text = "-0.10 mL (10 units)",
                        selected = false,
                        onClick = {
                            val next = maxOf(0.0, currentVal - 0.10)
                            volumeInput = String.format(Locale.US, "%.2f", next)
                        }
                    )

                    PepLogChip(
                        text = "-0.25 mL (25 units)",
                        selected = false,
                        onClick = {
                            val next = maxOf(0.0, currentVal - 0.25)
                            volumeInput = String.format(Locale.US, "%.2f", next)
                        }
                    )

                    PepLogChip(
                        text = "Reset to Full (${vial.item.bacWaterMl ?: 2.0} mL)",
                        selected = false,
                        onClick = {
                            volumeInput = String.format(Locale.US, "%.2f", vial.item.bacWaterMl ?: 2.0)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.large))

                // Mark Empty Option
                PepLogButton(
                    text = "Mark Vial as Empty / Finished",
                    onClick = onMarkEmpty,
                    variant = PepLogButtonVariant.Outlined,
                    modifier = Modifier.fillMaxWidth(),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.RemoveCircleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

                // Save & Cancel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PepLogButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        variant = PepLogButtonVariant.Ghost,
                        modifier = Modifier.weight(1f)
                    )

                    val newVol = volumeInput.toDoubleOrNull()
                    val isValid = newVol != null && newVol.isFinite() && newVol >= 0.0

                    PepLogButton(
                        text = "Save Volume",
                        onClick = {
                            if (newVol != null) {
                                onSaveVolume(newVol)
                            }
                        },
                        enabled = isValid,
                        variant = PepLogButtonVariant.Primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
