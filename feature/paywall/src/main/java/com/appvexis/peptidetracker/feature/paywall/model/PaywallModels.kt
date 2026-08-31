package com.appvexis.peptidetracker.feature.paywall.model

/**
 * UI state for the Paywall screen.
 */
data class PaywallUiState(
    val isLoading: Boolean = true,
    val isPremium: Boolean = false,
    val selectedPlan: SubscriptionPlan = SubscriptionPlan.YEARLY,
    val plans: List<PlanDisplay> = defaultPlans(),
    val error: String? = null
)

/**
 * Subscription plan options.
 */
enum class SubscriptionPlan {
    WEEKLY,
    MONTHLY,
    YEARLY
}

/**
 * Display model for a subscription plan in the paywall UI.
 */
data class PlanDisplay(
    val plan: SubscriptionPlan,
    val title: String,
    val price: String,
    val pricePerUnit: String,     // e.g., "₹13.4/week" for yearly
    val badge: String? = null,    // e.g., "Best Value", "Most Popular"
    val hasTrial: Boolean = false,
    val trialText: String? = null,
    val savings: String? = null   // e.g., "Save 80%"
)

/**
 * Default plan display data used until Play returns localized prices.
 */
fun defaultPlans(): List<PlanDisplay> = listOf(
    PlanDisplay(
        plan = SubscriptionPlan.YEARLY,
        title = "Yearly",
        price = "See price in Google Play",
        pricePerUnit = "",
        badge = null,
        hasTrial = false,
        trialText = null,
        savings = null
    ),
    PlanDisplay(
        plan = SubscriptionPlan.MONTHLY,
        title = "Monthly",
        price = "See price in Google Play",
        pricePerUnit = "",
        badge = null,
        hasTrial = false,
        savings = null
    ),
    PlanDisplay(
        plan = SubscriptionPlan.WEEKLY,
        title = "Weekly",
        price = "See price in Google Play",
        pricePerUnit = "",
        badge = null,
        hasTrial = false,
        savings = null
    )
)
