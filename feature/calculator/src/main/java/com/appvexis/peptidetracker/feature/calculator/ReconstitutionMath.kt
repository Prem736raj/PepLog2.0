package com.appvexis.peptidetracker.feature.calculator

import kotlin.math.floor

/**
 * Pure arithmetic used by the reconstitution UI.
 *
 * This performs unit conversion and concentration math only. It does not select,
 * recommend, validate, or prescribe a clinical dose.
 */
object ReconstitutionMath {

    fun calculate(
        vialStrengthMg: Double?,
        bacWaterMl: Double?,
        desiredDoseMg: Double?,
        syringeType: SyringeType
    ): CalculationResult? {
        if (!vialStrengthMg.isPositiveFinite() || !bacWaterMl.isPositiveFinite()) {
            return null
        }

        val concentration = vialStrengthMg!! / bacWaterMl!!
        if (!concentration.isPositiveFinite()) return null

        if (!desiredDoseMg.isPositiveFinite()) {
            return CalculationResult(concentrationMgMl = concentration)
        }

        val doseMg = desiredDoseMg!!
        val isOverDose = doseMg > vialStrengthMg
        val drawVolume = if (!isOverDose) doseMg / concentration else null
        val isCapacityExceeded = drawVolume != null && drawVolume > syringeType.capacityMl + EPSILON

        val syringeUnits = if (
            drawVolume != null && syringeType.usesU100Units
        ) {
            drawVolume * U100_UNITS_PER_ML
        } else {
            null
        }

        val doseCount = if (!isOverDose) safeWholeDoseCount(vialStrengthMg / doseMg) else null

        return CalculationResult(
            concentrationMgMl = concentration,
            drawVolumeMl = drawVolume,
            syringeUnits = syringeUnits,
            totalDosesPerVial = doseCount,
            isOverDose = isOverDose,
            isSyringeCapacityExceeded = isCapacityExceeded
        )
    }

    private fun safeWholeDoseCount(value: Double): Long? {
        if (!value.isFinite() || value < 0.0 || value > Long.MAX_VALUE.toDouble()) return null
        return floor(value).toLong()
    }

    private fun Double?.isPositiveFinite(): Boolean =
        this != null && isFinite() && this > 0.0

    private const val U100_UNITS_PER_ML = 100.0
    private const val EPSILON = 1e-12
}
