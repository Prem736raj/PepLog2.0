package com.appvexis.peptidetracker.feature.log

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appvexis.peptidetracker.core.model.DoseUnit
import com.appvexis.peptidetracker.core.model.LoggableCompound
import com.appvexis.peptidetracker.core.ui.components.PepLogButton
import com.appvexis.peptidetracker.core.ui.components.PepLogTextField
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme

/** Dialog for recording an unscheduled dose against a real active compound. */
@Composable
fun ManualLogDialog(
    availableCompounds: List<LoggableCompound>,
    onDismiss: () -> Unit,
    onConfirm: (compoundId: String, doseAmount: String, doseUnit: String, notes: String) -> Unit
) {
    var selectedCompoundId by remember(availableCompounds) {
        mutableStateOf(availableCompounds.firstOrNull()?.id.orEmpty())
    }
    val selectedCompound = availableCompounds.firstOrNull { it.id == selectedCompoundId }
    var compoundMenuExpanded by remember { mutableStateOf(false) }
    var doseAmount by remember(selectedCompoundId) {
        mutableStateOf(selectedCompound?.doseAmount?.toDisplayAmount() ?: "")
    }
    var doseUnit by remember(selectedCompoundId) {
        mutableStateOf(selectedCompound?.doseUnit ?: DoseUnit.MCG)
    }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = PepLogTheme.colors.surface,
        title = {
            Text(
                text = "Log a Dose",
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = PepLogTheme.colors.textPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = "Record an unscheduled dose from an active protocol.",
                    color = PepLogTheme.colors.textSecondary,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

                if (availableCompounds.isEmpty()) {
                    Text(
                        text = "Add an active protocol compound before logging a dose.",
                        color = PepLogTheme.colors.textSecondary,
                        fontSize = 14.sp
                    )
                } else {
                    Text(
                        text = "Compound",
                        color = PepLogTheme.colors.textSecondary,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Box {
                        OutlinedButton(
                            onClick = { compoundMenuExpanded = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                        ) {
                            Text(
                                text = selectedCompound?.name ?: "Select a compound",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        DropdownMenu(
                            expanded = compoundMenuExpanded,
                            onDismissRequest = { compoundMenuExpanded = false }
                        ) {
                            availableCompounds.forEach { compound ->
                                DropdownMenuItem(
                                    text = { Text(compound.name) },
                                    onClick = {
                                        selectedCompoundId = compound.id
                                        compoundMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))

                Row(modifier = Modifier.fillMaxWidth()) {
                    PepLogTextField(
                        value = doseAmount,
                        onValueChange = { doseAmount = it },
                        label = "Dose",
                        placeholder = "250",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(PepLogTheme.spacing.small))

                    Row(modifier = Modifier.weight(1.1f)) {
                        DoseUnit.entries.forEach { unit ->
                            FilterChip(
                                selected = doseUnit == unit,
                                onClick = { doseUnit = unit },
                                label = { Text(unit.name) },
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))

                PepLogTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Notes (Optional)",
                    placeholder = "Any additional details...",
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            val amount = doseAmount.toDoubleOrNull()
            PepLogButton(
                text = "Log Dose",
                onClick = {
                    if (selectedCompound != null && amount != null) {
                        onConfirm(selectedCompound.id, doseAmount, doseUnit.name, notes)
                    }
                },
                enabled = selectedCompound != null &&
                    amount != null && amount.isFinite() && amount > 0.0 && amount <= MAX_DOSE_AMOUNT,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = PepLogTheme.colors.textSecondary,
                    fontFamily = OutfitFontFamily
                )
            }
        }
    )
}

private const val MAX_DOSE_AMOUNT = 1_000_000.0

private fun Double.toDisplayAmount(): String =
    if (this % 1.0 == 0.0) toLong().toString() else toString()
