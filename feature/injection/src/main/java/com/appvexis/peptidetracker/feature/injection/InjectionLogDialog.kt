package com.appvexis.peptidetracker.feature.injection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appvexis.peptidetracker.core.model.HealingStatus
import com.appvexis.peptidetracker.core.model.InjectionSiteArea
import com.appvexis.peptidetracker.core.ui.components.PepLogButton
import com.appvexis.peptidetracker.core.ui.components.PepLogChip
import com.appvexis.peptidetracker.core.ui.components.PepLogTextField
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme

/**
 * Dialog for logging pain level and reaction when recording an injection at a site.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InjectionLogDialog(
    selectedSite: InjectionSiteArea,
    onDismiss: () -> Unit,
    onConfirm: (painLevel: Int, healingStatus: HealingStatus, notes: String?) -> Unit
) {
    var painLevel by remember { mutableIntStateOf(1) }
    var healingStatus by remember { mutableStateOf(HealingStatus.OK) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = PepLogTheme.colors.surface,
        title = {
            Column {
                Text(
                    text = "Log Injection",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = PepLogTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = selectedSite.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PepLogTheme.colors.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        text = {
            Column {
                // Pain Level Section
                Text(
                    text = "Pain Level",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = PepLogTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val painOptions = listOf(
                        PainOption(1, Icons.Default.SentimentVerySatisfied, "None", Color(0xFF4CAF50)),
                        PainOption(2, Icons.Default.SentimentSatisfied, "Mild", Color(0xFF8BC34A)),
                        PainOption(3, Icons.Default.SentimentNeutral, "Moderate", Color(0xFFFBBF24)),
                        PainOption(4, Icons.Default.SentimentDissatisfied, "Strong", Color(0xFFFF9800)),
                        PainOption(5, Icons.Default.SentimentVeryDissatisfied, "Severe", Color(0xFFF43F5E))
                    )
                    painOptions.forEach { option ->
                        PainLevelButton(
                            option = option,
                            isSelected = painLevel == option.level,
                            onClick = { painLevel = option.level }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

                // Site Reaction / Healing Status
                Text(
                    text = "Site Reaction",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = PepLogTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.small),
                    verticalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.small)
                ) {
                    HealingStatus.entries.forEach { status ->
                        PepLogChip(
                            text = status.displayLabel(),
                            selected = healingStatus == status,
                            onClick = { healingStatus = status }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

                // Notes
                PepLogTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Notes (Optional)",
                    placeholder = "Bruising, lumps, redness details...",
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            PepLogButton(
                text = "Log Site",
                onClick = { onConfirm(painLevel, healingStatus, notes) },
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

@Composable
private fun PainLevelButton(
    option: PainOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier
                    .background(option.color.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .border(1.5.dp, option.color, RoundedCornerShape(12.dp))
                else Modifier
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = option.icon,
            contentDescription = option.label,
            tint = if (isSelected) option.color else PepLogTheme.colors.textSecondary,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = option.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) option.color else PepLogTheme.colors.textSecondary,
            fontSize = 10.sp
        )
    }
}

private data class PainOption(
    val level: Int,
    val icon: ImageVector,
    val label: String,
    val color: Color
)

private fun HealingStatus.displayLabel(): String = when (this) {
    HealingStatus.OK -> "✓ No Issues"
    HealingStatus.BRUISED -> "🟣 Bruised"
    HealingStatus.LUMP -> "⚪ Lump"
    HealingStatus.REDNESS -> "🔴 Redness"
}
