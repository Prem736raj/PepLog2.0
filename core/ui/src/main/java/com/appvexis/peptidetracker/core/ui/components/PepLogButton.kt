package com.appvexis.peptidetracker.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme

enum class PepLogButtonVariant { Primary, Secondary, Outlined, Ghost }

/** A calm, high-contrast action hierarchy with no decorative glow or bounce. */
@Composable
fun PepLogButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: PepLogButtonVariant = PepLogButtonVariant.Primary,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: (@Composable () -> Unit)? = null,
) {
    val colors = PepLogTheme.colors
    val active = enabled && !isLoading
    val content: @Composable () -> Unit = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = when (variant) {
                        PepLogButtonVariant.Primary -> if (colors.isDark) colors.background else Color.White
                        PepLogButtonVariant.Secondary, PepLogButtonVariant.Outlined -> colors.primary
                        PepLogButtonVariant.Ghost -> colors.textPrimary
                    },
                    strokeWidth = 2.dp,
                )
            } else {
                icon?.invoke()
                if (icon != null) Spacer(Modifier.width(8.dp))
                Text(text = text, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
    val baseModifier = modifier.defaultMinSize(minHeight = 48.dp)

    when (variant) {
        PepLogButtonVariant.Primary -> Button(
            onClick = onClick,
            modifier = baseModifier,
            enabled = active,
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = if (colors.isDark) colors.background else Color.White,
                disabledContainerColor = colors.surfaceDim,
                disabledContentColor = colors.textSecondary,
            ),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            content = { content() },
        )

        PepLogButtonVariant.Secondary -> Button(
            onClick = onClick,
            modifier = baseModifier,
            enabled = active,
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary.copy(alpha = if (colors.isDark) 0.18f else 0.11f),
                contentColor = colors.primary,
                disabledContainerColor = colors.surfaceDim,
                disabledContentColor = colors.textSecondary,
            ),
            elevation = null,
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            content = { content() },
        )

        PepLogButtonVariant.Outlined -> OutlinedButton(
            onClick = onClick,
            modifier = baseModifier,
            enabled = active,
            shape = MaterialTheme.shapes.small,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = colors.primary,
                disabledContentColor = colors.textSecondary,
            ),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            content = { content() },
        )

        PepLogButtonVariant.Ghost -> TextButton(
            onClick = onClick,
            modifier = baseModifier,
            enabled = active,
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.textButtonColors(
                contentColor = colors.primary,
                disabledContentColor = colors.textSecondary,
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
            content = { content() },
        )
    }
}
