package com.appvexis.peptidetracker.feature.protocol

import com.appvexis.peptidetracker.core.model.DoseLog
import com.appvexis.peptidetracker.core.model.DoseStatus
import com.appvexis.peptidetracker.core.model.FrequencyType
import com.appvexis.peptidetracker.core.model.ProtocolCompound
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException
import java.util.UUID

/** Creates a finite, timezone-aware set of pending dose rows for a new compound. */
object DoseScheduleGenerator {
    /** Keep enough future rows for normal use without creating an unbounded database. */
    const val HORIZON_DAYS = 365L

    fun generate(
        compound: ProtocolCompound,
        now: Long,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): List<DoseLog> {
        val start = compound.startDate ?: now
        val end = compound.endDate ?: Instant.ofEpochMilli(start)
            .atZone(zoneId)
            .toLocalDate()
            .plusDays(HORIZON_DAYS - 1)
            .atTime(LocalTime.MAX)
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli()
        val firstDate = Instant.ofEpochMilli(start).atZone(zoneId).toLocalDate()
        val lastDate = Instant.ofEpochMilli(end).atZone(zoneId).toLocalDate()
        val time = parseTimeOfDay(compound.timeOfDay)

        return generateSequence(firstDate) { it.plusDays(1) }
            .takeWhile { it <= lastDate }
            .mapIndexedNotNull { dayIndex, date ->
                if (!isScheduled(compound, date, firstDate, dayIndex)) return@mapIndexedNotNull null
                val scheduled = ZonedDateTime.of(date, time, zoneId).toInstant().toEpochMilli()
                if (scheduled < start || scheduled > end) return@mapIndexedNotNull null
                DoseLog(
                    id = UUID.randomUUID().toString(),
                    protocolCompoundId = compound.id,
                    scheduledTime = scheduled,
                    actualTime = null,
                    doseAmount = compound.doseAmount,
                    doseUnit = compound.doseUnit,
                    status = DoseStatus.PENDING,
                    injectionSite = null,
                    injectionSide = null,
                    notes = compound.notes,
                    createdAt = now
                )
            }
            .toList()
    }

    private fun isScheduled(
        compound: ProtocolCompound,
        date: LocalDate,
        firstDate: LocalDate,
        dayIndex: Int
    ): Boolean = when (compound.frequencyType) {
        FrequencyType.DAILY -> true
        FrequencyType.WEEKLY -> date.dayOfWeek == firstDate.dayOfWeek
        FrequencyType.CUSTOM -> {
            // The stored convention is Sunday=1, Monday=2, ..., Saturday=7.
            val sundayBasedDay = (date.dayOfWeek.value % 7) + 1
            compound.frequencyDays.orEmpty().contains(sundayBasedDay)
        }
        FrequencyType.CYCLE -> {
            val onDays = compound.frequencyDays?.getOrNull(0)?.coerceAtLeast(1) ?: 5
            val offDays = compound.frequencyDays?.getOrNull(1)?.coerceAtLeast(0) ?: 2
            dayIndex % (onDays + offDays) < onDays
        }
    }

    private fun parseTimeOfDay(value: String): LocalTime {
        return when (value.trim().lowercase()) {
            "morning" -> LocalTime.of(8, 0)
            "afternoon" -> LocalTime.of(13, 0)
            "evening" -> LocalTime.of(18, 0)
            "night", "bedtime" -> LocalTime.of(22, 0)
            else -> try {
                LocalTime.parse(value)
            } catch (_: DateTimeParseException) {
                LocalTime.of(8, 0)
            }
        }
    }
}
