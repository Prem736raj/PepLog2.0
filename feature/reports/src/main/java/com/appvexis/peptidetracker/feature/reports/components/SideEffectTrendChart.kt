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
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Sick
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import com.appvexis.peptidetracker.feature.reports.model.DailySeverityPoint
import com.appvexis.peptidetracker.feature.reports.model.SideEffectCategoryCount
import com.appvexis.peptidetracker.feature.reports.model.SideEffectSummaryUiModel

/**
 * Side Effects tab view showing severity curves, category frequencies, and history.
 */
@Composable
fun SideEffectsTrendsView(
    sideEffectsData: SideEffectSummaryUiModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.medium)
    ) {
        // Top Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.small)
        ) {
            // Most Common Symptom
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
                                imageVector = Icons.Default.Sick,
                                contentDescription = null,
                                tint = PepLogTheme.colors.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Top Symptom",
                            style = MaterialTheme.typography.labelMedium,
                            color = PepLogTheme.colors.textSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = sideEffectsData.mostCommon?.replaceFirstChar { it.uppercase() } ?: "None",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = PepLogTheme.colors.textPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = "${sideEffectsData.totalEvents} total log entries",
                        style = MaterialTheme.typography.bodySmall,
                        color = PepLogTheme.colors.textSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Average Severity Card
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
                                .background(PepLogTheme.colors.accent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = PepLogTheme.colors.accent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Avg Severity",
                            style = MaterialTheme.typography.labelMedium,
                            color = PepLogTheme.colors.textSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${String.format("%.1f", sideEffectsData.avgSeverity)} / 10",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (sideEffectsData.avgSeverity <= 3.0)
                            PepLogTheme.colors.primary
                        else if (sideEffectsData.avgSeverity <= 6.0)
                            PepLogTheme.colors.secondary
                        else
                            PepLogTheme.colors.accent
                    )
                    Text(
                        text = when {
                            sideEffectsData.avgSeverity == 0.0 -> "No side effects"
                            sideEffectsData.avgSeverity <= 3.0 -> "Mild discomfort"
                            sideEffectsData.avgSeverity <= 6.0 -> "Moderate discomfort"
                            else -> "Severe — monitor closely"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = PepLogTheme.colors.textSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Severity Trend Canvas Chart
        PepLogCard(
            modifier = Modifier.fillMaxWidth(),
            isGlassmorphic = true
        ) {
            Column(
                modifier = Modifier.padding(PepLogTheme.spacing.medium)
            ) {
                Text(
                    text = "Symptom Severity Timeline",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = PepLogTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Daily average severity score (1 = Mild, 10 = Severe)",
                    style = MaterialTheme.typography.bodySmall,
                    color = PepLogTheme.colors.textSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (sideEffectsData.dailySeverityPoints.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No side effects reported in this range",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PepLogTheme.colors.textSecondary
                        )
                    }
                } else {
                    SideEffectSeverityCanvas(
                        points = sideEffectsData.dailySeverityPoints,
                        accentColor = PepLogTheme.colors.accent,
                        surfaceHigh = PepLogTheme.colors.surfaceHigh,
                        textColor = PepLogTheme.colors.textSecondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
            }
        }

        // Category Frequency Distribution
        if (sideEffectsData.categoryCounts.isNotEmpty()) {
            PepLogCard(
                modifier = Modifier.fillMaxWidth(),
                isGlassmorphic = false
            ) {
                Column(
                    modifier = Modifier.padding(PepLogTheme.spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Symptom Distribution",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = PepLogTheme.colors.textPrimary
                    )

                    sideEffectsData.categoryCounts.forEach { item ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.categoryName.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PepLogTheme.colors.textPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${item.count} times (${String.format("%.0f", item.percentage)}%)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PepLogTheme.colors.textSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (item.percentage / 100.0).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = item.color,
                                trackColor = PepLogTheme.colors.surfaceHigh
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Custom smooth curve Canvas for side effect severity.
 */
@Composable
private fun SideEffectSeverityCanvas(
    points: List<DailySeverityPoint>,
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

        val maxSeverity = 10f
        val step = if (points.size > 1) chartWidth / (points.size - 1) else chartWidth

        // Draw horizontal grid lines (0, 5, 10)
        for (i in 0..2) {
            val yFraction = i / 2f
            val y = topPadding + (yFraction * chartHeight)
            drawLine(
                color = surfaceHigh.copy(alpha = 0.5f),
                start = Offset(0f, y),
                end = Offset(chartWidth, y),
                strokeWidth = 0.8f
            )
        }

        if (points.isNotEmpty()) {
            val linePath = Path()
            val fillPath = Path()
            var first = true

            val animatedPointsCount = (points.size * animProgress.value).toInt().coerceAtLeast(1)

            for (i in 0 until animatedPointsCount) {
                val pt = points[i]
                val x = if (points.size > 1) i * step else chartWidth / 2f
                val y = topPadding + chartHeight - ((pt.avgSeverity.toFloat() / maxSeverity) * chartHeight)

                if (first) {
                    linePath.moveTo(x, y)
                    fillPath.moveTo(x, size.height - bottomPadding)
                    fillPath.lineTo(x, y)
                    first = false
                } else {
                    linePath.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
            }

            if (animatedPointsCount > 0) {
                val lastX = if (points.size > 1) (animatedPointsCount - 1) * step else chartWidth / 2f
                fillPath.lineTo(lastX, size.height - bottomPadding)
                fillPath.close()

                // Gradient fill
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.25f),
                            accentColor.copy(alpha = 0.02f)
                        ),
                        startY = topPadding,
                        endY = size.height - bottomPadding
                    )
                )

                // Line stroke
                drawPath(
                    path = linePath,
                    color = accentColor,
                    style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Draw dots on each point
                for (i in 0 until animatedPointsCount) {
                    val pt = points[i]
                    val x = if (points.size > 1) i * step else chartWidth / 2f
                    val y = topPadding + chartHeight - ((pt.avgSeverity.toFloat() / maxSeverity) * chartHeight)
                    drawCircle(color = accentColor, radius = 4f, center = Offset(x, y))
                }
            }

            // Draw date labels
            points.forEachIndexed { i, pt ->
                val showLabel = when {
                    points.size <= 7 -> true
                    points.size <= 14 -> i % 2 == 0
                    else -> i % 5 == 0
                }
                if (showLabel) {
                    val x = if (points.size > 1) i * step else chartWidth / 2f
                    drawContext.canvas.nativeCanvas.drawText(
                        pt.dayLabel,
                        x,
                        size.height - 4.dp.toPx(),
                        textPaint
                    )
                }
            }
        }
    }
}
