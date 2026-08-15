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
    primary = Color(0xFF22D3EE),              // Cyan-400
    onPrimary = Color(0xFF080B14),            // Dark background
    primaryContainer = Color(0xFF06B6D4),     // Cyan-500
    onPrimaryContainer = Color(0xFFF1F5F9),
    secondary = Color(0xFFA78BFA),            // Violet-400
    onSecondary = Color(0xFF080B14),
    secondaryContainer = Color(0xFF7C3AED),   // Violet-600
    onSecondaryContainer = Color(0xFFF1F5F9),
    tertiary = Color(0xFFFBBF24),             // Amber-400 (warning)
    onTertiary = Color(0xFF080B14),
    background = Color(0xFF080B14),           // True-dark
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF111827),              // Slate-900
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF1E293B),       // Slate-800
    onSurfaceVariant = Color(0xFF94A3B8),     // Slate-400
    error = Color(0xFFF43F5E),               // Rose-500
    onError = Color.White,
    outline = Color(0xFF334155),              // Slate-700 borders
    outlineVariant = Color(0xFF1E293B)        // Subtle borders
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0891B2),              // Cyan-600
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0E7490),     // Cyan-700
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF7C3AED),            // Violet-600
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF6D28D9),   // Violet-700
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFFD97706),             // Amber-600
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),           // Slate-50
    onBackground = Color(0xFF0F172A),         // Slate-900
    surface = Color(0xFFFFFFFF),              // Pure white
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),       // Slate-100
    onSurfaceVariant = Color(0xFF64748B),     // Slate-500
    error = Color(0xFFE11D48),               // Rose-600
    onError = Color.White,
    outline = Color(0xFFCBD5E1),             // Slate-300 borders
    outlineVariant = Color(0xFFE2E8F0)       // Subtle borders
)

@Composable
fun PepLogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val rememberedColors = remember { colors.copy() }.apply {
        updateColorsFrom(colors)
    }

    CompositionLocalProvider(
        LocalPepLogColors provides rememberedColors,
        LocalSpacing provides Spacing()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

/**
 * Accessor object for custom styling tokens in Compose components.
 * e.g. PepLogTheme.colors.surfaceHigh or PepLogTheme.spacing.medium
 */
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
