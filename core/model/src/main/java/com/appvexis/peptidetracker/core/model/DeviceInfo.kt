package com.appvexis.peptidetracker.core.model

import kotlinx.serialization.Serializable

/**
 * Domain representation of user device information, entitlements, and credits.
 */
@Serializable
data class DeviceInfo(
    val deviceId: String,
    val firstInstallTimestamp: Long,
    val trialCreditsRemaining: Int = 200,
    val isPremium: Boolean = false,
    val subscriptionType: String?, // 'monthly', 'yearly', 'lifetime', null
    val subscriptionExpiry: Long?,
    val googleUserId: String?,
    val googleEmail: String?
) {
    val isTrialActive: Boolean
        get() = trialCreditsRemaining > 0 && !isPremium
}
