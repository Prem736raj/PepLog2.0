package com.appvexis.peptidetracker.feature.progress.dialogs

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
import androidx.compose.material.icons.filled.MedicalInformation
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.appvexis.peptidetracker.core.model.Protocol
import com.appvexis.peptidetracker.core.ui.components.PepLogButton
import com.appvexis.peptidetracker.core.ui.components.PepLogButtonVariant
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.components.PepLogChip
import com.appvexis.peptidetracker.core.ui.components.PepLogTextField
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import com.appvexis.peptidetracker.feature.progress.model.BiomarkerCatalog
import com.appvexis.peptidetracker.feature.progress.model.BiomarkerDefinition

/**
 * Dialog for entering clinical biomarker lab test results with 50+ catalog shortcuts.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogBiomarkerDialog(
    protocols: List<Protocol>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, value: Double, unit: String, protocolId: String?, lab: String?, notes: String?) -> Unit
) {
    val colors = PepLogTheme.colors

    var biomarkerNameInput by remember { mutableStateOf("IGF-1 (Somatomedin C)") }
    var valueInput by remember { mutableStateOf("") }
    var unitInput by remember { mutableStateOf("ng/mL") }
    var labNameInput by remember { mutableStateOf("Labcorp") }
    var selectedProtocolId by remember { mutableStateOf<String?>(protocols.firstOrNull()?.id) }
    var notesInput by remember { mutableStateOf("") }

    val popularBiomarkers = listOf(
        "IGF-1 (Somatomedin C)",
        "Fasting Blood Glucose",
        "HbA1c (Glycated Hemoglobin)",
        "Fasting Insulin",
        "Total Testosterone",
        "Free Testosterone",
        "hs-CRP",
        "ALT (Alanine Aminotransferase)",
        "Total Cholesterol",
        "ApoB"
    )

    val popularLabs = listOf("Labcorp", "Quest Diagnostics", "PrivateMDLabs", "DirectLabs", "Hospital Lab")

    fun selectBiomarker(def: BiomarkerDefinition) {
        biomarkerNameInput = def.name
        unitInput = def.defaultUnit
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
                                imageVector = Icons.Default.MedicalInformation,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Log Lab Result",
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

                // Biomarker Name Input
                Text(
                    text = "Biomarker / Lab Test Name",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                PepLogTextField(
                    value = biomarkerNameInput,
                    onValueChange = {
                        biomarkerNameInput = it
                        BiomarkerCatalog.findDefinition(it)?.let { def -> unitInput = def.defaultUnit }
                    },
                    label = "Test Name",
                    placeholder = "e.g. IGF-1, Fasting Glucose"
                )

                // Quick Popular Biomarker Suggestions
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    popularBiomarkers.forEach { name ->
                        PepLogChip(
                            text = name,
                            selected = biomarkerNameInput == name,
                            onClick = {
                                BiomarkerCatalog.findDefinition(name)?.let { selectBiomarker(it) } ?: run {
                                    biomarkerNameInput = name
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

                // Value and Unit Inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PepLogTextField(
                        value = valueInput,
                        onValueChange = { valueInput = it },
                        label = "Test Value",
                        placeholder = "e.g. 245.0",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )

                    PepLogTextField(
                        value = unitInput,
                        onValueChange = { unitInput = it },
                        label = "Unit",
                        placeholder = "e.g. ng/mL",
                        modifier = Modifier.weight(0.8f)
                    )
                }

                // Reference Range Preview
                BiomarkerCatalog.findDefinition(biomarkerNameInput)?.let { def ->
                    if (def.rangeLow != null && def.rangeHigh != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = colors.surfaceHigh
                        ) {
                            Text(
                                text = "Standard Reference Range: ${def.rangeLow} - ${def.rangeHigh} ${def.defaultUnit}",
                                fontSize = 11.sp,
                                color = colors.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

                // Lab Provider
                Text(
                    text = "Laboratory Provider (Optional)",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                PepLogTextField(
                    value = labNameInput,
                    onValueChange = { labNameInput = it },
                    label = "Lab Name",
                    placeholder = "e.g. Labcorp"
                )

                // Lab Suggestions
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    popularLabs.forEach { lab ->
                        PepLogChip(
                            text = lab,
                            selected = labNameInput == lab,
                            onClick = { labNameInput = lab }
                        )
                    }
                }

                // Associated Protocol
                if (protocols.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))
                    Text(
                        text = "Associated protocol",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PepLogChip(
                            text = "None (General)",
                            selected = selectedProtocolId == null,
                            onClick = { selectedProtocolId = null }
                        )
                        protocols.forEach { proto ->
                            PepLogChip(
                                text = proto.name,
                                selected = selectedProtocolId == proto.id,
                                onClick = { selectedProtocolId = proto.id }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

                // Notes
                PepLogTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = "Clinical Notes (Optional)",
                    placeholder = "e.g. 12-hour fasted, drawn at 8:00 AM"
                )

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.large))

                // Action Buttons
                val valueNum = valueInput.toDoubleOrNull()
                val isValid = biomarkerNameInput.isNotBlank() && valueNum != null && unitInput.isNotBlank()

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
                        text = "Save Result",
                        onClick = {
                            if (valueNum != null) {
                                onConfirm(
                                    biomarkerNameInput.trim(),
                                    valueNum,
                                    unitInput.trim(),
                                    selectedProtocolId,
                                    labNameInput.takeIf { it.isNotBlank() },
                                    notesInput.takeIf { it.isNotBlank() }
                                )
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
