package com.appvexis.peptidetracker.feature.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appvexis.peptidetracker.core.ui.components.PepLogButton
import com.appvexis.peptidetracker.core.ui.components.PepLogButtonVariant
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.components.PepLogIcons
import com.appvexis.peptidetracker.core.ui.components.PepLogTag
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import com.appvexis.peptidetracker.feature.dashboard.model.NextDoseInfo

/** The next scheduled record is intentionally direct: timing, dose, then one action. */
@Composable
fun NextDoseCard(
    nextDose: NextDoseInfo?,
    onLogDose: (doseId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (nextDose == null) return

    val colors = PepLogTheme.colors
    val statusColor = if (nextDose.isOverdue) colors.accent else colors.primary
    val statusLabel = if (nextDose.isOverdue) "Needs attention" else "Next scheduled"

    PepLogCard(modifier = modifier.fillMaxWidth(), isGlassmorphic = true) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PepLogTag(
                    text = statusLabel,
                    color = statusColor,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = statusColor,
                        )
                    },
                )
                Text(
                    text = nextDose.scheduledTimeDisplay,
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.textSecondary,
                )
            }

            Spacer(Modifier.height(18.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(statusColor.copy(alpha = 0.12f), MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = PepLogIcons.DoseLog,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = nextDose.compoundName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${nextDose.doseDisplay} · ${nextDose.timeRemainingDisplay}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            PepLogButton(
                text = "Mark as logged",
                onClick = { onLogDose(nextDose.doseLog.id) },
                variant = if (nextDose.isOverdue) PepLogButtonVariant.Secondary else PepLogButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth(),
                icon = {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                },
            )
        }
    }
}
