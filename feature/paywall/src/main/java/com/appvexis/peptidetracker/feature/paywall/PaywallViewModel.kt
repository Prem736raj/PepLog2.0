package com.appvexis.peptidetracker.feature.paywall

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvexis.peptidetracker.core.billing.BillingConfig
import com.appvexis.peptidetracker.core.billing.SubscriptionManager
import com.appvexis.peptidetracker.feature.paywall.model.PaywallUiState
import com.appvexis.peptidetracker.feature.paywall.model.SubscriptionPlan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val subscriptionManager: SubscriptionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaywallUiState())
    val uiState: StateFlow<PaywallUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Initialize billing connection
            subscriptionManager.initialize()

            // Observe premium state
            launch {
                subscriptionManager.isPremium.collect { premium ->
                    _uiState.update { it.copy(isPremium = premium, isLoading = false) }
                }
            }

            // Observe loading state
            launch {
                subscriptionManager.isLoading.collect { loading ->
                    _uiState.update { it.copy(isLoading = loading) }
                }
            }

            // Observe errors
            launch {
                subscriptionManager.purchaseError.collect { error ->
                    _uiState.update { it.copy(error = error) }
                }
            }
        }
    }

    fun selectPlan(plan: SubscriptionPlan) {
        _uiState.update { it.copy(selectedPlan = plan) }
    }

    fun launchPurchase(activity: Activity) {
        val productId = when (_uiState.value.selectedPlan) {
            SubscriptionPlan.YEARLY -> BillingConfig.PRODUCT_ID_YEARLY
            SubscriptionPlan.MONTHLY -> BillingConfig.PRODUCT_ID_MONTHLY
            SubscriptionPlan.WEEKLY -> BillingConfig.PRODUCT_ID_WEEKLY
        }
        subscriptionManager.launchPurchase(activity, productId)
    }

    /**
     * Launches the primary CTA — yearly plan with 3-day trial.
     */
    fun launchTrialPurchase(activity: Activity) {
        subscriptionManager.launchPurchase(activity, BillingConfig.PRODUCT_ID_YEARLY)
    }

    fun clearError() {
        subscriptionManager.clearError()
    }
}
