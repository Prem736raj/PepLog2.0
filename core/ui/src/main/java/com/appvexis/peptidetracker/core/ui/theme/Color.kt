package com.appvexis.peptidetracker.core.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * A vibrant, colorful system designed for maximum user engagement and readability.
 *
 * PepLog uses a striking modern palette featuring deep indigos and vibrant teals,
 * standing out clearly from competitors. It strictly prioritizes WCAG contrast guidelines
 * to ensure all text remains highly legible.
 */
class PepLogColors(
    background: Color,
    surface: Color,
    surfaceHigh: Color,
    surfaceDim: Color,
    primary: Color,
    primaryVariant: Color,
    secondary: Color,
    accent: Color,
    warning: Color,
    textPrimary: Color,
    textSecondary: Color,
    success: Color,
    isDark: Boolean,
) {
    var background by mutableStateOf(background)
        private set
    var surface by mutableStateOf(surface)
        private set
    var surfaceHigh by mutableStateOf(surfaceHigh)
        private set
    var surfaceDim by mutableStateOf(surfaceDim)
        private set
    var primary by mutableStateOf(primary)
        private set
    var primaryVariant by mutableStateOf(primaryVariant)
        private set
    var secondary by mutableStateOf(secondary)
        private set
    var accent by mutableStateOf(accent)
        private set
    var warning by mutableStateOf(warning)
        private set
    var textPrimary by mutableStateOf(textPrimary)
        private set
    var textSecondary by mutableStateOf(textSecondary)
        private set
    var success by mutableStateOf(success)
        private set
    var isDark by mutableStateOf(isDark)
        private set

    fun copy(
        background: Color = this.background,
        surface: Color = this.surface,
        surfaceHigh: Color = this.surfaceHigh,
        surfaceDim: Color = this.surfaceDim,
        primary: Color = this.primary,
        primaryVariant: Color = this.primaryVariant,
        secondary: Color = this.secondary,
        accent: Color = this.accent,
        warning: Color = this.warning,
        textPrimary: Color = this.textPrimary,
        textSecondary: Color = this.textSecondary,
        success: Color = this.success,
        isDark: Boolean = this.isDark,
    ): PepLogColors = PepLogColors(
        background = background,
        surface = surface,
        surfaceHigh = surfaceHigh,
        surfaceDim = surfaceDim,
        primary = primary,
        primaryVariant = primaryVariant,
        secondary = secondary,
        accent = accent,
        warning = warning,
        textPrimary = textPrimary,
        textSecondary = textSecondary,
        success = success,
        isDark = isDark,
    )

    fun updateColorsFrom(other: PepLogColors) {
        background = other.background
        surface = other.surface
        surfaceHigh = other.surfaceHigh
        surfaceDim = other.surfaceDim
        primary = other.primary
        primaryVariant = other.primaryVariant
        secondary = other.secondary
        accent = other.accent
        warning = other.warning
        textPrimary = other.textPrimary
        textSecondary = other.textSecondary
        success = other.success
        isDark = other.isDark
    }
}

val DarkColors = PepLogColors(
    background = Color(0xFF0B1120),
    surface = Color(0xFF1E293B),
    surfaceHigh = Color(0xFF334155),
    surfaceDim = Color(0xFF020617),
    primary = Color(0xFF818CF8),
    primaryVariant = Color(0xFF6366F1),
    secondary = Color(0xFF22D3EE),
    accent = Color(0xFFFBBF24),
    warning = Color(0xFFF59E0B),
    textPrimary = Color(0xFFF8FAFC),
    textSecondary = Color(0xFF94A3B8),
    success = Color(0xFF34D399),
    isDark = true,
)

val LightColors = PepLogColors(
    background = Color(0xFFF8F9FA),
    surface = Color(0xFFFFFFFF),
    surfaceHigh = Color(0xFFF1F5F9),
    surfaceDim = Color(0xFFE2E8F0),
    primary = Color(0xFF4F46E5),
    primaryVariant = Color(0xFF4338CA),
    secondary = Color(0xFF0D9488),
    accent = Color(0xFFF59E0B),
    warning = Color(0xFFD97706),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF475569),
    success = Color(0xFF10B981),
    isDark = false,
)

val LocalPepLogColors = staticCompositionLocalOf { DarkColors }
