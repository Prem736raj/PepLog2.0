package com.appvexis.peptidetracker.core.model

import kotlinx.serialization.Serializable

/**
 * A protocol compound with the identity needed to create a valid dose log.
 *
 * UI code must pass [id] through to the database. Display names are deliberately
 * kept separate so a peptide's label can never be mistaken for a foreign key.
 */
@Serializable
data class LoggableCompound(
    val id: String,
    val protocolId: String,
    val peptideId: String,
    val name: String,
    val doseAmount: Double,
    val doseUnit: DoseUnit
)
