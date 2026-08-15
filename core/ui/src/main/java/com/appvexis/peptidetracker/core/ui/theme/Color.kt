package com.appvexis.peptidetracker.core.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Premium custom color system for the PepLog Application.
 *
 * v2 Redesign: Shifted from flat teal/amber to a richer, more vibrant
 * palette inspired by modern biotech/health-tech apps. Features:
 * - Deeper OLED-optimized dark backgrounds
 * - Vibrant cyan primary with violet secondary for contrast
 * - Gradient-ready color tokens
 * - Additional semantic colors (warning, surfaceDim)
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
    // Gradient tokens for premium components
    primaryGradientStart: Color,
    primaryGradientEnd: Color,
    accentGradientStart: Color,
    accentGradientEnd: Color
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
    var primaryGradientStart by mutableStateOf(primaryGradientStart)
        private set
    var primaryGradientEnd by mutableStateOf(primaryGradientEnd)
        private set
    var accentGradientStart by mutableStateOf(accentGradientStart)
        private set
    var accentGradientEnd by mutableStateOf(accentGradientEnd)
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
        primaryGradientStart: Color = this.primaryGradientStart,
        primaryGradientEnd: Color = this.primaryGradientEnd,
        accentGradientStart: Color = this.accentGradientStart,
        accentGradientEnd: Color = this.accentGradientEnd
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
        primaryGradientStart = primaryGradientStart,
        primaryGradientEnd = primaryGradientEnd,
        accentGradientStart = accentGradientStart,
        accentGradientEnd = accentGradientEnd
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
        primaryGradientStart = other.primaryGradientStart
        primaryGradientEnd = other.primaryGradientEnd
        accentGradientStart = other.accentGradientStart
        accentGradientEnd = other.accentGradientEnd
    }
}

// ─── Dark Theme ─────────────────────────────────────────────────────
// Deep, OLED-optimized biotech palette with vibrant accents
val DarkColors = PepLogColors(
    background = Color(0xFF080B14),            // True-dark OLED-optimized
    surface = Color(0xFF111827),               // Slate-900 card surface
    surfaceHigh = Color(0xFF1E293B),           // Slate-800 elevated dialogs
    surfaceDim = Color(0xFF030712),            // Ultra-dark for overlays
    primary = Color(0xFF22D3EE),               // Cyan-400 — vibrant, futuristic
    primaryVariant = Color(0xFF06B6D4),        // Cyan-500 — richer depth
    secondary = Color(0xFFA78BFA),             // Violet-400 — premium purple
    accent = Color(0xFFF43F5E),               // Rose-500 — sharp alarm/error
    warning = Color(0xFFFBBF24),              // Amber-400 — caution/alert
    textPrimary = Color(0xFFF1F5F9),          // Slate-100 — crisp white
    textSecondary = Color(0xFF94A3B8),        // Slate-400 — readable muted
    success = Color(0xFF34D399),              // Emerald-400 — vibrant green
    isDark = true,
    primaryGradientStart = Color(0xFF22D3EE),  // Cyan-400
    primaryGradientEnd = Color(0xFF0891B2),    // Cyan-600
    accentGradientStart = Color(0xFFA78BFA),   // Violet-400
    accentGradientEnd = Color(0xFF7C3AED)      // Violet-600
)

// ─── Light Theme ────────────────────────────────────────────────────
// Clean, professional light palette with the same vibrant accents
val LightColors = PepLogColors(
    background = Color(0xFFF8FAFC),            // Slate-50 — warm off-white
    surface = Color(0xFFFFFFFF),               // Pure white cards
    surfaceHigh = Color(0xFFF1F5F9),           // Slate-100 — elevated components
    surfaceDim = Color(0xFFE2E8F0),            // Slate-200 — overlay tint
    primary = Color(0xFF0891B2),               // Cyan-600 — deeper for contrast
    primaryVariant = Color(0xFF0E7490),         // Cyan-700 — even deeper
    secondary = Color(0xFF7C3AED),             // Violet-600 — deep purple
    accent = Color(0xFFE11D48),               // Rose-600 — strong alarm
    warning = Color(0xFFD97706),              // Amber-600 — darker caution
    textPrimary = Color(0xFF0F172A),           // Slate-900 — dark charcoal
    textSecondary = Color(0xFF64748B),         // Slate-500 — balanced gray
    success = Color(0xFF059669),              // Emerald-600 — darker green
    isDark = false,
    primaryGradientStart = Color(0xFF0891B2),   // Cyan-600
    primaryGradientEnd = Color(0xFF0E7490),     // Cyan-700
    accentGradientStart = Color(0xFF7C3AED),    // Violet-600
    accentGradientEnd = Color(0xFF6D28D9)       // Violet-700
)

val LocalPepLogColors = staticCompositionLocalOf { DarkColors }
