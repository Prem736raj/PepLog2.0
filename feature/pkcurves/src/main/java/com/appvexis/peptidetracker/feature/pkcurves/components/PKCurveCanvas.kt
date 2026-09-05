package com.appvexis.peptidetracker.feature.pkcurves.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.appvexis.peptidetracker.feature.pkcurves.model.CompoundCurveData
import com.appvexis.peptidetracker.feature.pkcurves.model.PKMarker
import com.appvexis.peptidetracker.feature.pkcurves.model.TimeWindow

/**
 * Custom Canvas composable rendering pharmacokinetic decay curves.
 *
 * Features:
 * - Multi-compound overlay with restrained area fills
 * - Animated reveal (left-to-right curve drawing)
 * - Peak/trough markers with circles
 * - Time axis + concentration axis with gridlines
 * - Touch crosshair for reading values
 */
@Composable
fun PKCurveCanvas(
    compounds: List<CompoundCurveData>,
    timeWindow: TimeWindow,
    maxConcentration: Double,
    animationProgress: Float,
    onCrosshairUpdate: (Double?) -> Unit,
    modifier: Modifier = Modifier,
    surfaceColor: Color = Color(0xFF151D19),
    gridColor: Color = Color(0xFF2A3530),
    textColor: Color = Color(0xFFA6B0AA),
    axisColor: Color = Color(0xFF526159)
) {
    val density = LocalDensity.current
    val textPaint = remember(textColor, density) {
        android.graphics.Paint().apply {
            isAntiAlias = true
            color = textColor.toArgb()
            textSize = with(density) { 10.dp.toPx() }
            typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
        }
    }

    val markerLabelPaint = remember(textColor, density) {
        android.graphics.Paint().apply {
            isAntiAlias = true
            color = textColor.toArgb()
            textSize = with(density) { 9.dp.toPx() }
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
        }
    }

    // Crosshair state
    var crosshairX by remember { mutableStateOf<Float?>(null) }

    // Chart margins
    val leftMargin = with(density) { 48.dp.toPx() }
    val rightMargin = with(density) { 16.dp.toPx() }
    val topMargin = with(density) { 20.dp.toPx() }
    val bottomMargin = with(density) { 36.dp.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(surfaceColor)
            .padding(4.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val chartWidth = size.width - leftMargin - rightMargin
                        if (offset.x in leftMargin..(leftMargin + chartWidth)) {
                            crosshairX = offset.x
                            val normalizedX =
                                (offset.x - leftMargin) / chartWidth
                            onCrosshairUpdate(normalizedX.toDouble() * timeWindow.hours)
                        } else {
                            crosshairX = null
                            onCrosshairUpdate(null)
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            crosshairX = null
                            onCrosshairUpdate(null)
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            val chartWidth = size.width - leftMargin - rightMargin
                            val newX = (crosshairX ?: leftMargin) + dragAmount
                            if (newX in leftMargin..(leftMargin + chartWidth)) {
                                crosshairX = newX
                                val normalizedX =
                                    (newX - leftMargin) / chartWidth
                                onCrosshairUpdate(normalizedX.toDouble() * timeWindow.hours)
                            }
                        }
                    )
                }
        ) {
            val chartLeft = leftMargin
            val chartTop = topMargin
            val chartRight = size.width - rightMargin
            val chartBottom = size.height - bottomMargin
            val chartWidth = chartRight - chartLeft
            val chartHeight = chartBottom - chartTop

            if (chartWidth <= 0f || chartHeight <= 0f) return@Canvas

            val safeMaxConc = if (maxConcentration > 0.0) maxConcentration else 1.0

            // --- Grid Lines ---
            drawGridLines(
                chartLeft, chartTop, chartRight, chartBottom,
                chartWidth, chartHeight,
                gridColor, axisColor, textPaint,
                timeWindow, safeMaxConc
            )

            // --- Curve Rendering ---
            val visibleCompounds = compounds.filter { it.isVisible && it.points.isNotEmpty() }
            for (compound in visibleCompounds) {
                drawCompoundCurve(
                    compound, chartLeft, chartTop, chartWidth, chartHeight,
                    timeWindow.hours, safeMaxConc, animationProgress,
                    compound.projectionStartHours
                )
            }

            // --- Peak/Trough Markers ---
            for (compound in visibleCompounds) {
                drawMarkers(
                    compound, chartLeft, chartTop, chartWidth, chartHeight,
                    timeWindow.hours, safeMaxConc, animationProgress, markerLabelPaint
                )
            }

            // Scheduled future doses are a forecast. Mark the boundary so the
            // elimination estimate cannot be mistaken for an observed reading.
            drawNowMarker(
                chartLeft = chartLeft,
                chartTop = chartTop,
                chartBottom = chartBottom,
                chartWidth = chartWidth,
                windowHours = timeWindow.hours,
                nowHours = visibleCompounds.firstOrNull()?.projectionStartHours
                    ?: timeWindow.hours * 0.7,
                textColor = textColor,
                textPaint = textPaint
            )

            // --- Crosshair ---
            crosshairX?.let { cx ->
                if (cx in chartLeft..chartRight) {
                    // Vertical dashed line
                    drawLine(
                        color = textColor.copy(alpha = 0.55f),
                        start = Offset(cx, chartTop),
                        end = Offset(cx, chartBottom),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
                    )

                    // Intersection dots
                    val normalizedX = ((cx - chartLeft) / chartWidth).toDouble()
                    val timeAtCrosshair = normalizedX * timeWindow.hours
                    for (compound in visibleCompounds) {
                        val point = compound.points.minByOrNull {
                            kotlin.math.abs(it.timeHours - timeAtCrosshair)
                        }
                        if (point != null) {
                            val yNorm = (point.concentration / safeMaxConc).coerceIn(0.0, 1.0)
                            val y = chartBottom - (yNorm * chartHeight).toFloat()
                            drawCircle(
                                color = compound.color,
                                radius = 5f,
                                center = Offset(cx, y)
                            )
                        }
                    }
                }
            }

            // --- X-Axis Labels ---
            drawTimeAxisLabels(
                chartLeft, chartBottom, chartWidth,
                timeWindow, textPaint
            )
        }
    }
}

/**
 * Draw horizontal grid lines and Y-axis labels.
 */
private fun DrawScope.drawGridLines(
    chartLeft: Float, chartTop: Float, chartRight: Float, chartBottom: Float,
    chartWidth: Float, chartHeight: Float,
    gridColor: Color, axisColor: Color,
    textPaint: android.graphics.Paint,
    timeWindow: TimeWindow, maxConc: Double
) {
    val gridLineCount = 5

    // Horizontal grid + Y-axis labels
    for (i in 0..gridLineCount) {
        val yFraction = i.toFloat() / gridLineCount
        val y = chartTop + yFraction * chartHeight
        val concValue = maxConc * (1.0 - yFraction)

        // Grid line
        drawLine(
            color = gridColor,
            start = Offset(chartLeft, y),
            end = Offset(chartRight, y),
            strokeWidth = 0.8f
        )

        // Y-axis label
        val label = formatConcentration(concValue)
        drawContext.canvas.nativeCanvas.drawText(
            label, chartLeft - 8f, y + textPaint.textSize / 3f,
            textPaint.apply { textAlign = android.graphics.Paint.Align.RIGHT }
        )
    }

    // Vertical grid lines (time divisions)
    val vGridCount = when (timeWindow) {
        TimeWindow.HOURS_24 -> 6 // every 4h
        TimeWindow.DAYS_7 -> 7   // every day
        TimeWindow.DAYS_30 -> 6  // every 5 days
    }
    for (i in 1 until vGridCount) {
        val xFraction = i.toFloat() / vGridCount
        val x = chartLeft + xFraction * chartWidth
        drawLine(
            color = gridColor.copy(alpha = 0.5f),
            start = Offset(x, chartTop),
            end = Offset(x, chartBottom),
            strokeWidth = 0.5f
        )
    }

    // Axes
    drawLine(
        color = axisColor,
        start = Offset(chartLeft, chartBottom),
        end = Offset(chartRight, chartBottom),
        strokeWidth = 1.2f
    )
    drawLine(
        color = axisColor,
        start = Offset(chartLeft, chartTop),
        end = Offset(chartLeft, chartBottom),
        strokeWidth = 1.2f
    )
}

/**
 * Draw a single compound's curve with a quiet area fill.
 */
private fun DrawScope.drawCompoundCurve(
    compound: CompoundCurveData,
    chartLeft: Float, chartTop: Float,
    chartWidth: Float, chartHeight: Float,
    windowHours: Double, maxConc: Double,
    animationProgress: Float,
    projectionStartHours: Double
) {
    val points = compound.points
    if (points.isEmpty()) return

    val chartBottom = chartTop + chartHeight

    // Keep observed history solid and scheduled future doses dashed.
    val historyPath = Path()
    val forecastPath = Path()
    val fillPath = Path()
    var historyStarted = false
    var forecastStarted = false
    var lastHistoryX = 0f
    var lastHistoryY = 0f

    val animatedPointCount = (points.size * animationProgress).toInt()
        .coerceIn(1, points.size)

    for (i in 0 until animatedPointCount) {
        val pt = points[i]
        val xNorm = (pt.timeHours / windowHours).coerceIn(0.0, 1.0)
        val yNorm = (pt.concentration / maxConc).coerceIn(0.0, 1.0)

        val x = chartLeft + (xNorm * chartWidth).toFloat()
        val y = chartBottom - (yNorm * chartHeight).toFloat()

        if (pt.timeHours <= projectionStartHours) {
            if (!historyStarted) {
                historyPath.moveTo(x, y)
                fillPath.moveTo(x, chartBottom) // start fill from bottom
                fillPath.lineTo(x, y)
                historyStarted = true
            } else {
                historyPath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
            lastHistoryX = x
            lastHistoryY = y
        } else {
            if (!forecastStarted) {
                if (historyStarted) forecastPath.moveTo(lastHistoryX, lastHistoryY)
                else forecastPath.moveTo(x, y)
                forecastStarted = true
            }
            forecastPath.lineTo(x, y)
        }
    }

    // Fill only the observed portion; the forecast remains visually lighter.
    if (historyStarted) {
        fillPath.lineTo(lastHistoryX, chartBottom)
        fillPath.close()
    }

    if (historyStarted) {
        drawPath(path = fillPath, color = compound.color.copy(alpha = 0.08f))
        drawPath(
            path = historyPath,
            color = compound.color,
            style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }

    if (forecastStarted) {
        drawPath(
            path = forecastPath,
            color = compound.color.copy(alpha = 0.72f),
            style = Stroke(
                width = 2.5f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
            )
        )
    }
}

private fun DrawScope.drawNowMarker(
    chartLeft: Float,
    chartTop: Float,
    chartBottom: Float,
    chartWidth: Float,
    windowHours: Double,
    nowHours: Double,
    textColor: Color,
    textPaint: android.graphics.Paint
) {
    val x = chartLeft + (nowHours / windowHours).coerceIn(0.0, 1.0).toFloat() * chartWidth
    drawLine(
        color = textColor.copy(alpha = 0.7f),
        start = Offset(x, chartTop),
        end = Offset(x, chartBottom),
        strokeWidth = 1.4f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
    )
    val labelPaint = android.graphics.Paint(textPaint).apply {
        color = textColor.toArgb()
        textAlign = android.graphics.Paint.Align.CENTER
    }
    drawContext.canvas.nativeCanvas.drawText("Now", x, chartTop - 5f, labelPaint)
}

/**
 * Draw peak/trough markers as small circles with labels.
 */
private fun DrawScope.drawMarkers(
    compound: CompoundCurveData,
    chartLeft: Float, chartTop: Float,
    chartWidth: Float, chartHeight: Float,
    windowHours: Double, maxConc: Double,
    animationProgress: Float,
    labelPaint: android.graphics.Paint
) {
    val chartBottom = chartTop + chartHeight
    val animatedTimeLimit = windowHours * animationProgress

    for (marker in compound.markers) {
        if (marker.timeHours > animatedTimeLimit) continue

        val xNorm = (marker.timeHours / windowHours).coerceIn(0.0, 1.0)
        val yNorm = (marker.concentration / maxConc).coerceIn(0.0, 1.0)
        val x = chartLeft + (xNorm * chartWidth).toFloat()
        val y = chartBottom - (yNorm * chartHeight).toFloat()

        // Inner filled circle
        drawCircle(
            color = if (marker.isPeak) compound.color else Color(0xFFC66F52),
            radius = 4.5f,
            center = Offset(x, y)
        )

        // Small triangle indicator (peak = up, trough = down)
        val triSize = 5f
        val triPath = Path().apply {
            if (marker.isPeak) {
                moveTo(x, y - 14f)
                lineTo(x - triSize, y - 14f + triSize)
                lineTo(x + triSize, y - 14f + triSize)
            } else {
                moveTo(x, y + 14f)
                lineTo(x - triSize, y + 14f - triSize)
                lineTo(x + triSize, y + 14f - triSize)
            }
            close()
        }
        drawPath(
            path = triPath,
            color = if (marker.isPeak) compound.color else Color(0xFFC66F52)
        )
    }
}

/**
 * Draw time labels along the X-axis.
 */
private fun DrawScope.drawTimeAxisLabels(
    chartLeft: Float, chartBottom: Float,
    chartWidth: Float,
    timeWindow: TimeWindow,
    textPaint: android.graphics.Paint
) {
    val labelCount = when (timeWindow) {
        TimeWindow.HOURS_24 -> 7  // 0, 4, 8, 12, 16, 20, 24
        TimeWindow.DAYS_7 -> 8    // 0, 1, 2, 3, 4, 5, 6, 7
        TimeWindow.DAYS_30 -> 7   // 0, 5, 10, 15, 20, 25, 30
    }

    val paint = android.graphics.Paint(textPaint).apply {
        textAlign = android.graphics.Paint.Align.CENTER
    }

    for (i in 0 until labelCount) {
        val fraction = i.toFloat() / (labelCount - 1)
        val x = chartLeft + fraction * chartWidth
        val timeValue = fraction * timeWindow.hours

        val label = when (timeWindow) {
            TimeWindow.HOURS_24 -> "${timeValue.toInt()}h"
            TimeWindow.DAYS_7 -> "D${(timeValue / 24.0).toInt()}"
            TimeWindow.DAYS_30 -> "D${(timeValue / 24.0).toInt()}"
        }

        drawContext.canvas.nativeCanvas.drawText(
            label, x, chartBottom + paint.textSize + 6f, paint
        )
    }
}

/**
 * Format concentration value for Y-axis display.
 */
private fun formatConcentration(value: Double): String {
    return when {
        value >= 100.0 -> "${value.toInt()}"
        value >= 10.0 -> String.format("%.0f", value)
        value >= 1.0 -> String.format("%.1f", value)
        value >= 0.01 -> String.format("%.2f", value)
        else -> "0"
    }
}
