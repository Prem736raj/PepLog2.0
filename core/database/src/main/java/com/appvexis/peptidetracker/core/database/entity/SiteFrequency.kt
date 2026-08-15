package com.appvexis.peptidetracker.core.database.entity

import androidx.room.ColumnInfo

/**
 * Projection class for injection site frequency aggregation query.
 */
data class SiteFrequency(
    @ColumnInfo(name = "body_area")
    val bodyArea: String,
    val count: Int
)
