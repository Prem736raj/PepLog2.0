package com.appvexis.peptidetracker.core.database.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.appvexis.peptidetracker.core.model.ProtocolWithCompounds

/**
 * Room Composite Entity linking a protocol stack with its mapped compounds.
 */
data class ProtocolWithCompoundsEntity(
    @Embedded 
    val protocol: ProtocolEntity,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "protocol_id"
    )
    val compounds: List<ProtocolCompoundEntity>
)

fun ProtocolWithCompoundsEntity.toDomain() = ProtocolWithCompounds(
    protocol = protocol.toDomain(),
    // Inactive compounds remain in Room for dose-history referential
    // integrity, but should not appear in active protocol editing surfaces.
    compounds = compounds.filter { it.isActive }.map { it.toDomain() }
)
