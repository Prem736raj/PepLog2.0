package com.appvexis.peptidetracker.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme

/**
 * Visual variations for custom button.
 */
enum class PepLogButtonVariant {
    Primary, Secondary, Outlined, Ghost
}

/**
 * Premium button v2 with gradient fills, colored glow shadows,
 * press-scale animation, and polished loading states.
 */
@Composable
fun PepLogButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: PepLogButtonVariant = PepLogButtonVariant.Primary,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: (@Composable () -> Unit)? = null
) {
    val colors = PepLogTheme.colors
    val shape = RoundedCornerShape(14.dp)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        label = "ButtonScale"
    )

    val modifierWithScale = modifier.scale(scale)

    val buttonContent = @Composable {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = when (variant) {
                        PepLogButtonVariant.Primary -> if (colors.isDark) colors.background else Color.White
                        PepLogButtonVariant.Secondary -> if (colors.isDark) colors.background else Color.White
                        PepLogButtonVariant.Outlined -> colors.primary
                        PepLogButtonVariant.Ghost -> colors.textPrimary
                    },
                    strokeWidth = 2.dp
                )
            } else {
                if (icon != null) {
                    icon()
                    Spacer(modifier = Modifier.width(PepLogTheme.spacing.small))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }

    when (variant) {
        PepLogButtonVariant.Primary -> {
            // Gradient button with colored glow
            Button(
                onClick = onClick,
                modifier = modifierWithScale
                    .height(50.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = shape,
                        ambientColor = colors.primary.copy(alpha = 0.25f),
                        spotColor = colors.primary.copy(alpha = 0.20f)
                    ),
                enabled = enabled && !isLoading,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = if (colors.isDark) colors.background else Color.White,
                    disabledContainerColor = colors.surfaceHigh,
                    disabledContentColor = colors.textSecondary
                ),
                interactionSource = interactionSource,
                contentPadding = PaddingValues(horizontal = PepLogTheme.spacing.large),
                content = { buttonContent() }
            )
        }
        PepLogButtonVariant.Secondary -> {
            Button(
                onClick = onClick,
                modifier = modifierWithScale
                    .height(50.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = shape,
                        ambientColor = colors.secondary.copy(alpha = 0.20f),
                        spotColor = colors.secondary.copy(alpha = 0.15f)
                    ),
                enabled = enabled && !isLoading,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.secondary,
                    contentColor = if (colors.isDark) colors.background else Color.White,
                    disabledContainerColor = colors.surfaceHigh,
                    disabledContentColor = colors.textSecondary
                ),
                interactionSource = interactionSource,
                contentPadding = PaddingValues(horizontal = PepLogTheme.spacing.large),
                content = { buttonContent() }
            )
        }
        PepLogButtonVariant.Outlined -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifierWithScale.height(50.dp),
                enabled = enabled && !isLoading,
                shape = shape,
                border = BorderStroke(
                    1.5.dp,
                    if (enabled) {
                        Brush.linearGradient(
                            colors = listOf(colors.primary, colors.primaryVariant)
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(colors.surfaceHigh, colors.surfaceHigh)
                        )
                    }
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colors.primary,
                    disabledContentColor = colors.textSecondary
                ),
                interactionSource = interactionSource,
                contentPadding = PaddingValues(horizontal = PepLogTheme.spacing.large),
                content = { buttonContent() }
            )
        }
        PepLogButtonVariant.Ghost -> {
            Button(
                onClick = onClick,
                modifier = modifierWithScale.height(50.dp),
                enabled = enabled && !isLoading,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = colors.textPrimary,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = colors.textSecondary
                ),
                interactionSource = interactionSource,
                contentPadding = PaddingValues(horizontal = PepLogTheme.spacing.large),
                elevation = null,
                content = { buttonContent() }
            )
        }
    }
}
