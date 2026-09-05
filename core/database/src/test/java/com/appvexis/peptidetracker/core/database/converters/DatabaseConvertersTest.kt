package com.appvexis.peptidetracker.core.database.converters

import com.appvexis.peptidetracker.core.model.DoseStatus
import com.appvexis.peptidetracker.core.model.DoseUnit
import com.appvexis.peptidetracker.core.model.FrequencyType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DatabaseConvertersTest {
    private val converters = DatabaseConverters()

    @Test
    fun unknownPersistedEnumsUseSafeDefaults() {
        assertEquals(DoseStatus.SKIPPED, converters.toDoseStatus("removed_status"))
        assertEquals(DoseUnit.IU, converters.toDoseUnit("removed_unit"))
        assertEquals(FrequencyType.DAILY, converters.toFrequencyType("removed_frequency"))
    }

    @Test
    fun malformedCollectionDataDoesNotCrashRoomReads() {
        assertNull(converters.toIntList("not-json"))
        assertNull(converters.toStringDoubleMap("not-json"))
    }
}
