package com.appvexis.peptidetracker.feature.reports.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Biotech
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.components.PepLogChip
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import com.appvexis.peptidetracker.feature.reports.model.BiomarkerPoint
import com.appvexis.peptidetracker.feature.reports.model.BiomarkerReportsUiModel
import com.appvexis.peptidetracker.feature.reports.model.BiomarkerSeriesUiModel

/**
 * Biomarkers tab view plotting lab values over time with baseline comparisons and reference bands.
 */
@Composable
fun BiomarkersTrendsView(
    biomarkersData: BiomarkerReportsUiModel,
    onSelectBiomarker: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.medium)
    ) {
        if (biomarkersData.availableBiomarkers.isEmpty()) {
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
                            imageVector = Icons.Default.Biotech,
                            contentDescription = null,
                            tint = PepLogTheme.colors.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Biomarker Labs Logged",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = PepLogTheme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Log lab results in the Progress tab to visualize biomarker trends across your cycle stacks.",
                            style = MaterialTheme.typography.bodySmall,
                            color = PepLogTheme.colors.textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Biomarker Selector Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                biomarkersData.availableBiomarkers.forEach { biomarker ->
                    PepLogChip(
                        text = biomarker,
                        selected = biomarker == biomarkersData.selectedBiomarker,
                        onClick = { onSelectBiomarker(biomarker) }
                    )
                }
            }

            // Active Series Summary Card + Chart
            val active = biomarkersData.activeSeries
            if (active != null) {
                PepLogCard(
                    modifier = Modifier.fillMaxWidth(),
                    isGlassmorphic = true
                ) {
                    Column(
                        modifier = Modifier.padding(PepLogTheme.spacing.medium)
                    ) {
                        // Title & Delta Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = active.biomarkerName,
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = PepLogTheme.colors.textPrimary
                                )
                                Text(
                                    text = "Baseline: ${active.baselineValue} ${active.unit} → Current: ${active.currentValue} ${active.unit}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PepLogTheme.colors.textSecondary
                                )
                            }

                            // Delta Badge
                            val isPositive = active.deltaValue >= 0.0
                            val deltaColor = if (isPositive) PepLogTheme.colors.primary else PepLogTheme.colors.secondary
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(deltaColor.copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = deltaColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${if (isPositive) "+" else ""}${String.format("%.1f", active.deltaValue)} ${active.unit} (${if (isPositive) "+" else ""}${String.format("%.1f", active.deltaPercentage)}%)",
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = deltaColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Biomarker Canvas Chart
                        BiomarkerCurveCanvas(
                            series = active,
                            primaryColor = PepLogTheme.colors.primary,
                            surfaceHigh = PepLogTheme.colors.surfaceHigh,
                            textColor = PepLogTheme.colors.textSecondary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                }
            }

            // All Biomarkers Overview Cards
            if (biomarkersData.allBiomarkerDeltas.isNotEmpty()) {
                PepLogCard(
                    modifier = Modifier.fillMaxWidth(),
                    isGlassmorphic = false
                ) {
                    Column(
                        modifier = Modifier.padding(PepLogTheme.spacing.medium),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "All Tracked Biomarkers",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = PepLogTheme.colors.textPrimary
                        )

                        biomarkersData.allBiomarkerDeltas.forEach { deltaItem ->
                            val isPos = deltaItem.delta >= 0.0
                            val badgeColor = if (isPos) PepLogTheme.colors.primary else PepLogTheme.colors.secondary
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(PepLogTheme.colors.surfaceHigh.copy(alpha = 0.5f))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = deltaItem.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = PepLogTheme.colors.textPrimary
                                    )
                                    Text(
                                        text = "${deltaItem.startValue} → ${deltaItem.currentValue} ${deltaItem.unit}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PepLogTheme.colors.textSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                                Text(
                                    text = "${if (isPos) "+" else ""}${String.format("%.1f", deltaItem.delta)} (${if (isPos) "+" else ""}${String.format("%.1f", deltaItem.deltaPercent)}%)",
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = badgeColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Custom Canvas drawing smooth biomarker trajectory curve.
 */
@Composable
private fun BiomarkerCurveCanvas(
    series: BiomarkerSeriesUiModel,
    primaryColor: Color,
    surfaceHigh: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(series) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, tween(1000, easing = FastOutSlowInEasing))
    }

    val density = LocalDensity.current
    val textPaint = remember {
        android.graphics.Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.parseColor("#8888A0")
            textSize = with(density) { 10.dp.toPx() }
            typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
            textAlign = android.graphics.Paint.Align.CENTER
        }
    }

    Canvas(modifier = modifier) {
        val points = series.points
        val bottomPadding = 24.dp.toPx()
        val topPadding = 16.dp.toPx()
        val chartHeight = size.height - bottomPadding - topPadding
        val chartWidth = size.width

        val minVal = (points.minOfOrNull { it.value } ?: 0.0) * 0.9
        val maxVal = ((points.maxOfOrNull { it.value } ?: 100.0) * 1.1).coerceAtLeast(minVal + 1.0)
        val valRange = (maxVal - minVal).toFloat()

        val step = if (points.size > 1) chartWidth / (points.size - 1) else chartWidth

        // Draw horizontal grid lines
        for (i in 0..3) {
            val yFrac = i / 3f
            val y = topPadding + (yFrac * chartHeight)
            drawLine(
                color = surfaceHigh.copy(alpha = 0.5f),
                start = Offset(0f, y),
                end = Offset(chartWidth, y),
                strokeWidth = 0.8f
            )
        }

        // Draw baseline dashed line
        val baselineNorm = ((series.baselineValue - minVal) / valRange).toFloat().coerceIn(0f, 1f)
        val baselineY = topPadding + chartHeight - (baselineNorm * chartHeight)
        drawLine(
            color = textColor.copy(alpha = 0.4f),
            start = Offset(0f, baselineY),
            end = Offset(chartWidth, baselineY),
            strokeWidth = 1f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
        )

        if (points.isNotEmpty()) {
            val linePath = Path()
            val fillPath = Path()
            var first = true

            val animatedPointsCount = (points.size * animProgress.value).toInt().coerceAtLeast(1)

            for (i in 0 until animatedPointsCount) {
                val pt = points[i]
                val norm = ((pt.value - minVal) / valRange).toFloat().coerceIn(0f, 1f)
                val x = if (points.size > 1) i * step else chartWidth / 2f
                val y = topPadding + chartHeight - (norm * chartHeight)

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

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.25f),
                            primaryColor.copy(alpha = 0.02f)
                        ),
                        startY = topPadding,
                        endY = size.height - bottomPadding
                    )
                )

                drawPath(
                    path = linePath,
                    color = primaryColor,
                    style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Dots on points
                for (i in 0 until animatedPointsCount) {
                    val pt = points[i]
                    val norm = ((pt.value - minVal) / valRange).toFloat().coerceIn(0f, 1f)
                    val x = if (points.size > 1) i * step else chartWidth / 2f
                    val y = topPadding + chartHeight - (norm * chartHeight)
                    drawCircle(color = primaryColor, radius = 4.5f, center = Offset(x, y))
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
