package com.appvexis.peptidetracker.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme

/**
 * Interactive filter chip v2 with gradient selection and scale animation.
 */
@Composable
fun PepLogChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val colors = PepLogTheme.colors
    val shape = RoundedCornerShape(12.dp)

    val chipScale by animateFloatAsState(
        targetValue = if (selected) 1.03f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "chipScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (selected) {
            colors.primary.copy(alpha = 0.15f)
        } else {
            if (colors.isDark) colors.surface else Color(0x08000000)
        },
        label = "chipBackground"
    )

    val borderColor by animateColorAsState(
        targetValue = if (selected) {
            colors.primary.copy(alpha = 0.6f)
        } else {
            if (colors.isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
        },
        label = "chipBorder"
    )

    val textColor by animateColorAsState(
        targetValue = if (selected) {
            colors.primary
        } else {
            colors.textSecondary
        },
        label = "chipText"
    )

    Box(
        modifier = modifier
            .scale(chipScale)
            .clip(shape)
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .border(1.dp, borderColor, shape)
            .padding(horizontal = PepLogTheme.spacing.medium, vertical = PepLogTheme.spacing.small),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(PepLogTheme.spacing.extraSmall))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = textColor
            )
        }
    }
}

/**
 * Non-interactive status or category tag v2.
 */
@Composable
fun PepLogTag(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = PepLogTheme.colors.primary,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.12f), shape)
            .border(1.dp, color.copy(alpha = 0.25f), shape)
            .padding(horizontal = PepLogTheme.spacing.small, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}
