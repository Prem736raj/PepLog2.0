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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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
import com.appvexis.peptidetracker.feature.reports.model.DailyWellnessPoint
import com.appvexis.peptidetracker.feature.reports.model.WellnessTrendUiModel

/**
 * Subjective wellness view tracking Mood, Energy, Sleep, Pain, and Libido trajectories.
 */
@Composable
fun WellnessTrendsView(
    wellnessData: WellnessTrendUiModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.medium)
    ) {
        // Metric Score Cards Grid
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Row 1: Mood & Energy
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WellnessScoreCard(
                    title = "Mood",
                    score = wellnessData.moodScore,
                    delta = wellnessData.moodDelta,
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
                WellnessScoreCard(
                    title = "Energy",
                    score = wellnessData.energyScore,
                    delta = wellnessData.energyDelta,
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 2: Sleep & Pain
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WellnessScoreCard(
                    title = "Sleep Quality",
                    score = wellnessData.sleepScore,
                    delta = wellnessData.sleepDelta,
                    color = Color(0xFF3B82F6),
                    modifier = Modifier.weight(1f)
                )
                WellnessScoreCard(
                    title = "Pain Level",
                    score = wellnessData.painScore,
                    delta = wellnessData.painDelta,
                    color = Color(0xFFF43F5E),
                    isPain = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 3: Libido
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WellnessScoreCard(
                    title = "Libido",
                    score = wellnessData.libidoScore,
                    delta = wellnessData.libidoDelta,
                    color = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Multi-Metric Wellness Canvas Chart
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
                        text = "Wellness Trajectory",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = PepLogTheme.colors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Daily subjective ratings (1 = Low, 10 = High)",
                    style = MaterialTheme.typography.bodySmall,
                    color = PepLogTheme.colors.textSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Chart Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendDot(name = "Mood", color = Color(0xFF10B981))
                    LegendDot(name = "Energy", color = Color(0xFFF59E0B))
                    LegendDot(name = "Sleep", color = Color(0xFF3B82F6))
                    LegendDot(name = "Pain", color = Color(0xFFF43F5E))
                    LegendDot(name = "Libido", color = Color(0xFF8B5CF6))
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (wellnessData.dailyTrends.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No daily wellness logs available for this range",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PepLogTheme.colors.textSecondary
                        )
                    }
                } else {
                    WellnessMultiLineCanvas(
                        points = wellnessData.dailyTrends,
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

@Composable
private fun LegendDot(name: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(name, fontSize = 11.sp, color = PepLogTheme.colors.textSecondary)
    }
}

@Composable
private fun WellnessScoreCard(
    title: String,
    score: Double,
    delta: Double,
    color: Color,
    modifier: Modifier = Modifier,
    isPain: Boolean = false
) {
    PepLogCard(
        modifier = modifier,
        isGlassmorphic = false
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = PepLogTheme.colors.textSecondary
                )
                // Delta Tag
                val isPositiveImprovement = if (isPain) delta <= 0.0 else delta >= 0.0
                val deltaColor = if (isPositiveImprovement) PepLogTheme.colors.primary else PepLogTheme.colors.accent
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(deltaColor.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (delta >= 0.0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = deltaColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${if (delta >= 0) "+" else ""}${String.format("%.1f", delta)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = deltaColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${String.format("%.1f", score)} / 10",
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = color
            )
        }
    }
}

@Composable
private fun WellnessMultiLineCanvas(
    points: List<DailyWellnessPoint>,
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

        val maxScore = 10f
        val step = if (points.size > 1) chartWidth / (points.size - 1) else chartWidth

        // Draw horizontal grid lines
        for (i in 0..2) {
            val yFrac = i / 2f
            val y = topPadding + (yFrac * chartHeight)
            drawLine(
                color = surfaceHigh.copy(alpha = 0.5f),
                start = Offset(0f, y),
                end = Offset(chartWidth, y),
                strokeWidth = 0.8f
            )
        }

        val animatedCount = (points.size * animProgress.value).toInt().coerceAtLeast(1)

        // Helper to draw a single metric line
        fun drawMetricLine(values: List<Double?>, color: Color) {
            val path = Path()
            var started = false

            for (i in 0 until animatedCount) {
                val scoreVal = values.getOrNull(i) ?: continue
                val x = if (points.size > 1) i * step else chartWidth / 2f
                val y = topPadding + chartHeight - ((scoreVal.toFloat() / maxScore) * chartHeight)

                if (!started) {
                    path.moveTo(x, y)
                    started = true
                } else {
                    path.lineTo(x, y)
                }
            }

            if (started) {
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = 2.2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Dots
                for (i in 0 until animatedCount) {
                    val scoreVal = values.getOrNull(i) ?: continue
                    val x = if (points.size > 1) i * step else chartWidth / 2f
                    val y = topPadding + chartHeight - ((scoreVal.toFloat() / maxScore) * chartHeight)
                    drawCircle(color = color, radius = 3.5f, center = Offset(x, y))
                }
            }
        }

        // Draw lines for each metric
        drawMetricLine(points.map { it.mood }, Color(0xFF10B981))
        drawMetricLine(points.map { it.energy }, Color(0xFFF59E0B))
        drawMetricLine(points.map { it.sleep }, Color(0xFF3B82F6))
        drawMetricLine(points.map { it.pain }, Color(0xFFF43F5E))
        drawMetricLine(points.map { it.libido }, Color(0xFF8B5CF6))

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
