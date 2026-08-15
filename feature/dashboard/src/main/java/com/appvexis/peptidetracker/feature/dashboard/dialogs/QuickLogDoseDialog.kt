package com.appvexis.peptidetracker.feature.dashboard.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Vaccines
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
import com.appvexis.peptidetracker.core.model.DoseUnit
import com.appvexis.peptidetracker.core.model.Peptide
import com.appvexis.peptidetracker.core.ui.components.PepLogButton
import com.appvexis.peptidetracker.core.ui.components.PepLogButtonVariant
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.components.PepLogChip
import com.appvexis.peptidetracker.core.ui.components.PepLogTextField
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme

/**
 * Quick dose logging popup from the dashboard.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickLogDoseDialog(
    availablePeptides: List<Peptide>,
    onDismiss: () -> Unit,
    onConfirmLog: (compoundName: String, amount: Double, unit: DoseUnit, notes: String?) -> Unit
) {
    val colors = PepLogTheme.colors

    var selectedPeptideName by remember {
        mutableStateOf(availablePeptides.firstOrNull()?.name ?: "BPC-157")
    }
    var doseAmountInput by remember { mutableStateOf("250") }
    var selectedUnit by remember { mutableStateOf(DoseUnit.MCG) }
    var notesInput by remember { mutableStateOf("") }

    val popularPeptides = listOf("BPC-157", "TB-500", "CJC-1295", "Ipamorelin", "Semaglutide", "Tirzepatide", "GHK-Cu")
    val dosePresets = when (selectedUnit) {
        DoseUnit.MCG -> listOf("100", "250", "300", "500", "1000")
        DoseUnit.MG -> listOf("0.25", "0.5", "1.0", "2.5", "5.0")
        DoseUnit.IU -> listOf("2", "4", "6", "8", "10")
    }

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
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Vaccines,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Quick Log Dose",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = colors.textPrimary
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

                // Peptide Name Input
                Text(
                    text = "Peptide Compound",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                PepLogTextField(
                    value = selectedPeptideName,
                    onValueChange = { selectedPeptideName = it },
                    label = "Peptide Name"
                )

                // Quick Popular Chips
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    popularPeptides.forEach { name ->
                        PepLogChip(
                            text = name,
                            selected = selectedPeptideName == name,
                            onClick = { selectedPeptideName = name }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

                // Dose Amount & Unit Row
                Text(
                    text = "Dose Amount & Unit",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PepLogTextField(
                        value = doseAmountInput,
                        onValueChange = { doseAmountInput = it },
                        label = "Amount",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DoseUnit.entries.forEach { unit ->
                            PepLogChip(
                                text = unit.name,
                                selected = selectedUnit == unit,
                                onClick = { selectedUnit = unit }
                            )
                        }
                    }
                }

                // Quick Dose Presets
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    dosePresets.forEach { preset ->
                        PepLogChip(
                            text = "$preset ${selectedUnit.name.lowercase()}",
                            selected = doseAmountInput == preset,
                            onClick = { doseAmountInput = preset }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

                // Notes
                PepLogTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = "Notes / Injection Site (Optional)",
                    placeholder = "e.g. Lower abdomen right side"
                )

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.large))

                // Actions
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

                    val amount = doseAmountInput.toDoubleOrNull()
                    val isValid = selectedPeptideName.isNotBlank() && amount != null && amount > 0

                    PepLogButton(
                        text = "Record Dose",
                        onClick = {
                            if (amount != null) {
                                onConfirmLog(selectedPeptideName, amount, selectedUnit, notesInput.takeIf { it.isNotBlank() })
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
