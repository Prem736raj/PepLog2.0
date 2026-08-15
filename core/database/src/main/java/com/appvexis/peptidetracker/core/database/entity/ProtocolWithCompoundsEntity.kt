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
    compounds = compounds.map { it.toDomain() }
)
