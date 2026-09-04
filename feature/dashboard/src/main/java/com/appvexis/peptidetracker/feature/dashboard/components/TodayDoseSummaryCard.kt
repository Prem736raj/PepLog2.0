package com.appvexis.peptidetracker.feature.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.components.PepLogTag
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import com.appvexis.peptidetracker.feature.dashboard.model.TodayDoseUiModel

/** A compact daily schedule that favours legibility and deliberate actions. */
@Composable
fun TodayDoseSummaryCard(
    todayDoses: List<TodayDoseUiModel>,
    takenCount: Int,
    totalCount: Int,
    adherencePercent: Int,
    onMarkTaken: (doseId: String) -> Unit,
    onViewDailyLog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PepLogTheme.colors
    val supportingCopy = when {
        totalCount == 0 -> "No scheduled doses for today."
        takenCount >= totalCount -> "All scheduled doses are logged."
        else -> "${totalCount - takenCount} record${if (totalCount - takenCount == 1) "" else "s"} still need attention."
    }

    PepLogCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Today’s schedule",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = supportingCopy,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                    )
                }
                AdherenceProgressRing(
                    progressPercent = adherencePercent,
                    takenDoses = takenCount,
                    totalDoses = totalCount,
                    size = 72.dp,
                    strokeWidth = 7.dp,
                )
            }

            if (todayDoses.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                todayDoses.forEachIndexed { index, dose ->
                    if (index > 0) HorizontalDivider(color = colors.surfaceDim)
                    TodayDoseItemRow(dose = dose, onMarkTaken = onMarkTaken)
                }
                HorizontalDivider(color = colors.surfaceDim)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clickable(role = Role.Button, onClick = onViewDailyLog)
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Open daily log",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.primary,
                    )
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayDoseItemRow(
    dose: TodayDoseUiModel,
    onMarkTaken: (doseId: String) -> Unit,
) {
    val colors = PepLogTheme.colors
    val statusColor = when {
        dose.isTaken -> colors.success
        dose.isOverdue -> colors.accent
        else -> colors.primary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(statusColor.copy(alpha = 0.12f), MaterialTheme.shapes.extraSmall),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (dose.isTaken) Icons.Default.Check else Icons.Default.Schedule,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = dose.compoundName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "${dose.doseDisplay} · ${dose.timeDisplay}",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
            )
        }
        if (dose.isOverdue && !dose.isTaken) {
            PepLogTag(text = "Due", color = colors.accent)
            Spacer(Modifier.width(4.dp))
        }
        if (dose.isTaken) {
            Text(
                text = "Logged",
                style = MaterialTheme.typography.labelMedium,
                color = colors.success,
            )
        } else {
            IconButton(onClick = { onMarkTaken(dose.doseLog.id) }) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Mark ${dose.compoundName} as logged",
                    tint = colors.primary,
                )
            }
        }
    }
}
