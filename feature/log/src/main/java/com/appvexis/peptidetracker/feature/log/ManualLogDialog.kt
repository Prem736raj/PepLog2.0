package com.appvexis.peptidetracker.feature.log

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
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
import com.appvexis.peptidetracker.core.ui.components.PepLogButton
import com.appvexis.peptidetracker.core.ui.components.PepLogTextField
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme

/**
 * Dialog for manually logging a dose when the user wants to
 * record an ad-hoc or previously unscheduled dose.
 */
@Composable
fun ManualLogDialog(
    onDismiss: () -> Unit,
    onConfirm: (peptideName: String, doseAmount: String, doseUnit: String, notes: String) -> Unit
) {
    var peptideName by remember { mutableStateOf("") }
    var doseAmount by remember { mutableStateOf("") }
    var doseUnit by remember { mutableStateOf("mcg") }
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
                    text = "Manually record a dose you've taken.",
                    color = PepLogTheme.colors.textSecondary,
                    fontSize = 14.sp
                )
                
                Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))
                
                PepLogTextField(
                    value = peptideName,
                    onValueChange = { peptideName = it },
                    label = "Peptide Name",
                    placeholder = "e.g., BPC-157",
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    PepLogTextField(
                        value = doseAmount,
                        onValueChange = { doseAmount = it },
                        label = "Dose",
                        placeholder = "250",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.width(PepLogTheme.spacing.small))
                    
                    PepLogTextField(
                        value = doseUnit,
                        onValueChange = { doseUnit = it },
                        label = "Unit",
                        placeholder = "mcg",
                        modifier = Modifier.weight(0.6f)
                    )
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
            PepLogButton(
                text = "Log Dose",
                onClick = { onConfirm(peptideName, doseAmount, doseUnit, notes) },
                enabled = peptideName.isNotBlank() && doseAmount.isNotBlank(),
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
