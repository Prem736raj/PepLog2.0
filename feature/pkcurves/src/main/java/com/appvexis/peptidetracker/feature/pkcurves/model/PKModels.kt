package com.appvexis.peptidetracker.feature.pkcurves.model

import androidx.compose.ui.graphics.Color

/**
 * Time window options for the PK curve visualization X-axis.
 */
enum class TimeWindow(val label: String, val hours: Double) {
    HOURS_24("24h", 24.0),
    DAYS_7("7d", 168.0),
    DAYS_30("30d", 720.0)
}

/**
 * A single point on a pharmacokinetic decay curve.
 * @param timeHours hours elapsed from the reference start
 * @param concentration relative concentration (0.0 — 1.0+, can exceed 1.0 for superposition)
 */
data class PKCurvePoint(
    val timeHours: Double,
    val concentration: Double
)

/**
 * A peak or trough marker on the curve.
 */
data class PKMarker(
    val timeHours: Double,
    val concentration: Double,
    val isPeak: Boolean // true = peak, false = trough
)

/**
 * All computed curve data for a single compound within a protocol.
 */
data class CompoundCurveData(
    val compoundId: String,
    val peptideId: String,
    val peptideName: String,
    val halfLifeHours: Double,
    val doseAmountMg: Double,
    val doseUnit: String,
    val color: Color,
    val points: List<PKCurvePoint>,
    val markers: List<PKMarker>,
    val currentLevel: Double, // current concentration at "now"
    val isVisible: Boolean = true
)

/**
 * Chart color palette for up to 8 simultaneous compounds.
 */
object PKChartColors {
    val palette = listOf(
        Color(0xFF5E9D90), // Mineral teal
        Color(0xFFC39755), // Ochre
        Color(0xFF7A8C83), // Sage
        Color(0xFFC66F52), // Terracotta
        Color(0xFF7C9A8B), // Sage
        Color(0xFF6F92A0), // Slate blue
        Color(0xFF9B7381), // Dusty plum
        Color(0xFFB9A06A)  // Soft gold
    )

    fun getColor(index: Int): Color = palette[index % palette.size]
}

/**
 * Full UI state for the PK Visualizer screen.
 */
data class PKVisualizerUiState(
    val isLoading: Boolean = true,
    val activeTimeWindow: TimeWindow = TimeWindow.DAYS_7,
    val compounds: List<CompoundCurveData> = emptyList(),
    val hasData: Boolean = false,
    val animationProgress: Float = 0f,
    val crosshairTimeHours: Double? = null, // null = no crosshair
    val maxConcentration: Double = 1.0
)
