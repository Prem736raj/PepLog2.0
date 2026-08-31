package com.appvexis.peptidetracker.core.billing

/**
 * Central configuration for Google Play Billing product IDs and pricing display.
 * Product IDs must match exactly with Google Play Console subscription configurations.
 *
 * Trial configuration and prices are controlled by Google Play Console. The
 * app must display Play's returned offer terms at purchase time.
 */
object BillingConfig {

    // ---- Product IDs (must match Play Console) ---- //
    const val PRODUCT_ID_YEARLY = "peplog_premium_yearly"
    const val PRODUCT_ID_MONTHLY = "peplog_premium_monthly"
    const val PRODUCT_ID_WEEKLY = "peplog_premium_weekly"

    /** All subscription product IDs to query from Play. */
    val ALL_PRODUCT_IDS = listOf(PRODUCT_ID_YEARLY, PRODUCT_ID_MONTHLY, PRODUCT_ID_WEEKLY)

    // ---- DataStore Keys ---- //
    const val PREF_KEY_IS_PREMIUM = "is_premium_cached"
    const val PREF_KEY_SUBSCRIPTION_PRODUCT = "subscription_product_id"
}
