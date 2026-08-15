package com.appvexis.peptidetracker.feature.inventory.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import com.appvexis.peptidetracker.feature.inventory.model.ExpirationStatus
import java.util.Locale

/**
 * Custom Canvas composable rendering an authentic medical peptide vial
 * with animated liquid fill level, graduation lines, crimp cap, and specular reflections.
 */
@Composable
fun VialLiquidGauge(
    fillPercent: Float,
    isReconstituted: Boolean,
    expirationStatus: ExpirationStatus,
    remainingVolumeMl: Double?,
    totalVolumeMl: Double?,
    vialStrengthMg: Double,
    modifier: Modifier = Modifier,
    width: Dp = 64.dp,
    height: Dp = 108.dp,
    showReadout: Boolean = true
) {
    val animatedFill by animateFloatAsState(
        targetValue = fillPercent.coerceIn(0.0f, 1.0f),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "VialFillAnimation"
    )

    val colors = PepLogTheme.colors

    // Liquid gradient based on state
    val liquidColors = when {
        !isReconstituted -> listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8)) // Lyophilized powder
        expirationStatus == ExpirationStatus.EXPIRED || expirationStatus == ExpirationStatus.CRITICAL -> listOf(
            Color(0xFFF43F5E),
            Color(0xFFDC2626)
        )
        expirationStatus == ExpirationStatus.EXPIRING_SOON -> listOf(
            Color(0xFFFBBF24),
            Color(0xFFD97706)
        )
        else -> listOf(
            Color(0xFF22D3EE),
            Color(0xFF0D9488)
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .width(width)
                .height(height),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(width, height)) {
                drawVial(
                    fillRatio = animatedFill,
                    isReconstituted = isReconstituted,
                    liquidColors = liquidColors,
                    isDark = colors.isDark
                )
            }
        }

        if (showReadout) {
            Spacer(modifier = Modifier.height(4.dp))
            val readoutText = if (isReconstituted && remainingVolumeMl != null) {
                String.format(Locale.US, "%.2f mL", remainingVolumeMl)
            } else if (!isReconstituted) {
                "${vialStrengthMg.toInt()} mg dry"
            } else {
                "0.00 mL"
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = colors.surfaceHigh
            ) {
                Text(
                    text = readoutText,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = colors.textPrimary,
                    modifier = Modifier
                )
            }
        }
    }
}

private fun DrawScope.drawVial(
    fillRatio: Float,
    isReconstituted: Boolean,
    liquidColors: List<Color>,
    isDark: Boolean
) {
    val canvasWidth = size.width
    val canvasHeight = size.height

    // Dimensions proportions
    val capWidth = canvasWidth * 0.58f
    val capHeight = canvasHeight * 0.11f
    val neckWidth = canvasWidth * 0.44f
    val neckHeight = canvasHeight * 0.08f
    val bodyWidth = canvasWidth * 0.88f
    val bodyHeight = canvasHeight * 0.74f

    val centerX = canvasWidth / 2f
    val topY = canvasHeight * 0.03f

    // 1. Rubber Stopper & Crimp Cap
    val capLeft = centerX - capWidth / 2f
    val capTop = topY

    // Metallic crimp collar gradient
    val capBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFE2E8F0),
            Color(0xFF94A3B8),
            Color(0xFFCBD5E1),
            Color(0xFF64748B)
        ),
        start = Offset(capLeft, capTop),
        end = Offset(capLeft + capWidth, capTop + capHeight)
    )

    drawRoundRect(
        brush = capBrush,
        topLeft = Offset(capLeft, capTop),
        size = Size(capWidth, capHeight),
        cornerRadius = CornerRadius(4f, 4f)
    )

    // Rubber stopper center target (circle in the middle of cap)
    drawCircle(
        color = Color(0xFF334155),
        radius = capHeight * 0.32f,
        center = Offset(centerX, capTop + capHeight * 0.45f)
    )

    // 2. Glass Neck
    val neckLeft = centerX - neckWidth / 2f
    val neckTop = capTop + capHeight
    val neckBrush = Brush.verticalGradient(
        colors = if (isDark) listOf(Color(0x20FFFFFF), Color(0x10FFFFFF)) else listOf(Color(0x30E2E8F0), Color(0x15CBD5E1))
    )
    drawRect(
        brush = neckBrush,
        topLeft = Offset(neckLeft, neckTop),
        size = Size(neckWidth, neckHeight)
    )

    // 3. Glass Body Outline Path
    val bodyLeft = centerX - bodyWidth / 2f
    val bodyTop = neckTop + neckHeight
    val bodyCornerRadius = 14f

    val bodyPath = Path().apply {
        addRoundRect(
            RoundRect(
                left = bodyLeft,
                top = bodyTop,
                right = bodyLeft + bodyWidth,
                bottom = bodyTop + bodyHeight,
                cornerRadius = CornerRadius(bodyCornerRadius, bodyCornerRadius)
            )
        )
    }

    // Draw Glass Background Tint
    drawPath(
        path = bodyPath,
        color = if (isDark) Color(0x15FFFFFF) else Color(0x25E2E8F0)
    )

    // 4. Liquid or Powder Fill
    if (!isReconstituted) {
        // Draw Lyophilized Powder Cake at the bottom
        val cakeHeight = bodyHeight * 0.22f
        val cakeTop = bodyTop + bodyHeight - cakeHeight
        val powderPath = Path().apply {
            moveTo(bodyLeft + 4f, cakeTop + 6f)
            cubicTo(
                bodyLeft + bodyWidth * 0.3f, cakeTop - 2f,
                bodyLeft + bodyWidth * 0.7f, cakeTop + 8f,
                bodyLeft + bodyWidth - 4f, cakeTop + 4f
            )
            lineTo(bodyLeft + bodyWidth - 4f, bodyTop + bodyHeight - 4f)
            lineTo(bodyLeft + 4f, bodyTop + bodyHeight - 4f)
            close()
        }

        drawPath(
            path = powderPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFF1F5F9), Color(0xFFCBD5E1))
            )
        )
    } else if (fillRatio > 0.0f) {
        // Draw Liquid Volume
        val liquidHeight = (bodyHeight - 8f) * fillRatio
        val liquidTop = bodyTop + bodyHeight - 4f - liquidHeight

        val liquidPath = Path().apply {
            moveTo(bodyLeft + 4f, liquidTop)
            // Gentle meniscus curve
            quadraticTo(
                centerX, liquidTop + 4f,
                bodyLeft + bodyWidth - 4f, liquidTop
            )
            lineTo(bodyLeft + bodyWidth - 4f, bodyTop + bodyHeight - 6f)
            quadraticTo(
                centerX, bodyTop + bodyHeight - 2f,
                bodyLeft + 4f, bodyTop + bodyHeight - 6f
            )
            close()
        }

        drawPath(
            path = liquidPath,
            brush = Brush.verticalGradient(
                colors = liquidColors,
                startY = liquidTop,
                endY = bodyTop + bodyHeight
            )
        )

        // Surface Meniscus Highlight
        drawLine(
            color = Color.White.copy(alpha = 0.6f),
            start = Offset(bodyLeft + 6f, liquidTop),
            end = Offset(bodyLeft + bodyWidth - 6f, liquidTop),
            strokeWidth = 2f
        )
    }

    // 5. Graduation Tick Marks on Glass
    val tickMarginLeft = bodyLeft + 6f
    val tickSteps = 4
    for (i in 1..tickSteps) {
        val tickY = bodyTop + bodyHeight * (0.2f + 0.16f * i)
        val tickLength = if (i % 2 == 0) 10f else 6f
        drawLine(
            color = if (isDark) Color(0x60FFFFFF) else Color(0x6064748B),
            start = Offset(tickMarginLeft, tickY),
            end = Offset(tickMarginLeft + tickLength, tickY),
            strokeWidth = 1.5f
        )
    }

    // 6. Glass Body Stroke & Specular Highlight
    drawPath(
        path = bodyPath,
        color = if (isDark) Color(0x40FFFFFF) else Color(0x5094A3B8),
        style = Stroke(width = 2.5f)
    )

    // Vertical Glass Reflection Streak
    drawLine(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                Color.White.copy(alpha = if (isDark) 0.35f else 0.55f),
                Color.Transparent
            ),
            startY = bodyTop + 6f,
            endY = bodyTop + bodyHeight - 6f
        ),
        start = Offset(bodyLeft + 6f, bodyTop + 6f),
        end = Offset(bodyLeft + 6f, bodyTop + bodyHeight - 6f),
        strokeWidth = 3f
    )
}
