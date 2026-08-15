package com.appvexis.peptidetracker.core.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Custom spacing grid system (4px/8px-based) to maintain professional visual rhythm.
 */
data class Spacing(
    val none: Dp = 0.dp,
    val extraSmall: Dp = 4.dp,    // Micro spacings, divider margins
    val small: Dp = 8.dp,         // Padding inside smaller chips/badges
    val medium: Dp = 16.dp,       // Core standard content padding
    val large: Dp = 24.dp,        // Standard screen margins
    val extraLarge: Dp = 32.dp,   // Visual spacing between layout cards
    val huge: Dp = 48.dp,         // Section spacing
    val massive: Dp = 64.dp       // Spacing for top-banners/empty illustrations
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }
