package com.appvexis.peptidetracker.feature.injection.rotation

import com.appvexis.peptidetracker.core.model.HealingStatus
import com.appvexis.peptidetracker.core.model.InjectionSiteArea
import com.appvexis.peptidetracker.feature.injection.model.ReadinessLevel
import com.appvexis.peptidetracker.feature.injection.model.SiteStatus

/**
 * Injection site rotation assistant.
 * Suggests the optimal next injection site based on usage history,
 * healing status, and rotation best practices.
 *
 * Algorithm priorities:
 * 1. Sites with healing issues (BRUISED, LUMP, REDNESS) are EXCLUDED
 * 2. UNUSED sites are preferred (diversify usage)
 * 3. READY sites (7+ days rest) ranked by longest rest period
 * 4. HEALING sites (2-7 days) ranked by longest rest period
 * 5. RECENT sites are last resort
 * 6. Alternates sides (left/right) when possible
 */
object RotationSuggestionEngine {

    /**
     * Returns a ranked list of suggested injection sites (best first).
     * @param siteStatuses Current status of all sites
     * @param lastUsedSide "left" or "right" from the most recent injection — used for alternation
     * @param preferredCategory "SubQ" or "IM" — filter to only relevant route
     */
    fun suggestNextSites(
        siteStatuses: Map<InjectionSiteArea, SiteStatus>,
        lastUsedSide: String? = null,
        preferredCategory: String? = null
    ): List<SuggestionResult> {
        val allAreas = InjectionSiteArea.entries
            .filter { area ->
                // Filter by admin route category if specified
                preferredCategory == null || area.category == preferredCategory
            }

        val scored = allAreas.map { area ->
            val status = siteStatuses[area]
            val score = calculateScore(status, area, lastUsedSide)
            SuggestionResult(
                area = area,
                score = score,
                reason = buildReason(status, area, lastUsedSide),
                isExcluded = status?.healingStatus != null &&
                        status.healingStatus != HealingStatus.OK
            )
        }

        return scored
            .filter { !it.isExcluded }
            .sortedByDescending { it.score }
    }

    /**
     * Gets the single best suggestion.
     */
    fun getBestSuggestion(
        siteStatuses: Map<InjectionSiteArea, SiteStatus>,
        lastUsedSide: String? = null,
        preferredCategory: String? = null
    ): SuggestionResult? {
        return suggestNextSites(siteStatuses, lastUsedSide, preferredCategory).firstOrNull()
    }

    /**
     * Calculates a rotation score for a site (higher = better choice).
     */
    private fun calculateScore(
        status: SiteStatus?,
        area: InjectionSiteArea,
        lastUsedSide: String?
    ): Int {
        var score = 0

        // Base score by readiness level
        val readiness = status?.readinessLevel ?: ReadinessLevel.UNUSED
        score += when (readiness) {
            ReadinessLevel.UNUSED -> 100   // Never used — highest priority
            ReadinessLevel.READY -> 80     // 7+ days rest — great
            ReadinessLevel.HEALING -> 40   // 2-7 days — okay if needed
            ReadinessLevel.RECENT -> 10    // Within 48h — avoid if possible
        }

        // Bonus for days since last use (more rest = better)
        val daysSince = status?.daysSinceLastUse
        if (daysSince != null) {
            score += minOf(daysSince * 3, 30) // Cap at 30 bonus points
        }

        // Side alternation bonus: prefer opposite side from last injection
        if (lastUsedSide != null) {
            val areaName = area.name.lowercase()
            val isOpposite = when (lastUsedSide.lowercase()) {
                "left" -> areaName.contains("right")
                "right" -> areaName.contains("left")
                else -> false
            }
            if (isOpposite) {
                score += 20  // Bonus for alternating sides
            }
        }

        // Penalty for low usage count diversity (prefer under-used sites)
        val usageCount = status?.usageCount ?: 0
        score -= minOf(usageCount * 2, 20) // Penalty capped at 20

        // Healing issue penalty (should be excluded, but just in case)
        if (status?.healingStatus != HealingStatus.OK && status?.healingStatus != null) {
            score -= 200 // Hard penalty
        }

        return score
    }

    /**
     * Builds a human-readable reason for why this site was suggested.
     */
    private fun buildReason(
        status: SiteStatus?,
        area: InjectionSiteArea,
        lastUsedSide: String?
    ): String {
        val readiness = status?.readinessLevel ?: ReadinessLevel.UNUSED

        val baseReason = when (readiness) {
            ReadinessLevel.UNUSED -> "Never used — fresh site"
            ReadinessLevel.READY -> "Well rested (${status?.daysSinceLastUse ?: 7}+ days)"
            ReadinessLevel.HEALING -> "Healing (${status?.daysSinceLastUse ?: 2} days since last use)"
            ReadinessLevel.RECENT -> "Recently used — consider resting"
        }

        val sideNote = if (lastUsedSide != null) {
            val areaName = area.name.lowercase()
            val isOpposite = when (lastUsedSide.lowercase()) {
                "left" -> areaName.contains("right")
                "right" -> areaName.contains("left")
                else -> false
            }
            if (isOpposite) " • Alternates side ✓" else ""
        } else ""

        return baseReason + sideNote
    }
}

/**
 * Result of the rotation suggestion algorithm.
 */
data class SuggestionResult(
    val area: InjectionSiteArea,
    val score: Int,
    val reason: String,
    val isExcluded: Boolean = false
)
