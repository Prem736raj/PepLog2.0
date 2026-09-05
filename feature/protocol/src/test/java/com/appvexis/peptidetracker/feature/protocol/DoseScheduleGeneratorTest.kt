package com.appvexis.peptidetracker.feature.protocol

import com.appvexis.peptidetracker.core.model.AdminRoute
import com.appvexis.peptidetracker.core.model.DoseStatus
import com.appvexis.peptidetracker.core.model.DoseUnit
import com.appvexis.peptidetracker.core.model.FrequencyType
import com.appvexis.peptidetracker.core.model.ProtocolCompound
import com.appvexis.peptidetracker.core.model.TitrationStep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class DoseScheduleGeneratorTest {
    private val zone = ZoneOffset.UTC
    private val start = LocalDate.of(2026, 1, 1)
        .atStartOfDay(zone)
        .toInstant()
        .toEpochMilli()

    @Test
    fun dailyScheduleIsBoundedAndUsesTheCompoundIdentity() {
        val compound = compound(FrequencyType.DAILY)

        val result = DoseScheduleGenerator.generate(compound, start, zone)

        assertEquals(365, result.size)
        assertTrue(result.all { it.protocolCompoundId == "compound-id" })
        assertTrue(result.all { it.status == DoseStatus.PENDING })
        assertEquals(DoseUnit.MCG, result.first().doseUnit)
    }

    @Test
    fun weeklyScheduleUsesTheStartWeekday() {
        val compound = compound(FrequencyType.WEEKLY)

        val result = DoseScheduleGenerator.generate(compound, start, zone)

        assertEquals(53, result.size)
    }

    @Test
    fun scheduleUsesChosenTimeAndTitrationDoseByWeek() {
        val compound = compound(FrequencyType.DAILY).copy(
            timeOfDay = "22:30",
            titrationEnabled = true,
            titrationSchedule = listOf(
                TitrationStep(week = 1, doseAmount = 100.0),
                TitrationStep(week = 2, doseAmount = 200.0),
            ),
        )

        val result = DoseScheduleGenerator.generate(compound, start, zone)

        assertEquals(22, java.time.Instant.ofEpochMilli(result.first().scheduledTime)
            .atZone(zone).hour)
        assertEquals(30, java.time.Instant.ofEpochMilli(result.first().scheduledTime)
            .atZone(zone).minute)
        assertEquals(100.0, result[0].doseAmount, 0.0)
        assertEquals(200.0, result[7].doseAmount, 0.0)
    }

    private fun compound(frequencyType: FrequencyType) = ProtocolCompound(
        id = "compound-id",
        protocolId = "protocol-id",
        peptideId = "peptide-id",
        doseAmount = 250.0,
        doseUnit = DoseUnit.MCG,
        frequencyType = frequencyType,
        frequencyDays = null,
        timeOfDay = "Morning",
        adminRoute = AdminRoute.SUBQ,
        startDate = start,
        endDate = null,
        notes = null
    )
}
