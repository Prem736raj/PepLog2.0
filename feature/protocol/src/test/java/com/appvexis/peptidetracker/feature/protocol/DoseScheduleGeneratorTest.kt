package com.appvexis.peptidetracker.feature.protocol

import com.appvexis.peptidetracker.core.model.AdminRoute
import com.appvexis.peptidetracker.core.model.DoseStatus
import com.appvexis.peptidetracker.core.model.DoseUnit
import com.appvexis.peptidetracker.core.model.FrequencyType
import com.appvexis.peptidetracker.core.model.ProtocolCompound
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
