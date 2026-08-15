package com.appvexis.peptidetracker.feature.health.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appvexis.peptidetracker.core.model.HealthMetricType
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import com.appvexis.peptidetracker.feature.health.model.HealthMetricSummary
import kotlinx.coroutines.delay
import kotlin.math.abs

/**
 * Horizontal scrolling row of glassmorphic metric summary cards.
 * Each card shows the latest value, trend arrow, and change percentage.
 */
@Composable
fun HealthMetricCards(
    summaries: List<HealthMetricSummary>,
    selectedType: HealthMetricType,
    onTypeSelected: (HealthMetricType) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(summaries) { index, summary ->
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                delay(index * 80L)
                visible = true
            }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 2 }
            ) {
                MetricCard(
                    summary = summary,
                    isSelected = summary.metricType == selectedType,
                    onClick = { onTypeSelected(summary.metricType) }
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    summary: HealthMetricSummary,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = getMetricIcon(summary.metricType)
    val metricColor = getMetricColor(summary.metricType)
    val borderAlpha by animateFloatAsState(
        targetValue = if (isSelected) 0.6f else 0.15f,
        animationSpec = tween(300), label = "border"
    )

    Box(
        modifier = modifier
            .width(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PepLogTheme.colors.surface.copy(alpha = 0.85f),
                        PepLogTheme.colors.surface.copy(alpha = 0.65f)
                    )
                )
            )
            .then(
                if (isSelected) {
                    Modifier.background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                metricColor.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                } else Modifier
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = summary.metricType.displayName,
                    tint = metricColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = summary.metricType.displayName,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = PepLogTheme.colors.textSecondary,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = formatValue(summary.latestValue, summary.metricType),
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = PepLogTheme.colors.textPrimary
            )

            Text(
                text = summary.metricType.unit,
                fontFamily = OutfitFontFamily,
                fontSize = 11.sp,
                color = PepLogTheme.colors.textSecondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Change indicator
            if (summary.changePercent != null) {
                val isPositive = summary.changePercent > 0
                val changeColor = if (isChangeGood(summary.metricType, summary.changePercent)) {
                    PepLogTheme.colors.success
                } else {
                    PepLogTheme.colors.accent
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = changeColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${String.format("%.1f", abs(summary.changePercent))}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = changeColor,
                        fontFamily = OutfitFontFamily
                    )
                }
            } else {
                Text(
                    text = "${summary.dataPointCount} readings",
                    fontSize = 10.sp,
                    color = PepLogTheme.colors.textSecondary.copy(alpha = 0.6f),
                    fontFamily = OutfitFontFamily
                )
            }
        }
    }
}

private fun formatValue(value: Double, type: HealthMetricType): String {
    return when (type) {
        HealthMetricType.STEPS -> String.format("%.0f", value)
        HealthMetricType.SLEEP_DURATION -> String.format("%.1f", value)
        HealthMetricType.BODY_FAT -> String.format("%.1f", value)
        HealthMetricType.WEIGHT -> String.format("%.1f", value)
        else -> String.format("%.0f", value)
    }
}

private fun isChangeGood(type: HealthMetricType, changePercent: Double): Boolean {
    return when (type) {
        HealthMetricType.SLEEP_DURATION, HealthMetricType.STEPS -> changePercent > 0
        HealthMetricType.HEART_RATE, HealthMetricType.RESTING_HEART_RATE -> changePercent < 0
        HealthMetricType.BLOOD_PRESSURE_SYSTOLIC, HealthMetricType.BLOOD_PRESSURE_DIASTOLIC -> changePercent < 0
        HealthMetricType.BODY_FAT -> changePercent < 0
        HealthMetricType.WEIGHT -> false // neutral
    }
}

fun getMetricIcon(type: HealthMetricType): ImageVector {
    return when (type) {
        HealthMetricType.WEIGHT -> Icons.Default.Scale
        HealthMetricType.SLEEP_DURATION -> Icons.Default.Bedtime
        HealthMetricType.HEART_RATE -> Icons.Default.Favorite
        HealthMetricType.RESTING_HEART_RATE -> Icons.Default.HeartBroken
        HealthMetricType.BLOOD_PRESSURE_SYSTOLIC -> Icons.Default.MonitorHeart
        HealthMetricType.BLOOD_PRESSURE_DIASTOLIC -> Icons.Default.MonitorHeart
        @Suppress("DEPRECATION")
        HealthMetricType.STEPS -> Icons.Default.DirectionsWalk
        HealthMetricType.BODY_FAT -> Icons.Default.FitnessCenter
    }
}

fun getMetricColor(type: HealthMetricType): Color {
    return when (type) {
        HealthMetricType.WEIGHT -> Color(0xFF22D3EE)       // Teal
        HealthMetricType.SLEEP_DURATION -> Color(0xFF7C83FF)  // Indigo
        HealthMetricType.HEART_RATE -> Color(0xFFF43F5E)      // Coral
        HealthMetricType.RESTING_HEART_RATE -> Color(0xFFE85D75) // Rose
        HealthMetricType.BLOOD_PRESSURE_SYSTOLIC -> Color(0xFFFBBF24) // Amber
        HealthMetricType.BLOOD_PRESSURE_DIASTOLIC -> Color(0xFFE6A050)
        HealthMetricType.STEPS -> Color(0xFF4CAF50)           // Green
        HealthMetricType.BODY_FAT -> Color(0xFFCE93D8)        // Lavender
    }
}
