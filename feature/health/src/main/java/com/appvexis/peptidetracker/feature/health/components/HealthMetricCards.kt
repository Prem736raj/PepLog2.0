package com.appvexis.peptidetracker.feature.health.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appvexis.peptidetracker.core.model.HealthMetricType
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import com.appvexis.peptidetracker.feature.health.model.HealthMetricSummary
import kotlin.math.abs

/** Compact metric cards that use colour only to distinguish data series. */
@Composable
fun HealthMetricCards(
    summaries: List<HealthMetricSummary>,
    selectedType: HealthMetricType,
    onTypeSelected: (HealthMetricType) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(items = summaries, key = { it.metricType.name }) { summary ->
            MetricCard(
                summary = summary,
                isSelected = summary.metricType == selectedType,
                onClick = { onTypeSelected(summary.metricType) },
            )
        }
    }
}

@Composable
private fun MetricCard(
    summary: HealthMetricSummary,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colors = PepLogTheme.colors
    val metricColor = getMetricColor(summary.metricType)
    PepLogCard(
        onClick = onClick,
        isGlassmorphic = isSelected,
        cornerRadius = 12.dp,
        modifier = Modifier.width(156.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = getMetricIcon(summary.metricType),
                    contentDescription = null,
                    tint = metricColor,
                    modifier = Modifier.size(19.dp),
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    text = summary.metricType.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary,
                    maxLines = 1,
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = formatValue(summary.latestValue, summary.metricType),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(summary.metricType.unit, style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
            Spacer(Modifier.height(8.dp))

            summary.changePercent?.let { change ->
                val positive = change > 0
                val changeColor = when {
                    summary.metricType == HealthMetricType.WEIGHT -> colors.textSecondary
                    isChangeGood(summary.metricType, change) -> colors.success
                    else -> colors.accent
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (positive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = changeColor,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = "${String.format("%.1f", abs(change))}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = changeColor,
                    )
                }
            } ?: Text(
                text = "${summary.dataPointCount} readings",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary,
            )
        }
    }
}

private fun formatValue(value: Double, type: HealthMetricType): String = when (type) {
    HealthMetricType.STEPS -> String.format("%.0f", value)
    HealthMetricType.SLEEP_DURATION,
    HealthMetricType.BODY_FAT,
    HealthMetricType.WEIGHT -> String.format("%.1f", value)
    else -> String.format("%.0f", value)
}

private fun isChangeGood(type: HealthMetricType, changePercent: Double): Boolean = when (type) {
    HealthMetricType.SLEEP_DURATION, HealthMetricType.STEPS -> changePercent > 0
    HealthMetricType.HEART_RATE,
    HealthMetricType.RESTING_HEART_RATE,
    HealthMetricType.BLOOD_PRESSURE_SYSTOLIC,
    HealthMetricType.BLOOD_PRESSURE_DIASTOLIC,
    HealthMetricType.BODY_FAT -> changePercent < 0
    HealthMetricType.WEIGHT -> false
}

fun getMetricIcon(type: HealthMetricType): ImageVector = when (type) {
    HealthMetricType.WEIGHT -> Icons.Default.Scale
    HealthMetricType.SLEEP_DURATION -> Icons.Default.Bedtime
    HealthMetricType.HEART_RATE -> Icons.Default.Favorite
    HealthMetricType.RESTING_HEART_RATE -> Icons.Default.HeartBroken
    HealthMetricType.BLOOD_PRESSURE_SYSTOLIC,
    HealthMetricType.BLOOD_PRESSURE_DIASTOLIC -> Icons.Default.MonitorHeart
    HealthMetricType.STEPS -> Icons.Default.DirectionsWalk
    HealthMetricType.BODY_FAT -> Icons.Default.FitnessCenter
}

fun getMetricColor(type: HealthMetricType): Color = when (type) {
    HealthMetricType.WEIGHT -> Color(0xFF5E9D90)
    HealthMetricType.SLEEP_DURATION -> Color(0xFF87927F)
    HealthMetricType.HEART_RATE -> Color(0xFFC97A5A)
    HealthMetricType.RESTING_HEART_RATE -> Color(0xFFB97862)
    HealthMetricType.BLOOD_PRESSURE_SYSTOLIC -> Color(0xFFC59854)
    HealthMetricType.BLOOD_PRESSURE_DIASTOLIC -> Color(0xFFB48A58)
    HealthMetricType.STEPS -> Color(0xFF79A889)
    HealthMetricType.BODY_FAT -> Color(0xFF8B958E)
}
