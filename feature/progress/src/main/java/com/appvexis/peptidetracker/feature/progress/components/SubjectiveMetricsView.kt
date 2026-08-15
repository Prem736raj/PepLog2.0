package com.appvexis.peptidetracker.feature.progress.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appvexis.peptidetracker.core.model.SideEffectLog
import com.appvexis.peptidetracker.core.ui.components.PepLogButton
import com.appvexis.peptidetracker.core.ui.components.PepLogButtonVariant
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.components.PepLogTextField
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import com.appvexis.peptidetracker.feature.progress.model.DailySubjectiveCheckIn

/**
 * Subjective wellness check-in and side effect logging view.
 */
@Composable
fun SubjectiveMetricsView(
    latestCheckIn: DailySubjectiveCheckIn?,
    history: List<DailySubjectiveCheckIn>,
    onSaveCheckIn: (mood: Int, energy: Int, sleep: Int, pain: Int, libido: Int, notes: String?) -> Unit,
    onLogSideEffectClick: () -> Unit,
    onDeleteSideEffect: (id: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = PepLogTheme.colors

    var moodVal by remember(latestCheckIn) { mutableFloatStateOf(latestCheckIn?.mood?.toFloat() ?: 7f) }
    var energyVal by remember(latestCheckIn) { mutableFloatStateOf(latestCheckIn?.energy?.toFloat() ?: 7f) }
    var sleepVal by remember(latestCheckIn) { mutableFloatStateOf(latestCheckIn?.sleep?.toFloat() ?: 8f) }
    var painVal by remember(latestCheckIn) { mutableFloatStateOf(latestCheckIn?.pain?.toFloat() ?: 1f) }
    var libidoVal by remember(latestCheckIn) { mutableFloatStateOf(latestCheckIn?.libido?.toFloat() ?: 7f) }
    var notesInput by remember(latestCheckIn) { mutableStateOf(latestCheckIn?.notes ?: "") }

    Column(modifier = modifier.fillMaxWidth()) {
        // Today's Check-in Card
        PepLogCard(
            isGlassmorphic = true,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Daily Wellness Check-in",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Track cognitive, energy, and recovery metrics (1-10 scale)",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

                // Sliders
                MetricSliderRow(
                    icon = Icons.Default.Mood,
                    iconTint = colors.secondary,
                    label = "Mood & Mental Clarity",
                    value = moodVal,
                    onValueChange = { moodVal = it },
                    valueLabel = "${moodVal.toInt()}/10"
                )

                MetricSliderRow(
                    icon = Icons.Default.Bolt,
                    iconTint = colors.primary,
                    label = "Physical Energy & Drive",
                    value = energyVal,
                    onValueChange = { energyVal = it },
                    valueLabel = "${energyVal.toInt()}/10"
                )

                MetricSliderRow(
                    icon = Icons.Default.Bedtime,
                    iconTint = colors.primaryVariant,
                    label = "Sleep Quality & Restfulness",
                    value = sleepVal,
                    onValueChange = { sleepVal = it },
                    valueLabel = "${sleepVal.toInt()}/10"
                )

                MetricSliderRow(
                    icon = Icons.Default.Healing,
                    iconTint = colors.accent,
                    label = "Pain & Soreness Level",
                    value = painVal,
                    onValueChange = { painVal = it },
                    valueLabel = "${painVal.toInt()}/10"
                )

                MetricSliderRow(
                    icon = Icons.Default.LocalFireDepartment,
                    iconTint = colors.secondary,
                    label = "Libido & Vitality",
                    value = libidoVal,
                    onValueChange = { libidoVal = it },
                    valueLabel = "${libidoVal.toInt()}/10"
                )

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))

                PepLogTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = "Daily Wellness Notes",
                    placeholder = "e.g. Deep REM sleep recorded, no brain fog, high focus"
                )

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

                PepLogButton(
                    text = "Save Today's Check-in",
                    onClick = {
                        onSaveCheckIn(
                            moodVal.toInt(),
                            energyVal.toInt(),
                            sleepVal.toInt(),
                            painVal.toInt(),
                            libidoVal.toInt(),
                            notesInput.takeIf { it.isNotBlank() }
                        )
                    },
                    variant = PepLogButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth(),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(PepLogTheme.spacing.large))

        // Side Effects & Symptoms Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Side Effect & Symptom Log",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = colors.textPrimary
                )
                Text(
                    text = "Monitor protocol reactions and tolerances",
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            }

            PepLogButton(
                text = "Log Symptom",
                onClick = onLogSideEffectClick,
                variant = PepLogButtonVariant.Secondary,
                modifier = Modifier.height(36.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))

        // Side effect list from history
        val allSideEffects = history.flatMap { it.sideEffects }
        if (allSideEffects.isEmpty()) {
            PepLogCard(
                isGlassmorphic = true,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.success.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = colors.success,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "No Adverse Reactions Logged",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Protocols are well tolerated. Tap 'Log Symptom' if any side effect arises.",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                allSideEffects.take(10).forEach { se ->
                    SideEffectCardItem(
                        item = se,
                        onDeleteClick = { onDeleteSideEffect(se.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricSliderRow(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueLabel: String
) {
    val colors = PepLogTheme.colors

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary
                )
            }

            Text(
                text = valueLabel,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = colors.primary
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 1f..10f,
            steps = 8,
            colors = SliderDefaults.colors(
                thumbColor = colors.primary,
                activeTrackColor = colors.primary,
                inactiveTrackColor = colors.surfaceHigh
            )
        )
    }
}

@Composable
private fun SideEffectCardItem(
    item: SideEffectLog,
    onDeleteClick: () -> Unit
) {
    val colors = PepLogTheme.colors

    PepLogCard(
        isGlassmorphic = true,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.accent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = item.category ?: item.type,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Severity: ${item.severity}/10 ${if (!item.notes.isNullOrBlank()) "• ${item.notes}" else ""}",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
            }

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = colors.textSecondary.copy(alpha = 0.7f),
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}
