package com.appvexis.peptidetracker.feature.injection.model

import com.appvexis.peptidetracker.core.model.HealingStatus
import com.appvexis.peptidetracker.core.model.InjectionSiteArea

/**
 * Represents the visual state of a single injection site zone on the body map.
 * Used by the body map Canvas to render color-coded overlays.
 */
data class SiteStatus(
    val area: InjectionSiteArea,
    val lastUsedTimestamp: Long?,
    val usageCount: Int = 0,
    val healingStatus: HealingStatus = HealingStatus.OK,
    val daysSinceLastUse: Int? = null
) {
    /**
     * Visual classification for the body map overlay.
     * - RECENT: Used within last 48 hours — RED zone (needs rest)
     * - HEALING: Used 2-7 days ago — YELLOW zone (healing)
     * - READY: Not used in 7+ days — GREEN zone (ready to use)
     * - UNUSED: Never used — neutral/default
     */
    val readinessLevel: ReadinessLevel
        get() = when {
            daysSinceLastUse == null -> ReadinessLevel.UNUSED
            daysSinceLastUse <= 2 -> ReadinessLevel.RECENT
            daysSinceLastUse <= 7 -> ReadinessLevel.HEALING
            else -> ReadinessLevel.READY
        }
}

enum class ReadinessLevel {
    RECENT,   // Red — within 48h
    HEALING,  // Yellow — 2-7 days
    READY,    // Green — 7+ days
    UNUSED    // Neutral — never used
}
