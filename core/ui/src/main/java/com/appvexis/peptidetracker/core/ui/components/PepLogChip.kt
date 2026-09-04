package com.appvexis.peptidetracker.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme

@Composable
fun PepLogChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val colors = PepLogTheme.colors
    val shape = MaterialTheme.shapes.small
    val background = if (selected) colors.primary.copy(alpha = if (colors.isDark) 0.2f else 0.12f) else colors.surface
    val border = if (selected) colors.primary.copy(alpha = 0.55f) else MaterialTheme.colorScheme.outlineVariant
    val contentColor = if (selected) colors.primary else colors.textSecondary

    Box(
        modifier = modifier
            .heightIn(min = 44.dp)
            .background(background, shape)
            .border(1.dp, border, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            leadingIcon?.invoke()
            if (leadingIcon != null) Spacer(Modifier.width(6.dp))
            Text(text = text, style = MaterialTheme.typography.labelMedium, color = contentColor)
        }
    }
}

@Composable
fun PepLogTag(
    text: String,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = PepLogTheme.colors.primary,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val shape = MaterialTheme.shapes.extraSmall
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.12f), shape)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            leadingIcon?.invoke()
            if (leadingIcon != null) Spacer(Modifier.width(4.dp))
            Text(text = text, style = MaterialTheme.typography.labelSmall, color = color)
        }
    }
}
