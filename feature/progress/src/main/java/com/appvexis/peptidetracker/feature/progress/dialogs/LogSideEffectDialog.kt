package com.appvexis.peptidetracker.feature.progress.dialogs

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
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

/**
 * Dialog for recording side effects and adverse symptoms.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogSideEffectDialog(
    protocols: List<Protocol>,
    onDismiss: () -> Unit,
    onConfirm: (category: String, severity: Int, protocolId: String?, notes: String?) -> Unit
) {
    val colors = PepLogTheme.colors

    val commonSymptoms = listOf(
        "Nausea",
        "Headache",
        "Water Retention",
        "Facial Flushing",
        "Injection Site Itch",
        "Fatigue",
        "Dizziness",
        "Appetite Suppression",
        "Muscle Soreness",
        "Insomnia"
    )

    var selectedSymptom by remember { mutableStateOf("Nausea") }
    var severityVal by remember { mutableFloatStateOf(4f) }
    var selectedProtocolId by remember { mutableStateOf<String?>(protocols.firstOrNull()?.id) }
    var notesInput by remember { mutableStateOf("") }

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
                                .background(colors.accent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Log Side Effect",
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

                // Symptom Category Chips
                Text(
                    text = "Symptom / Reaction",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                PepLogTextField(
                    value = selectedSymptom,
                    onValueChange = { selectedSymptom = it },
                    label = "Symptom Name",
                    placeholder = "e.g. Mild headache, injection site welt"
                )

                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    commonSymptoms.forEach { symptom ->
                        PepLogChip(
                            text = symptom,
                            selected = selectedSymptom == symptom,
                            onClick = { selectedSymptom = symptom }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

                // Severity Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Severity (1 - 10)",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "${severityVal.toInt()} / 10",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.accent
                    )
                }

                Slider(
                    value = severityVal,
                    onValueChange = { severityVal = it },
                    valueRange = 1f..10f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = colors.accent,
                        activeTrackColor = colors.accent,
                        inactiveTrackColor = colors.surfaceHigh
                    )
                )

                // Associated Protocol
                if (protocols.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))
                    Text(
                        text = "Associated Protocol Stack",
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
                    label = "Reaction Notes (Optional)",
                    placeholder = "e.g. Occurred ~30 mins after dosing, resolved after meal"
                )

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
                        text = "Save Symptom",
                        onClick = {
                            if (selectedSymptom.isNotBlank()) {
                                onConfirm(
                                    selectedSymptom.trim(),
                                    severityVal.toInt(),
                                    selectedProtocolId,
                                    notesInput.takeIf { it.isNotBlank() }
                                )
                            }
                        },
                        enabled = selectedSymptom.isNotBlank(),
                        variant = PepLogButtonVariant.Primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
