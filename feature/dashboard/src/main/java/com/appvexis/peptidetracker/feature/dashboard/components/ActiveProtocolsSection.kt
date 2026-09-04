package com.appvexis.peptidetracker.feature.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appvexis.peptidetracker.core.ui.components.PepLogBrandMark
import com.appvexis.peptidetracker.core.ui.components.PepLogButton
import com.appvexis.peptidetracker.core.ui.components.PepLogButtonVariant
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.components.PepLogTag
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import com.appvexis.peptidetracker.feature.dashboard.model.ActiveProtocolUiModel

@Composable
fun ActiveProtocolsSection(
    protocols: List<ActiveProtocolUiModel>,
    onProtocolClick: (protocolId: String) -> Unit,
    onCreateProtocolClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PepLogTheme.colors
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Active protocols",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            PepLogButton(
                text = "Add",
                onClick = onCreateProtocolClick,
                variant = PepLogButtonVariant.Ghost,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
            )
        }
        Spacer(Modifier.height(10.dp))

        if (protocols.isEmpty()) {
            PepLogCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PepLogBrandMark(size = 40.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "No active protocols",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Create one when you are ready to plan and review scheduled entries.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                        )
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                protocols.forEach { protocol ->
                    ProtocolCardItem(item = protocol, onClick = { onProtocolClick(protocol.protocol.id) })
                }
            }
        }
    }
}

@Composable
private fun ProtocolCardItem(item: ActiveProtocolUiModel, onClick: () -> Unit) {
    val colors = PepLogTheme.colors
    val protocol = item.protocol
    PepLogCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = protocol.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    protocol.goal?.takeIf(String::isNotBlank)?.let { goal ->
                        Text(
                            text = goal,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Open ${protocol.name}",
                    tint = colors.textSecondary,
                )
            }

            if (item.compoundNames.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item.compoundNames.take(3).forEach { name -> PepLogTag(text = name) }
                    if (item.compoundNames.size > 3) PepLogTag(text = "+${item.compoundNames.size - 3}")
                }
            }

            if (item.totalCycleDays != null && item.progressPercent != null) {
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "Day ${item.daysElapsed} of ${item.totalCycleDays}",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textSecondary,
                    )
                    Text(
                        text = "${(item.progressPercent * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.primary,
                    )
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { item.progressPercent.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                    color = colors.primary,
                    trackColor = colors.surfaceDim,
                )
            }
        }
    }
}
