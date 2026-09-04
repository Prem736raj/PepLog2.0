package com.appvexis.peptidetracker.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme

/**
 * The app's base information container. `isGlassmorphic` remains for source compatibility,
 * but now simply selects a quiet tonal surface instead of a translucent, gradient treatment.
 */
@Composable
fun PepLogCard(
    modifier: Modifier = Modifier,
    isGlassmorphic: Boolean = false,
    cornerRadius: Dp = 14.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = PepLogTheme.colors
    val shape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius)
    val cardModifier = if (onClick == null) {
        modifier
    } else {
        modifier.clickable(role = Role.Button, onClick = onClick)
    }

    Surface(
        modifier = cardModifier,
        shape = shape,
        color = if (isGlassmorphic) colors.surfaceHigh else colors.surface,
        contentColor = colors.textPrimary,
        border = BorderStroke(
            width = 1.dp,
            color = if (isGlassmorphic) {
                colors.primary.copy(alpha = if (colors.isDark) 0.17f else 0.14f)
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(modifier = Modifier.padding(PepLogTheme.spacing.medium), content = content)
    }
}
