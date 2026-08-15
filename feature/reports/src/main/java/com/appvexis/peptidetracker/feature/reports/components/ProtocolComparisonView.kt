package com.appvexis.peptidetracker.feature.reports.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import com.appvexis.peptidetracker.feature.reports.model.ProtocolComparisonCardModel
import com.appvexis.peptidetracker.feature.reports.model.ProtocolComparisonUiModel

/**
 * Cross-protocol comparison view analyzing comparative stack performance.
 */
@Composable
fun ProtocolComparisonView(
    comparisonData: ProtocolComparisonUiModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.medium)
    ) {
        if (comparisonData.protocols.isEmpty()) {
            PepLogCard(
                modifier = Modifier.fillMaxWidth(),
                isGlassmorphic = false
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(PepLogTheme.spacing.large),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                            contentDescription = null,
                            tint = PepLogTheme.colors.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Protocols To Compare",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = PepLogTheme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Create multiple protocol stacks and log doses to analyze comparative effectiveness and adherence.",
                            style = MaterialTheme.typography.bodySmall,
                            color = PepLogTheme.colors.textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Highlights / Best Protocol Insight Banner
            PepLogCard(
                modifier = Modifier.fillMaxWidth(),
                isGlassmorphic = true
            ) {
                Row(
                    modifier = Modifier.padding(PepLogTheme.spacing.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PepLogTheme.colors.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = PepLogTheme.colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Stack Performance Analysis",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = PepLogTheme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val insightText = when {
                            comparisonData.bestAdherenceProtocol != null ->
                                "Top adherence: \"${comparisonData.bestAdherenceProtocol}\""
                            comparisonData.lowestSideEffectProtocol != null ->
                                "Lowest side effects: \"${comparisonData.lowestSideEffectProtocol}\""
                            else -> "Comparing ${comparisonData.protocols.size} protocol stacks"
                        }
                        Text(
                            text = insightText,
                            style = MaterialTheme.typography.bodySmall,
                            color = PepLogTheme.colors.textSecondary
                        )
                    }
                }
            }

            // Protocol Cards List
            comparisonData.protocols.forEach { protocolItem ->
                ProtocolComparisonItemCard(protocol = protocolItem)
            }
        }
    }
}

@Composable
private fun ProtocolComparisonItemCard(
    protocol: ProtocolComparisonCardModel,
    modifier: Modifier = Modifier
) {
    PepLogCard(
        modifier = modifier.fillMaxWidth(),
        isGlassmorphic = false
    ) {
        Column(
            modifier = Modifier.padding(PepLogTheme.spacing.medium)
        ) {
            // Protocol Name & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = protocol.protocolName,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = PepLogTheme.colors.textPrimary
                    )
                    if (protocol.goal != null) {
                        Text(
                            text = "Goal: ${protocol.goal}",
                            style = MaterialTheme.typography.bodySmall,
                            color = PepLogTheme.colors.textSecondary
                        )
                    }
                }

                // Active / Completed Tag
                val statusColor = if (protocol.isActive) PepLogTheme.colors.primary else PepLogTheme.colors.textSecondary
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (protocol.isActive) "Active" else "Completed",
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Metrics Grid (2x2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Adherence
                MetricTile(
                    title = "Adherence",
                    value = "${String.format("%.1f", protocol.adherenceRate)}%",
                    sub = "${protocol.totalDosesTaken}/${protocol.totalDosesScheduled} doses",
                    color = if (protocol.adherenceRate >= 80.0) PepLogTheme.colors.primary else PepLogTheme.colors.secondary,
                    modifier = Modifier.weight(1f)
                )

                // Cycle Length
                MetricTile(
                    title = "Duration",
                    value = "${protocol.totalDays} Days",
                    sub = "Total tracked",
                    color = PepLogTheme.colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Side Effect Severity
                val avgSev = protocol.avgSideEffectSeverity
                MetricTile(
                    title = "Side Effects",
                    value = if (avgSev != null) "${String.format("%.1f", avgSev)} / 10" else "None",
                    sub = "${protocol.sideEffectCount} logged events",
                    color = if (avgSev == null || avgSev <= 3.0) PepLogTheme.colors.primary else PepLogTheme.colors.accent,
                    modifier = Modifier.weight(1f)
                )

                // Wellness Delta
                val wDelta = protocol.wellnessDelta
                val isPos = wDelta >= 0.0
                MetricTile(
                    title = "Wellness Impact",
                    value = "${if (isPos) "+" else ""}${String.format("%.1f", wDelta)}",
                    sub = "Net subjective shift",
                    color = if (isPos) PepLogTheme.colors.primary else PepLogTheme.colors.secondary,
                    modifier = Modifier.weight(1f)
                )
            }

            // Highlights
            if (protocol.highlights.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    protocol.highlights.forEach { highlight ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PepLogTheme.colors.primary.copy(alpha = 0.08f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "✨ $highlight",
                                fontSize = 11.sp,
                                color = PepLogTheme.colors.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricTile(
    title: String,
    value: String,
    sub: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(PepLogTheme.colors.surfaceHigh.copy(alpha = 0.4f))
            .padding(10.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = PepLogTheme.colors.textSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = color
            )
            Text(
                text = sub,
                style = MaterialTheme.typography.bodySmall,
                color = PepLogTheme.colors.textSecondary,
                fontSize = 10.sp
            )
        }
    }
}
