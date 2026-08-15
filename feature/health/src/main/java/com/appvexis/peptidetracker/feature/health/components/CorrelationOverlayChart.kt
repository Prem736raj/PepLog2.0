package com.appvexis.peptidetracker.feature.health.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import com.appvexis.peptidetracker.feature.health.model.CorrelationDataPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Dual-axis correlation overlay chart.
 * Shows health metric values as a smooth curve with dose markers overlaid as vertical indicator lines.
 * Canvas-based with animated reveal.
 */
@Composable
fun CorrelationOverlayChart(
    dataPoints: List<CorrelationDataPoint>,
    doseMarkers: List<Long>,
    metricUnit: String,
    metricName: String,
    modifier: Modifier = Modifier,
    curveColor: Color = PepLogTheme.colors.primary,
    doseMarkerColor: Color = PepLogTheme.colors.secondary,
    areaGradientAlpha: Float = 0.15f
) {
    if (dataPoints.isEmpty()) {
        EmptyChartState(metricName, modifier)
        return
    }

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(dataPoints) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 1200))
    }

    val textColor = PepLogTheme.colors.textSecondary
    val gridColor = PepLogTheme.colors.textSecondary.copy(alpha = 0.1f)

    Column(modifier = modifier.fillMaxWidth()) {
        // Chart header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = metricName,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = PepLogTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.weight(1f))
            // Legend
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .padding(0.dp)
                ) {
                    Canvas(modifier = Modifier.size(8.dp)) {
                        drawCircle(color = curveColor, radius = 4.dp.toPx())
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = metricUnit,
                    fontSize = 11.sp,
                    color = textColor,
                    fontFamily = OutfitFontFamily
                )
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                ) {
                    Canvas(modifier = Modifier.size(8.dp)) {
                        drawCircle(color = doseMarkerColor, radius = 4.dp.toPx())
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Doses",
                    fontSize = 11.sp,
                    color = textColor,
                    fontFamily = OutfitFontFamily
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            val chartWidth = size.width
            val chartHeight = size.height
            val paddingLeft = 48f
            val paddingBottom = 32f
            val paddingTop = 16f
            val drawWidth = chartWidth - paddingLeft
            val drawHeight = chartHeight - paddingBottom - paddingTop

            // Calculate bounds
            val minValue = dataPoints.minOf { it.healthValue }
            val maxValue = dataPoints.maxOf { it.healthValue }
            val valueRange = (maxValue - minValue).let { if (it == 0.0) 1.0 else it }
            val minTime = dataPoints.minOf { it.timestamp }
            val maxTime = dataPoints.maxOf { it.timestamp }
            val timeRange = (maxTime - minTime).let { if (it == 0L) 1L else it }

            // Draw horizontal grid lines
            val gridLineCount = 4
            for (i in 0..gridLineCount) {
                val y = paddingTop + drawHeight - (drawHeight * i / gridLineCount)
                drawLine(
                    color = gridColor,
                    start = Offset(paddingLeft, y),
                    end = Offset(chartWidth, y),
                    strokeWidth = 1f
                )
            }

            // Draw Y-axis labels
            for (i in 0..gridLineCount) {
                val value = minValue + (valueRange * i / gridLineCount)
                val y = paddingTop + drawHeight - (drawHeight * i / gridLineCount)
                drawContext.canvas.nativeCanvas.drawText(
                    String.format("%.0f", value),
                    8f,
                    y + 4f,
                    android.graphics.Paint().apply {
                        color = textColor.hashCode()
                        textSize = 24f
                        isAntiAlias = true
                    }
                )
            }

            // Calculate animated point positions
            val animatedPoints = dataPoints.mapIndexed { index, dp ->
                val x = paddingLeft + ((dp.timestamp - minTime).toFloat() / timeRange) * drawWidth
                val y = paddingTop + drawHeight - ((dp.healthValue - minValue).toFloat() / valueRange.toFloat()) * drawHeight
                Offset(x, y)
            }

            val visibleCount = (animatedPoints.size * animationProgress.value).toInt()
                .coerceAtLeast(1)
                .coerceAtMost(animatedPoints.size)

            val visiblePoints = animatedPoints.take(visibleCount)

            // Draw dose marker lines (vertical dashed lines)
            val allTimestamps = doseMarkers.filter { it in minTime..maxTime }
            for (doseTime in allTimestamps) {
                val x = paddingLeft + ((doseTime - minTime).toFloat() / timeRange) * drawWidth
                drawLine(
                    color = doseMarkerColor.copy(alpha = 0.4f * animationProgress.value),
                    start = Offset(x, paddingTop),
                    end = Offset(x, paddingTop + drawHeight),
                    strokeWidth = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
                )
                // Small dose dot at bottom
                drawCircle(
                    color = doseMarkerColor.copy(alpha = 0.7f * animationProgress.value),
                    radius = 3.dp.toPx(),
                    center = Offset(x, paddingTop + drawHeight + 8f)
                )
            }

            // Draw area gradient under curve
            if (visiblePoints.size >= 2) {
                val areaPath = Path().apply {
                    moveTo(visiblePoints.first().x, paddingTop + drawHeight)
                    for (i in visiblePoints.indices) {
                        if (i == 0) {
                            lineTo(visiblePoints[i].x, visiblePoints[i].y)
                        } else {
                            val prev = visiblePoints[i - 1]
                            val curr = visiblePoints[i]
                            val cx1 = (prev.x + curr.x) / 2f
                            cubicTo(cx1, prev.y, cx1, curr.y, curr.x, curr.y)
                        }
                    }
                    lineTo(visiblePoints.last().x, paddingTop + drawHeight)
                    close()
                }

                drawPath(
                    path = areaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            curveColor.copy(alpha = areaGradientAlpha * animationProgress.value),
                            Color.Transparent
                        ),
                        startY = paddingTop,
                        endY = paddingTop + drawHeight
                    )
                )
            }

            // Draw the smooth curve line
            if (visiblePoints.size >= 2) {
                val curvePath = Path().apply {
                    moveTo(visiblePoints.first().x, visiblePoints.first().y)
                    for (i in 1 until visiblePoints.size) {
                        val prev = visiblePoints[i - 1]
                        val curr = visiblePoints[i]
                        val cx1 = (prev.x + curr.x) / 2f
                        cubicTo(cx1, prev.y, cx1, curr.y, curr.x, curr.y)
                    }
                }

                drawPath(
                    path = curvePath,
                    color = curveColor.copy(alpha = animationProgress.value),
                    style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                )
            }

            // Draw data point dots
            for (point in visiblePoints) {
                drawCircle(
                    color = curveColor.copy(alpha = animationProgress.value),
                    radius = 3.dp.toPx(),
                    center = point
                )
                // Glow effect
                drawCircle(
                    color = curveColor.copy(alpha = 0.15f * animationProgress.value),
                    radius = 6.dp.toPx(),
                    center = point
                )
            }

            // Highlight dose-day data points with outer ring
            for ((index, dp) in dataPoints.take(visibleCount).withIndex()) {
                if (dp.hasDose) {
                    drawCircle(
                        color = doseMarkerColor.copy(alpha = 0.5f * animationProgress.value),
                        radius = 5.dp.toPx(),
                        center = visiblePoints[index],
                        style = Stroke(width = 1.5f)
                    )
                }
            }

            // Draw X-axis time labels
            drawXAxisLabels(
                minTime = minTime,
                maxTime = maxTime,
                paddingLeft = paddingLeft,
                drawWidth = drawWidth,
                y = chartHeight - 4f,
                textColor = textColor
            )
        }
    }
}

/**
 * Draw time labels on the X-axis.
 */
private fun DrawScope.drawXAxisLabels(
    minTime: Long,
    maxTime: Long,
    paddingLeft: Float,
    drawWidth: Float,
    y: Float,
    textColor: Color
) {
    val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
    val labelCount = 5
    val timeRange = maxTime - minTime

    for (i in 0 until labelCount) {
        val time = minTime + (timeRange * i / (labelCount - 1))
        val x = paddingLeft + (drawWidth * i / (labelCount - 1))
        drawContext.canvas.nativeCanvas.drawText(
            dateFormat.format(Date(time)),
            x - 20f,
            y,
            android.graphics.Paint().apply {
                color = textColor.hashCode()
                textSize = 22f
                isAntiAlias = true
            }
        )
    }
}

/**
 * Empty state when no data is available for the selected metric.
 */
@Composable
private fun EmptyChartState(metricName: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No $metricName Data",
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = PepLogTheme.colors.textSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Sync from Health Connect to see charts",
                fontSize = 12.sp,
                color = PepLogTheme.colors.textSecondary.copy(alpha = 0.7f),
                fontFamily = OutfitFontFamily
            )
        }
    }
}
