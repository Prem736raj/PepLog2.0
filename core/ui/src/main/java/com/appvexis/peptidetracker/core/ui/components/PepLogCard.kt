package com.appvexis.peptidetracker.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme

/**
 * Premium card component v2.
 *
 * Features:
 * - Glassmorphic mode with gradient borders and translucent overlay
 * - Press-down scale animation for tactile feedback
 * - Inner glow effect via layered gradients
 * - Configurable corner radius and elevation
 */
@Composable
fun PepLogCard(
    modifier: Modifier = Modifier,
    isGlassmorphic: Boolean = false,
    cornerRadius: Dp = 20.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val colors = PepLogTheme.colors

    // Press interaction for scale animation
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "CardScale"
    )

    val cardModifier = modifier
        .scale(scale)
        .clip(shape)
        .let {
            if (onClick != null) {
                it.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
            } else it
        }

    if (isGlassmorphic) {
        // Premium glassmorphic card with gradient border and translucent fill
        val glassBackground = if (colors.isDark) {
            Color(0x0DFFFFFF) // 5% white overlay
        } else {
            Color(0xBFFFFFFF) // 75% white overlay
        }

        val borderBrush = Brush.linearGradient(
            colors = if (colors.isDark) {
                listOf(
                    Color(0x33FFFFFF),                     // White highlight top
                    Color(0x08FFFFFF),                     // Fade mid
                    colors.primary.copy(alpha = 0.20f),   // Primary glow bottom
                    colors.secondary.copy(alpha = 0.10f)  // Secondary tint
                )
            } else {
                listOf(
                    Color(0x80FFFFFF),
                    Color(0x1AFFFFFF),
                    colors.primary.copy(alpha = 0.15f),
                    colors.secondary.copy(alpha = 0.08f)
                )
            }
        )

        Box(
            modifier = cardModifier
                .shadow(
                    elevation = if (colors.isDark) 8.dp else 4.dp,
                    shape = shape,
                    ambientColor = colors.primary.copy(alpha = 0.08f),
                    spotColor = colors.primary.copy(alpha = 0.05f)
                )
                .background(glassBackground, shape)
                .border(BorderStroke(1.dp, borderBrush), shape)
                .padding(PepLogTheme.spacing.medium)
        ) {
            content()
        }
    } else {
        // Standard elevated card
        Surface(
            modifier = cardModifier.shadow(
                elevation = if (colors.isDark) 6.dp else 2.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.06f),
                spotColor = Color.Black.copy(alpha = 0.04f)
            ),
            shape = shape,
            color = colors.surface,
            border = BorderStroke(
                width = 1.dp,
                color = if (colors.isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
            )
        ) {
            Box(modifier = Modifier.padding(PepLogTheme.spacing.medium)) {
                content()
            }
        }
    }
}
