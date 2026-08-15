package com.appvexis.peptidetracker.core.model

import kotlinx.serialization.Serializable

/**
 * Step detail representing titration ramping rules.
 */
@Serializable
data class TitrationStep(
    val week: Int,
    val doseAmount: Double
)

/**
 * Domain model representing a single peptide compound mapped inside a cycle protocol stack.
 */
@Serializable
data class ProtocolCompound(
    val id: String,
    val protocolId: String,
    val peptideId: String,
    val doseAmount: Double,
    val doseUnit: DoseUnit,
    val frequencyType: FrequencyType,
    val frequencyDays: List<Int>?,         // E.g. [1, 3, 5] for Mon/Wed/Fri where Sunday=1
    val timeOfDay: String,                 // 'morning', 'evening', 'bedtime', or direct time
    val adminRoute: AdminRoute,
    val titrationEnabled: Boolean = false,
    val titrationSchedule: List<TitrationStep>? = null,
    val startDate: Long?,
    val endDate: Long?,
    val isActive: Boolean = true,
    val notes: String?
)
