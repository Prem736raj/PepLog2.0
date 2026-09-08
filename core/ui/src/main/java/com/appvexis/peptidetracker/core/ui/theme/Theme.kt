package com.appvexis.peptidetracker.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkColors.primary,
    onPrimary = DarkColors.background,
    primaryContainer = Color(0xFF3730A3),
    onPrimaryContainer = DarkColors.textPrimary,
    secondary = DarkColors.secondary,
    onSecondary = DarkColors.background,
    secondaryContainer = Color(0xFF164E63),
    onSecondaryContainer = DarkColors.textPrimary,
    tertiary = DarkColors.warning,
    onTertiary = DarkColors.background,
    background = DarkColors.background,
    onBackground = DarkColors.textPrimary,
    surface = DarkColors.surface,
    onSurface = DarkColors.textPrimary,
    surfaceVariant = DarkColors.surfaceHigh,
    onSurfaceVariant = DarkColors.textSecondary,
    error = DarkColors.accent,
    onError = DarkColors.background,
    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF334155),
)

private val LightColorScheme = lightColorScheme(
    primary = LightColors.primary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = LightColors.primaryVariant,
    secondary = LightColors.secondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = LightColors.textPrimary,
    tertiary = LightColors.warning,
    onTertiary = Color.White,
    background = LightColors.background,
    onBackground = LightColors.textPrimary,
    surface = LightColors.surface,
    onSurface = LightColors.textPrimary,
    surfaceVariant = LightColors.surfaceHigh,
    onSurfaceVariant = LightColors.textSecondary,
    error = LightColors.accent,
    onError = Color.White,
    outline = Color(0xFF94A3B8),
    outlineVariant = Color(0xFFCBD5E1),
)

@Composable
fun PepLogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val rememberedColors = remember { colors.copy() }.apply { updateColorsFrom(colors) }

    CompositionLocalProvider(
        LocalPepLogColors provides rememberedColors,
        LocalSpacing provides Spacing(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = PepLogShapes,
            content = content,
        )
    }
}

object PepLogTheme {
    val colors: PepLogColors
        @Composable
        @ReadOnlyComposable
        get() = LocalPepLogColors.current

    val spacing: Spacing
        @Composable
        @ReadOnlyComposable
        get() = LocalSpacing.current
}
