package com.appvexis.peptidetracker.feature.calculator

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme

/**
 * Animated syringe visualization showing the fill level for a given draw volume.
 *
 * Renders a photorealistic U-100 insulin syringe with:
 * - Barrel with graduated tick marks (every 10 units)
 * - Animated liquid fill level based on [fillUnits]
 * - Plunger position animated to match fill level
 * - Needle tip at the bottom
 * - Unit labels on the side
 *
 * @param fillUnits The number of units to fill the syringe to (0 to [maxUnits])
 * @param maxUnits The total syringe capacity (default 100 for U-100)
 * @param isDark Whether to render in dark mode colors
 */
@Composable
fun SyringeVisualization(
    fillUnits: Double,
    maxUnits: Double = 100.0,
    modifier: Modifier = Modifier
) {
    val colors = PepLogTheme.colors
    val textMeasurer = rememberTextMeasurer()
    
    // Smooth animation for fill level
    val animatedFill by animateFloatAsState(
        targetValue = (fillUnits.coerceIn(0.0, maxUnits) / maxUnits).toFloat(),
        animationSpec = tween(durationMillis = 600),
        label = "SyringeFillAnimation"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        // Syringe dimensions relative to canvas
        val barrelWidth = canvasWidth * 0.18f
        val barrelLeft = (canvasWidth - barrelWidth) / 2f
        val barrelRight = barrelLeft + barrelWidth
        val barrelTop = canvasHeight * 0.12f
        val barrelBottom = canvasHeight * 0.78f
        val barrelHeight = barrelBottom - barrelTop
        
        // Needle dimensions
        val needleWidth = barrelWidth * 0.08f
        val needleTop = barrelBottom
        val needleBottom = canvasHeight * 0.95f
        
        // Plunger dimensions
        val plungerWidth = barrelWidth * 0.85f
        val plungerHeight = 8f
        
        // Calculate fill Y position (fill from bottom up)
        val fillHeight = barrelHeight * animatedFill
        val fillTop = barrelBottom - fillHeight
        
        // Plunger Y position sits at top of liquid
        val plungerY = fillTop - plungerHeight / 2f

        // ========== DRAW BARREL BACKGROUND ==========
        val barrelBg = if (colors.isDark) Color(0xFF2A2A40) else Color(0xFFF5F5F5)
        val barrelBorder = if (colors.isDark) Color(0xFF4A4A60) else Color(0xFFCCCCCC)
        
        // Barrel body
        drawRoundRect(
            color = barrelBg,
            topLeft = Offset(barrelLeft, barrelTop),
            size = Size(barrelWidth, barrelHeight),
            cornerRadius = CornerRadius(6f, 6f)
        )
        
        // Barrel glass reflection gradient
        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.White.copy(alpha = if (colors.isDark) 0.04f else 0.4f),
                    Color.Transparent,
                    Color.White.copy(alpha = if (colors.isDark) 0.02f else 0.15f)
                ),
                startX = barrelLeft,
                endX = barrelRight
            ),
            topLeft = Offset(barrelLeft, barrelTop),
            size = Size(barrelWidth, barrelHeight),
            cornerRadius = CornerRadius(6f, 6f)
        )
        
        // Barrel outline
        drawRoundRect(
            color = barrelBorder,
            topLeft = Offset(barrelLeft, barrelTop),
            size = Size(barrelWidth, barrelHeight),
            cornerRadius = CornerRadius(6f, 6f),
            style = Stroke(width = 2f)
        )

        // ========== DRAW LIQUID FILL ==========
        if (animatedFill > 0.001f) {
            val liquidGradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF8FC4B6).copy(alpha = 0.4f),
                    Color(0xFF5E9D90).copy(alpha = 0.7f),
                    Color(0xFF3F786F).copy(alpha = 0.85f)
                ),
                startY = fillTop,
                endY = barrelBottom
            )
            
            // Clip fill to barrel bounds
            val fillPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = Rect(
                            left = barrelLeft + 2f,
                            top = fillTop,
                            right = barrelRight - 2f,
                            bottom = barrelBottom - 1f
                        ),
                        bottomLeft = CornerRadius(4f, 4f),
                        bottomRight = CornerRadius(4f, 4f),
                        topLeft = CornerRadius(0f, 0f),
                        topRight = CornerRadius(0f, 0f)
                    )
                )
            }
            drawPath(fillPath, liquidGradient)
            
            // Liquid meniscus (curved top surface)
            val meniscusPath = Path().apply {
                moveTo(barrelLeft + 2f, fillTop)
                quadraticTo(
                    (barrelLeft + barrelRight) / 2f, fillTop + 4f,
                    barrelRight - 2f, fillTop
                )
            }
            drawPath(
                meniscusPath,
                Color(0xFF5E9D90).copy(alpha = 0.6f),
                style = Stroke(width = 2f, cap = StrokeCap.Round)
            )
        }

        // ========== DRAW GRADUATED MARKS ==========
        val majorTicks = 10 // Every 10 units
        val totalMajorTicks = (maxUnits / majorTicks).toInt()
        val tickColor = if (colors.isDark) Color(0xFF888899) else Color(0xFF888888)
        val labelColor = if (colors.isDark) Color(0xFFAAB0C0) else Color(0xFF555555)
        
        for (i in 0..totalMajorTicks) {
            val unitValue = i * majorTicks
            val yFraction = 1f - (unitValue.toFloat() / maxUnits.toFloat())
            val y = barrelTop + barrelHeight * yFraction
            
            val isMajor = unitValue % 20 == 0 || unitValue == 0
            val tickLength = if (isMajor) barrelWidth * 0.25f else barrelWidth * 0.15f
            
            // Left tick
            drawLine(
                color = tickColor,
                start = Offset(barrelLeft, y),
                end = Offset(barrelLeft + tickLength, y),
                strokeWidth = if (isMajor) 1.5f else 1f
            )
            
            // Right tick
            drawLine(
                color = tickColor,
                start = Offset(barrelRight - tickLength, y),
                end = Offset(barrelRight, y),
                strokeWidth = if (isMajor) 1.5f else 1f
            )
            
            // Labels for major ticks (left side, outside barrel)
            if (isMajor && unitValue > 0) {
                val label = "$unitValue"
                val textResult = textMeasurer.measure(
                    text = label,
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = labelColor
                    )
                )
                drawText(
                    textLayoutResult = textResult,
                    topLeft = Offset(
                        barrelLeft - textResult.size.width - 8f,
                        y - textResult.size.height / 2f
                    )
                )
            }
        }
        
        // Minor ticks (every 5 units)
        for (i in 0..(maxUnits / 5).toInt()) {
            val unitValue = i * 5
            if (unitValue % majorTicks != 0) {
                val yFraction = 1f - (unitValue.toFloat() / maxUnits.toFloat())
                val y = barrelTop + barrelHeight * yFraction
                val tickLen = barrelWidth * 0.1f
                
                drawLine(
                    color = tickColor.copy(alpha = 0.5f),
                    start = Offset(barrelLeft, y),
                    end = Offset(barrelLeft + tickLen, y),
                    strokeWidth = 0.8f
                )
                drawLine(
                    color = tickColor.copy(alpha = 0.5f),
                    start = Offset(barrelRight - tickLen, y),
                    end = Offset(barrelRight, y),
                    strokeWidth = 0.8f
                )
            }
        }

        // ========== DRAW PLUNGER ==========
        val plungerColor = if (colors.isDark) Color(0xFF555577) else Color(0xFFAAAAAA)
        val plungerHandleColor = if (colors.isDark) Color(0xFF444466) else Color(0xFF999999)
        val plungerLeft = barrelLeft + (barrelWidth - plungerWidth) / 2f
        
        // Plunger rubber stopper
        drawRoundRect(
            color = Color(0xFF333355),
            topLeft = Offset(plungerLeft, plungerY - 2f),
            size = Size(plungerWidth, plungerHeight + 4f),
            cornerRadius = CornerRadius(3f, 3f)
        )
        
        // Plunger rod going up from stopper
        val rodWidth = barrelWidth * 0.06f
        val rodLeft = (barrelLeft + barrelRight - rodWidth) / 2f
        val rodTop = canvasHeight * 0.02f
        
        drawRoundRect(
            color = plungerColor,
            topLeft = Offset(rodLeft, rodTop),
            size = Size(rodWidth, plungerY - rodTop),
            cornerRadius = CornerRadius(2f, 2f)
        )
        
        // Plunger handle (top)
        val handleWidth = barrelWidth * 0.55f
        val handleLeft = (barrelLeft + barrelRight - handleWidth) / 2f
        drawRoundRect(
            color = plungerHandleColor,
            topLeft = Offset(handleLeft, rodTop - 4f),
            size = Size(handleWidth, 10f),
            cornerRadius = CornerRadius(4f, 4f)
        )

        // ========== DRAW NEEDLE ==========
        // Needle hub (connector between barrel and needle)
        val hubWidth = barrelWidth * 0.35f
        val hubHeight = 14f
        val hubLeft = (barrelLeft + barrelRight - hubWidth) / 2f
        drawRoundRect(
            color = if (colors.isDark) Color(0xFF555577) else Color(0xFFBBBBBB),
            topLeft = Offset(hubLeft, needleTop - 2f),
            size = Size(hubWidth, hubHeight),
            cornerRadius = CornerRadius(3f, 3f)
        )
        
        // Needle shaft
        val needleCenterX = (barrelLeft + barrelRight) / 2f
        drawLine(
            color = if (colors.isDark) Color(0xFF888899) else Color(0xFFAAAAAA),
            start = Offset(needleCenterX, needleTop + hubHeight - 2f),
            end = Offset(needleCenterX, needleBottom),
            strokeWidth = needleWidth,
            cap = StrokeCap.Round
        )
        
        // Needle bevel tip highlight
        drawLine(
            color = Color.White.copy(alpha = 0.3f),
            start = Offset(needleCenterX - 0.5f, needleBottom - 6f),
            end = Offset(needleCenterX - 0.5f, needleBottom),
            strokeWidth = 1f,
            cap = StrokeCap.Round
        )

        // ========== DRAW FILL INDICATOR LINE ==========
        if (animatedFill > 0.001f) {
            // Dashed indicator line extending from fill level to right label
            val indicatorX = barrelRight + 12f
            drawLine(
                color = Color(0xFF5E9D90),
                start = Offset(barrelRight + 2f, fillTop),
                end = Offset(indicatorX + 4f, fillTop),
                strokeWidth = 1.5f,
                cap = StrokeCap.Round
            )
            
            // Fill value label
            val fillLabel = String.format("%.1f U", fillUnits)
            val fillTextResult = textMeasurer.measure(
                text = fillLabel,
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5E9D90)
                )
            )
            drawText(
                textLayoutResult = fillTextResult,
                topLeft = Offset(
                    indicatorX + 8f,
                    fillTop - fillTextResult.size.height / 2f
                )
            )
        }

        // ========== DRAW "UNITS" LABEL AT BOTTOM ==========
        val unitsLabel = "units"
        val unitsTextResult = textMeasurer.measure(
            text = unitsLabel,
            style = TextStyle(
                fontSize = 9.sp,
                fontWeight = FontWeight.Normal,
                color = labelColor.copy(alpha = 0.6f)
            )
        )
        drawText(
            textLayoutResult = unitsTextResult,
            topLeft = Offset(
                barrelLeft - unitsTextResult.size.width - 8f,
                barrelBottom - unitsTextResult.size.height + 2f
            )
        )
    }
}
