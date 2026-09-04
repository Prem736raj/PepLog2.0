package com.appvexis.peptidetracker.core.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * A restrained colour system for a private health log.
 *
 * PepLog deliberately avoids the electric cyan, purple and glass effects that made the
 * previous interface feel like a generic generated health dashboard. The palette is built
 * around warm paper, deep evergreen ink and a single calm mineral accent.
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
    background = Color(0xFF0E1411),
    surface = Color(0xFF151D19),
    surfaceHigh = Color(0xFF1D2722),
    surfaceDim = Color(0xFF090D0B),
    primary = Color(0xFF8EC5B6),
    primaryVariant = Color(0xFFA9D4C7),
    secondary = Color(0xFFB6C3BC),
    accent = Color(0xFFE0A27B),
    warning = Color(0xFFDAB36B),
    textPrimary = Color(0xFFF0F3EE),
    textSecondary = Color(0xFFA6B0AA),
    success = Color(0xFF8FC8A7),
    isDark = true,
)

val LightColors = PepLogColors(
    background = Color(0xFFF6F6F1),
    surface = Color(0xFFFFFFFF),
    surfaceHigh = Color(0xFFEEF1EB),
    surfaceDim = Color(0xFFE1E5DE),
    primary = Color(0xFF1E625D),
    primaryVariant = Color(0xFF194C48),
    secondary = Color(0xFF56645F),
    accent = Color(0xFFB85E3C),
    warning = Color(0xFF99681F),
    textPrimary = Color(0xFF17201C),
    textSecondary = Color(0xFF637069),
    success = Color(0xFF2F7656),
    isDark = false,
)

val LocalPepLogColors = staticCompositionLocalOf { DarkColors }
