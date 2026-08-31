package com.appvexis.peptidetracker.feature.pkcurves.engine

import com.appvexis.peptidetracker.feature.pkcurves.model.PKCurvePoint
import com.appvexis.peptidetracker.feature.pkcurves.model.PKMarker
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max

/**
 * Pharmacokinetic computation engine.
 *
 * Models first-order elimination kinetics:
 *   C(t) = C₀ × e^(-k_el × t)
 * where k_el = ln(2) / t½
 *
 * Supports multi-dose superposition: at each time point,
 * the total concentration is the sum of residual concentrations
 * from every prior dose injection.
 */
object PKEngine {

    private const val LN_2 = 0.693147180559945

    /**
     * Compute the decay constant (elimination rate constant) from half-life.
     */
    fun eliminationConstant(halfLifeHours: Double): Double {
        if (halfLifeHours <= 0.0) return 0.0
        return LN_2 / halfLifeHours
    }

    /**
     * Single-dose exponential decay at time t after injection.
     * @param c0 initial concentration (dose amount, normalized)
     * @param kEl elimination rate constant
     * @param t hours elapsed since injection
     */
    fun singleDoseConcentration(c0: Double, kEl: Double, t: Double): Double {
        if (t < 0.0) return 0.0
        return c0 * exp(-kEl * t)
    }

    /**
     * Compute the superimposed PK curve for multiple dose events.
     *
     * @param doseTimesHours list of hours-since-epoch for each dose event (sorted ascending)
     * @param doseAmounts corresponding dose amounts for each event
     * @param halfLifeHours half-life in hours
     * @param windowStartHours the start of the visible time window (hours since epoch)
     * @param windowEndHours the end of the visible time window (hours since epoch)
     * @param resolution number of points to compute across the window
     * @return list of PKCurvePoint with concentrations from superposition
     */
    fun computeSuperpositionCurve(
        doseTimesHours: List<Double>,
        doseAmounts: List<Double>,
        halfLifeHours: Double,
        windowStartHours: Double,
        windowEndHours: Double,
        resolution: Int = 500
    ): List<PKCurvePoint> {
        if (doseTimesHours.isEmpty() || doseTimesHours.size != doseAmounts.size ||
            !halfLifeHours.isFinite() || halfLifeHours <= 0.0 || resolution < 1) return emptyList()

        val kEl = eliminationConstant(halfLifeHours)
        val windowDuration = windowEndHours - windowStartHours
        if (windowDuration <= 0.0) return emptyList()

        val step = windowDuration / resolution
        val points = mutableListOf<PKCurvePoint>()

        // Only consider doses that could still contribute
        // A dose's contribution drops below ~0.1% after 10 half-lives
        val lookbackHours = halfLifeHours * 10.0

        for (i in 0..resolution) {
            val t = windowStartHours + i * step
            var totalConcentration = 0.0

            for (j in doseTimesHours.indices) {
                val doseTime = doseTimesHours[j]
                val elapsed = t - doseTime
                // Only include doses that happened before time t and are within lookback window
                if (elapsed >= 0.0 && elapsed <= lookbackHours) {
                    totalConcentration += singleDoseConcentration(doseAmounts[j], kEl, elapsed)
                }
            }

            points.add(
                PKCurvePoint(
                    timeHours = t - windowStartHours, // normalize to window-relative
                    concentration = totalConcentration
                )
            )
        }

        return points
    }

    /**
     * Detect peaks and troughs from a computed curve.
     * A peak is a local maximum, a trough is a local minimum.
     * We also identify global peak and final trough.
     *
     * @param points the curve points (must be sorted by timeHours)
     * @param minProminence minimum concentration delta to be considered a significant peak/trough
     */
    fun detectMarkers(
        points: List<PKCurvePoint>,
        minProminence: Double = 0.01
    ): List<PKMarker> {
        if (points.size < 3) return emptyList()

        val markers = mutableListOf<PKMarker>()

        for (i in 1 until points.size - 1) {
            val prev = points[i - 1].concentration
            val curr = points[i].concentration
            val next = points[i + 1].concentration

            // Peak detection: current is higher than both neighbors
            if (curr > prev && curr > next && curr > minProminence) {
                markers.add(
                    PKMarker(
                        timeHours = points[i].timeHours,
                        concentration = curr,
                        isPeak = true
                    )
                )
            }
            // Trough detection: current is lower than both neighbors
            else if (curr < prev && curr < next && curr > minProminence * 0.1) {
                markers.add(
                    PKMarker(
                        timeHours = points[i].timeHours,
                        concentration = curr,
                        isPeak = false
                    )
                )
            }
        }

        // Deduplicate closely spaced markers (within 1% of the window)
        val windowSpan = points.lastOrNull()?.timeHours ?: 1.0
        val minGap = windowSpan * 0.01
        return markers.filterIndexed { index, marker ->
            if (index == 0) true
            else (marker.timeHours - markers[index - 1].timeHours) > minGap
        }
    }

    /**
     * Compute concentration at a specific point in time (for crosshair display).
     */
    fun concentrationAt(
        doseTimesHours: List<Double>,
        doseAmounts: List<Double>,
        halfLifeHours: Double,
        timeHours: Double
    ): Double {
        if (doseTimesHours.isEmpty() || doseTimesHours.size != doseAmounts.size ||
            !halfLifeHours.isFinite() || halfLifeHours <= 0.0) return 0.0
        val kEl = eliminationConstant(halfLifeHours)
        val lookbackHours = halfLifeHours * 10.0

        var total = 0.0
        for (j in doseTimesHours.indices) {
            val elapsed = timeHours - doseTimesHours[j]
            if (elapsed >= 0.0 && elapsed <= lookbackHours) {
                total += singleDoseConcentration(doseAmounts[j], kEl, elapsed)
            }
        }
        return total
    }

    /**
     * Find the maximum concentration across all curve points.
     */
    fun maxConcentration(curves: List<List<PKCurvePoint>>): Double {
        return curves.flatMap { it }.maxOfOrNull { it.concentration } ?: 1.0
    }
}
