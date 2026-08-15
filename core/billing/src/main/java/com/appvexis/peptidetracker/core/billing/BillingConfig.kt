package com.appvexis.peptidetracker.core.billing

/**
 * Central configuration for Google Play Billing product IDs and pricing display.
 * Product IDs must match exactly with Google Play Console subscription configurations.
 *
 * Trial Configuration (set in Play Console):
 * - Yearly plan: 3-day free trial → ₹699/year auto-renewal
 * - Monthly plan: No trial → ₹139/month
 * - Weekly plan: No trial → ₹69/week
 */
object BillingConfig {

    // ---- Product IDs (must match Play Console) ---- //
    const val PRODUCT_ID_YEARLY = "peplog_premium_yearly"
    const val PRODUCT_ID_MONTHLY = "peplog_premium_monthly"
    const val PRODUCT_ID_WEEKLY = "peplog_premium_weekly"

    /** All subscription product IDs to query from Play. */
    val ALL_PRODUCT_IDS = listOf(PRODUCT_ID_YEARLY, PRODUCT_ID_MONTHLY, PRODUCT_ID_WEEKLY)

    // ---- Display Pricing (fallback when Play Console price unavailable) ---- //
    const val DISPLAY_PRICE_YEARLY = "₹699/year"
    const val DISPLAY_PRICE_MONTHLY = "₹139/month"
    const val DISPLAY_PRICE_WEEKLY = "₹69/week"

    // ---- Trial ---- //
    const val TRIAL_DAYS = 3

    // ---- DataStore Keys ---- //
    const val PREF_KEY_IS_PREMIUM = "is_premium_cached"
    const val PREF_KEY_SUBSCRIPTION_EXPIRY = "subscription_expiry_ms"
    const val PREF_KEY_SUBSCRIPTION_PRODUCT = "subscription_product_id"
}
