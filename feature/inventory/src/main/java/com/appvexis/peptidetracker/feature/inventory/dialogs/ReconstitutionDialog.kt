package com.appvexis.peptidetracker.feature.inventory.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Interactive reconstitution tracking dialog calculating resulting concentration,
 * syringe dosage conversion (mcg/unit), and setting up the 28-day expiration countdown.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReconstitutionDialog(
    vial: VialUiModel,
    onDismiss: () -> Unit,
    onConfirmReconstitution: (bacWaterMl: Double, reconstitutionDate: Long) -> Unit
) {
    val colors = PepLogTheme.colors
    var bacWaterInput by remember { mutableStateOf("2.0") }
    val bacWaterPresets = listOf("1.0", "2.0", "2.5", "3.0", "5.0")

    val strengthMg = vial.item.vialStrengthMg
    val bacWaterMl = bacWaterInput.toDoubleOrNull() ?: 0.0

    // Calculations
    val concentrationMgMl = if (bacWaterMl > 0) strengthMg / bacWaterMl else 0.0
    val mcgPerUnit = concentrationMgMl * 10.0 // 1 unit on U-100 = 0.01 mL -> 0.01 * 1000 mcg = 10 * conc

    val now = System.currentTimeMillis()
    val expirationTimestamp = now + TimeUnit.DAYS.toMillis(28)
    val dateFormat = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault())
    val formattedExpDate = dateFormat.format(Date(expirationTimestamp))

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Science,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(PepLogTheme.spacing.small))
                        Column {
                            Text(
                                text = "Reconstitute Vial",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "${vial.peptideName} (${strengthMg.toInt()} mg)",
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

                // Bac Water Input
                Text(
                    text = "Bacteriostatic Water Added (mL) *",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                PepLogTextField(
                    value = bacWaterInput,
                    onValueChange = { bacWaterInput = it },
                    label = "Bac Water Volume (mL)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                // Quick Presets
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    bacWaterPresets.forEach { preset ->
                        PepLogChip(
                            text = "$preset mL",
                            selected = bacWaterInput == preset,
                            onClick = { bacWaterInput = preset }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

                // Real-time Concentration & Syringe Calculation Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surfaceHigh,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        colors.primary.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Calculation Results",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = colors.textPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Concentration",
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                                Text(
                                    text = String.format(Locale.US, "%.2f mg/mL", concentrationMgMl),
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = colors.primary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "U-100 Insulin Syringe",
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                                Text(
                                    text = String.format(Locale.US, "%.1f mcg / unit", mcgPerUnit),
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = colors.secondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

                // 28-Day Expiration Notice Box
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colors.secondary.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        colors.secondary.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = colors.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "28-Day Expiration Countdown Starts",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Expires on $formattedExpDate",
                                fontSize = 12.sp,
                                color = colors.secondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))

                // Storage advice
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Store at 2-8°C (Refrigerated). Protect from light and direct heat.",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.large))

                // Action Buttons
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

                    PepLogButton(
                        text = "Confirm & Mix",
                        onClick = {
                            if (bacWaterMl > 0) {
                                onConfirmReconstitution(bacWaterMl, now)
                            }
                        },
                        enabled = bacWaterMl > 0,
                        variant = PepLogButtonVariant.Primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
