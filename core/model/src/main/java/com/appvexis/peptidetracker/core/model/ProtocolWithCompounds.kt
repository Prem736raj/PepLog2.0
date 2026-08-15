package com.appvexis.peptidetracker.core.model

import kotlinx.serialization.Serializable

/**
 * Composite domain model combining a main protocol with its scheduled compounds.
 */
@Serializable
data class ProtocolWithCompounds(
    val protocol: Protocol,
    val compounds: List<ProtocolCompound>
)
