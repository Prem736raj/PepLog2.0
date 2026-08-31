package com.appvexis.peptidetracker.navigation

import androidx.lifecycle.ViewModel
import com.appvexis.peptidetracker.core.billing.SubscriptionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Supplies the cached, Play-refreshed entitlement state to navigation.
 * Keeping this at the app shell prevents premium routes from being reachable
 * only because a user skipped a visual paywall.
 */
@HiltViewModel
class SubscriptionAccessViewModel @Inject constructor(
    subscriptionManager: SubscriptionManager
) : ViewModel() {
    val isPremium: StateFlow<Boolean> = subscriptionManager.isPremium
}
