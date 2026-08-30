package com.appvexis.peptidetracker.feature.pkcurves.engine

import com.appvexis.peptidetracker.feature.pkcurves.model.PKCurvePoint
import com.appvexis.peptidetracker.feature.pkcurves.model.PKMarker
import kotlin.math.exp

/**
 * First-order elimination estimator used for educational visualization.
 *
 * The output is an estimated relative level derived from user-entered dose events
 * and a half-life. It is not a measured blood/serum concentration and intentionally
 * does not model absorption, distribution volume, bioavailability, metabolism,
 * individual variation, or active metabolites.
 */
object PKEngine {

    private const val LN_2 = 0.693147180559945
    private const val MAX_RESOLUTION = 5_000
    private const val LOOKBACK_HALF_LIVES = 10.0

    fun eliminationConstant(halfLifeHours: Double): Double {
        if (!halfLifeHours.isFinite() || halfLifeHours <= 0.0) return 0.0
        return LN_2 / halfLifeHours
    }

    fun singleDoseConcentration(c0: Double, kEl: Double, t: Double): Double {
        if (!c0.isFinite() || c0 < 0.0 || !kEl.isFinite() || kEl <= 0.0 || !t.isFinite() || t < 0.0) {
            return 0.0
        }
        val result = c0 * exp(-kEl * t)
        return result.takeIf { it.isFinite() && it >= 0.0 } ?: 0.0
    }

    fun computeSuperpositionCurve(
        doseTimesHours: List<Double>,
        doseAmounts: List<Double>,
        halfLifeHours: Double,
        windowStartHours: Double,
        windowEndHours: Double,
        resolution: Int = 500
    ): List<PKCurvePoint> {
        if (!inputsAreValid(doseTimesHours, doseAmounts, halfLifeHours)) return emptyList()
        if (!windowStartHours.isFinite() || !windowEndHours.isFinite() || windowEndHours <= windowStartHours) {
            return emptyList()
        }
        if (resolution !in 2..MAX_RESOLUTION) return emptyList()

        val kEl = eliminationConstant(halfLifeHours)
        if (kEl <= 0.0) return emptyList()

        val windowDuration = windowEndHours - windowStartHours
        val step = windowDuration / resolution.toDouble()
        val lookbackHours = halfLifeHours * LOOKBACK_HALF_LIVES
        if (!step.isFinite() || !lookbackHours.isFinite()) return emptyList()

        return List(resolution + 1) { i ->
            val t = windowStartHours + i * step
            var total = 0.0
            for (j in doseTimesHours.indices) {
                val elapsed = t - doseTimesHours[j]
                if (elapsed.isFinite() && elapsed in 0.0..lookbackHours) {
                    total += singleDoseConcentration(doseAmounts[j], kEl, elapsed)
                }
            }
            PKCurvePoint(
                timeHours = t - windowStartHours,
                concentration = total.takeIf { it.isFinite() && it >= 0.0 } ?: 0.0
            )
        }
    }

    fun detectMarkers(
        points: List<PKCurvePoint>,
        minProminence: Double = 0.01
    ): List<PKMarker> {
        if (points.size < 3 || !minProminence.isFinite() || minProminence < 0.0) return emptyList()
        if (points.any { !it.timeHours.isFinite() || !it.concentration.isFinite() || it.concentration < 0.0 }) {
            return emptyList()
        }

        val markers = mutableListOf<PKMarker>()
        for (i in 1 until points.lastIndex) {
            val prev = points[i - 1].concentration
            val curr = points[i].concentration
            val next = points[i + 1].concentration
            when {
                curr > prev && curr > next && curr > minProminence -> markers += PKMarker(
                    timeHours = points[i].timeHours,
                    concentration = curr,
                    isPeak = true
                )
                curr < prev && curr < next && curr > minProminence * 0.1 -> markers += PKMarker(
                    timeHours = points[i].timeHours,
                    concentration = curr,
                    isPeak = false
                )
            }
        }

        val windowSpan = points.last().timeHours.coerceAtLeast(0.0)
        val minGap = windowSpan * 0.01
        val deduped = mutableListOf<PKMarker>()
        for (marker in markers) {
            if (deduped.isEmpty() || marker.timeHours - deduped.last().timeHours > minGap) {
                deduped += marker
            }
        }
        return deduped
    }

    fun concentrationAt(
        doseTimesHours: List<Double>,
        doseAmounts: List<Double>,
        halfLifeHours: Double,
        timeHours: Double
    ): Double {
        if (!inputsAreValid(doseTimesHours, doseAmounts, halfLifeHours) || !timeHours.isFinite()) return 0.0
        val kEl = eliminationConstant(halfLifeHours)
        val lookbackHours = halfLifeHours * LOOKBACK_HALF_LIVES
        if (kEl <= 0.0 || !lookbackHours.isFinite()) return 0.0

        var total = 0.0
        for (j in doseTimesHours.indices) {
            val elapsed = timeHours - doseTimesHours[j]
            if (elapsed.isFinite() && elapsed in 0.0..lookbackHours) {
                total += singleDoseConcentration(doseAmounts[j], kEl, elapsed)
            }
        }
        return total.takeIf { it.isFinite() && it >= 0.0 } ?: 0.0
    }

    fun maxConcentration(curves: List<List<PKCurvePoint>>): Double {
        val max = curves.asSequence()
            .flatten()
            .map { it.concentration }
            .filter { it.isFinite() && it >= 0.0 }
            .maxOrNull()
        return max?.takeIf { it > 0.0 } ?: 1.0
    }

    private fun inputsAreValid(
        doseTimesHours: List<Double>,
        doseAmounts: List<Double>,
        halfLifeHours: Double
    ): Boolean =
        doseTimesHours.isNotEmpty() &&
            doseTimesHours.size == doseAmounts.size &&
            halfLifeHours.isFinite() && halfLifeHours > 0.0 &&
            doseTimesHours.all { it.isFinite() } &&
            doseAmounts.all { it.isFinite() && it >= 0.0 }
}
