package com.appvexis.peptidetracker.feature.reports.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import com.appvexis.peptidetracker.feature.reports.model.AdherenceSummaryUiModel
import com.appvexis.peptidetracker.feature.reports.model.DailyAdherencePoint

/**
 * Adherence tab view featuring metric highlights and an interactive daily adherence bar chart.
 */
@Composable
fun AdherenceDashboardView(
    adherenceData: AdherenceSummaryUiModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.medium)
    ) {
        // Summary Cards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.small)
        ) {
            // Adherence Rate Card
            PepLogCard(
                modifier = Modifier.weight(1f),
                isGlassmorphic = false
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(PepLogTheme.colors.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = PepLogTheme.colors.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Adherence",
                            style = MaterialTheme.typography.labelMedium,
                            color = PepLogTheme.colors.textSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${String.format("%.1f", adherenceData.overallPercentage)}%",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = if (adherenceData.overallPercentage >= 80.0)
                            PepLogTheme.colors.primary
                        else
                            PepLogTheme.colors.secondary
                    )
                    Text(
                        text = "${adherenceData.totalTaken} of ${adherenceData.totalScheduled} doses",
                        style = MaterialTheme.typography.bodySmall,
                        color = PepLogTheme.colors.textSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Streak Card
            PepLogCard(
                modifier = Modifier.weight(1f),
                isGlassmorphic = false
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(PepLogTheme.colors.secondary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = PepLogTheme.colors.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Streak",
                            style = MaterialTheme.typography.labelMedium,
                            color = PepLogTheme.colors.textSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${adherenceData.currentStreakDays} Days",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = PepLogTheme.colors.secondary
                    )
                    Text(
                        text = if (adherenceData.currentStreakDays > 0) "Active on-time streak" else "Log today's dose",
                        style = MaterialTheme.typography.bodySmall,
                        color = PepLogTheme.colors.textSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Daily Adherence Bar Chart Card
        PepLogCard(
            modifier = Modifier.fillMaxWidth(),
            isGlassmorphic = true
        ) {
            Column(
                modifier = Modifier.padding(PepLogTheme.spacing.medium)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily Dose Adherence",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = PepLogTheme.colors.textPrimary
                    )
                    // Legend
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(PepLogTheme.colors.primary)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Taken", fontSize = 11.sp, color = PepLogTheme.colors.textSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(PepLogTheme.colors.accent)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Missed", fontSize = 11.sp, color = PepLogTheme.colors.textSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (adherenceData.dailyPoints.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No scheduled dose data available for this range",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PepLogTheme.colors.textSecondary
                        )
                    }
                } else {
                    DailyAdherenceBarCanvas(
                        points = adherenceData.dailyPoints,
                        primaryColor = PepLogTheme.colors.primary,
                        accentColor = PepLogTheme.colors.accent,
                        surfaceHigh = PepLogTheme.colors.surfaceHigh,
                        textColor = PepLogTheme.colors.textSecondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
            }
        }
    }
}

/**
 * Custom Canvas drawing daily adherence bars with animations.
 */
@Composable
private fun DailyAdherenceBarCanvas(
    points: List<DailyAdherencePoint>,
    primaryColor: Color,
    accentColor: Color,
    surfaceHigh: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, tween(1000, easing = FastOutSlowInEasing))
    }

    val density = LocalDensity.current
    val textPaint = remember(textColor, density) {
        android.graphics.Paint().apply {
            isAntiAlias = true
            color = textColor.toArgb()
            textSize = with(density) { 10.dp.toPx() }
            typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
            textAlign = android.graphics.Paint.Align.CENTER
        }
    }

    Canvas(modifier = modifier) {
        val bottomPadding = 24.dp.toPx()
        val topPadding = 12.dp.toPx()
        val chartHeight = size.height - bottomPadding - topPadding
        val chartWidth = size.width

        val maxDoses = (points.maxOfOrNull { it.scheduled } ?: 1).coerceAtLeast(1)
        val barCount = points.size
        val totalSpacing = chartWidth * 0.35f
        val barWidth = ((chartWidth - totalSpacing) / barCount).coerceIn(8f, 32f)
        val step = chartWidth / barCount

        // Draw baseline
        drawLine(
            color = surfaceHigh,
            start = Offset(0f, size.height - bottomPadding),
            end = Offset(chartWidth, size.height - bottomPadding),
            strokeWidth = 1.5f
        )

        points.forEachIndexed { index, pt ->
            val centerX = (index * step) + (step / 2f)
            val barLeft = centerX - (barWidth / 2f)
            val barRight = centerX + (barWidth / 2f)

            // Scheduled height background bar
            val totalFrac = (pt.scheduled.toFloat() / maxDoses.toFloat()) * animProgress.value
            val totalBarHeight = (totalFrac * chartHeight).coerceAtLeast(4f)
            val totalBarTop = size.height - bottomPadding - totalBarHeight

            // Background scheduled slot
            drawRoundRect(
                color = surfaceHigh.copy(alpha = 0.6f),
                topLeft = Offset(barLeft, totalBarTop),
                size = Size(barWidth, totalBarHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            // Taken bar portion
            if (pt.taken > 0) {
                val takenFrac = (pt.taken.toFloat() / maxDoses.toFloat()) * animProgress.value
                val takenHeight = (takenFrac * chartHeight).coerceAtLeast(4f)
                val takenTop = size.height - bottomPadding - takenHeight

                drawRoundRect(
                    color = primaryColor,
                    topLeft = Offset(barLeft, takenTop),
                    size = Size(barWidth, takenHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }

            // Missed indicator
            if (pt.missed > 0 && pt.taken == 0) {
                drawRoundRect(
                    color = accentColor.copy(alpha = 0.8f),
                    topLeft = Offset(barLeft, totalBarTop),
                    size = Size(barWidth, totalBarHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }

            // Date label (show every Nth label to avoid overlap)
            val showLabel = when {
                barCount <= 7 -> true
                barCount <= 14 -> index % 2 == 0
                barCount <= 30 -> index % 5 == 0
                else -> index % 10 == 0
            }

            if (showLabel) {
                drawContext.canvas.nativeCanvas.drawText(
                    pt.dayLabel,
                    centerX,
                    size.height - 4.dp.toPx(),
                    textPaint
                )
            }
        }
    }
}
