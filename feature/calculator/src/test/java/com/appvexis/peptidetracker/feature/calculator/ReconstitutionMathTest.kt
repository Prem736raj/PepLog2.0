package com.appvexis.peptidetracker.feature.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReconstitutionMathTest {

    @Test
    fun `5 mg in 2 mL with 250 mcg dose produces 10 U draw`() {
        val result = ReconstitutionMath.calculate(
            vialStrengthMg = 5.0,
            bacWaterMl = 2.0,
            desiredDoseMg = 0.250,
            syringeType = SyringeType.U100_INSULIN
        )!!

        assertEquals(2.5, result.concentrationMgMl, 1e-9)
        assertEquals(0.1, result.drawVolumeMl!!, 1e-9)
        assertEquals(10.0, result.syringeUnits!!, 1e-9)
        assertEquals(20L, result.totalDosesPerVial)
        assertTrue(result.isValidForDrawing)
    }

    @Test
    fun `dose larger than vial contents is rejected`() {
        val result = ReconstitutionMath.calculate(
            vialStrengthMg = 5.0,
            bacWaterMl = 2.0,
            desiredDoseMg = 6.0,
            syringeType = SyringeType.U100_INSULIN
        )!!

        assertTrue(result.isOverDose)
        assertNull(result.drawVolumeMl)
        assertNull(result.syringeUnits)
        assertFalse(result.isValidForDrawing)
    }

    @Test
    fun `half mL syringe flags a draw above 50 units`() {
        val result = ReconstitutionMath.calculate(
            vialStrengthMg = 5.0,
            bacWaterMl = 2.0,
            desiredDoseMg = 1.5,
            syringeType = SyringeType.U100_HALF
        )!!

        assertEquals(0.6, result.drawVolumeMl!!, 1e-9)
        assertEquals(60.0, result.syringeUnits!!, 1e-9)
        assertTrue(result.isSyringeCapacityExceeded)
        assertFalse(result.isValidForDrawing)
    }

    @Test
    fun `standard syringe does not invent U100 units`() {
        val result = ReconstitutionMath.calculate(
            vialStrengthMg = 5.0,
            bacWaterMl = 2.0,
            desiredDoseMg = 0.25,
            syringeType = SyringeType.STANDARD_1ML
        )!!

        assertEquals(0.1, result.drawVolumeMl!!, 1e-9)
        assertNull(result.syringeUnits)
    }

    @Test
    fun `zero and non finite inputs do not produce a calculation`() {
        assertNull(ReconstitutionMath.calculate(0.0, 2.0, 0.25, SyringeType.U100_INSULIN))
        assertNull(ReconstitutionMath.calculate(5.0, Double.POSITIVE_INFINITY, 0.25, SyringeType.U100_INSULIN))
    }
}
