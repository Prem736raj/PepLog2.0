package com.appvexis.peptidetracker.feature.pkcurves.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PKEngineTest {

    @Test
    fun concentrationFallsToHalfAtOneHalfLife() {
        val result = PKEngine.concentrationAt(
            doseTimesHours = listOf(0.0),
            doseAmounts = listOf(10.0),
            halfLifeHours = 12.0,
            timeHours = 12.0,
        )

        assertEquals(5.0, result, 0.0001)
    }

    @Test
    fun futureDoseDoesNotContributeBeforeItsScheduledTime() {
        val result = PKEngine.concentrationAt(
            doseTimesHours = listOf(24.0),
            doseAmounts = listOf(10.0),
            halfLifeHours = 12.0,
            timeHours = 12.0,
        )

        assertEquals(0.0, result, 0.0001)
    }

    @Test
    fun superpositionCurveIncludesMultiplePastDoses() {
        val curve = PKEngine.computeSuperpositionCurve(
            doseTimesHours = listOf(0.0, 12.0),
            doseAmounts = listOf(10.0, 10.0),
            halfLifeHours = 12.0,
            windowStartHours = 0.0,
            windowEndHours = 24.0,
            resolution = 24,
        )

        val atTwentyFour = curve.single { it.timeHours == 24.0 }
        assertEquals(7.5, atTwentyFour.concentration, 0.0001)
        assertTrue(curve.maxOf { it.concentration } >= 10.0)
    }
}
