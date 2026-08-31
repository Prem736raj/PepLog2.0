package com.appvexis.peptidetracker.core.billing

import android.app.Activity
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Extension property for billing-specific DataStore.
 * Separate from user preferences to keep billing state isolated.
 */
private val Context.billingDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "peplog_billing_prefs"
)

/**
 * Central subscription state manager.
 *
 * Responsibilities:
 * - Exposes [isPremium] as the single source of truth for subscription status
 * - Manages purchase flow initiation
 * - Handles purchase acknowledgement
 * - Caches subscription state in DataStore for offline access
 * - Refreshes state from BillingClient.queryPurchasesAsync() on each app launch
 */
@Singleton
class SubscriptionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val billingClientWrapper: BillingClientWrapper
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // ---- DataStore keys ---- //
    private object Keys {
        val IS_PREMIUM = booleanPreferencesKey(BillingConfig.PREF_KEY_IS_PREMIUM)
        val PRODUCT_ID = stringPreferencesKey(BillingConfig.PREF_KEY_SUBSCRIPTION_PRODUCT)
    }

    // ---- Primary State ---- //

    /**
     * The single source of truth for premium status.
     * Reads from DataStore cache initially, then refreshes from Play on connect.
     */
    val isPremium: StateFlow<Boolean> = context.billingDataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences())
            else throw e
        }
        .map { prefs -> prefs[Keys.IS_PREMIUM] ?: false }
        .stateIn(scope, SharingStarted.Eagerly, false)

    private val _subscriptionProduct = MutableStateFlow<String?>(null)
    val subscriptionProduct: StateFlow<String?> = _subscriptionProduct.asStateFlow()

    val products: StateFlow<List<BillingProductDisplay>> = billingClientWrapper.productDetails
        .map { details ->
            details.map { product ->
                val offer = product.subscriptionOfferDetails?.firstOrNull()
                val phases = offer?.pricingPhases?.pricingPhaseList.orEmpty()
                val paidPhase = phases.lastOrNull { it.priceAmountMicros > 0L }
                    ?: phases.lastOrNull()
                val trialPhase = phases.firstOrNull { it.priceAmountMicros == 0L }
                BillingProductDisplay(
                    productId = product.productId,
                    priceText = paidPhase?.let {
                        "${it.formattedPrice}${billingPeriodSuffix(it.billingPeriod)}"
                    } ?: "Unavailable",
                    hasFreeTrial = trialPhase != null
                )
            }
        }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _purchaseError = MutableStateFlow<String?>(null)
    val purchaseError: StateFlow<String?> = _purchaseError.asStateFlow()

    init {
        // Wire up purchase update callback from BillingClientWrapper
        billingClientWrapper.onPurchasesUpdated = { result, purchases ->
            scope.launch {
                handlePurchaseUpdate(result, purchases)
            }
        }
    }

    // ---- Initialization ---- //

    /**
     * Call on app startup to connect to billing and refresh subscription state.
     */
    fun initialize() {
        scope.launch {
            try {
                _isLoading.value = true
                val connected = billingClientWrapper.connect()
                if (connected) {
                    billingClientWrapper.queryProductDetails()
                    refreshSubscriptionState()
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to initialize billing")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ---- Purchase Flow ---- //

    /**
     * Launches the purchase flow for a specific product.
     *
     * @param activity The activity context for the billing sheet.
     * @param productId The product ID to purchase (defaults to yearly with trial).
     */
    fun launchPurchase(activity: Activity, productId: String = BillingConfig.PRODUCT_ID_YEARLY) {
        scope.launch {
            _isLoading.value = true
            _purchaseError.value = null

            try {
                // Ensure connected
                if (!billingClientWrapper.isConnected.value) {
                    billingClientWrapper.connect()
                }

                // Find the product details
                val details = billingClientWrapper.productDetails.value
                    .firstOrNull { it.productId == productId }

                if (details == null) {
                    // Try fetching again
                    val freshDetails = billingClientWrapper.queryProductDetails()
                    val found = freshDetails.firstOrNull { it.productId == productId }
                    if (found == null) {
                        _purchaseError.value = "Product not available. Please try again."
                        _isLoading.value = false
                        return@launch
                    }
                    launchBillingFlow(activity, found)
                } else {
                    launchBillingFlow(activity, details)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to launch purchase")
                _purchaseError.value = "Purchase failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun launchBillingFlow(activity: Activity, productDetails: ProductDetails) {
        // Get the first available offer token (the one with the free trial is
        // configured in Play Console — billing library picks it up automatically)
        val offerToken = productDetails.subscriptionOfferDetails
            ?.firstOrNull()
            ?.offerToken

        if (offerToken == null) {
            _purchaseError.value = "No offer available for this product."
            return
        }

        val result = billingClientWrapper.launchPurchaseFlow(activity, productDetails, offerToken)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            _purchaseError.value = "Billing flow error (code: ${result.responseCode})"
        }
    }

    // ---- Purchase Handling ---- //

    private suspend fun handlePurchaseUpdate(result: BillingResult, purchases: List<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    val productId = purchase.products.firstOrNull { it in BillingConfig.ALL_PRODUCT_IDS }
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && productId != null) {
                        // Acknowledge if needed
                        val acknowledged = purchase.isAcknowledged ||
                            billingClientWrapper.acknowledgePurchase(purchase.purchaseToken)
                        if (!acknowledged) {
                            Timber.e("Failed to acknowledge purchase")
                            _purchaseError.value = "Purchase is awaiting Play verification. Please try again shortly."
                            return@forEach
                        }
                        // Update subscription state
                        updateCachedState(isPremium = true, productId = productId)
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Timber.d("User cancelled purchase")
                _purchaseError.value = null // Not an error
            }
            else -> {
                Timber.e("Purchase update error: code=${result.responseCode}, msg=${result.debugMessage}")
                _purchaseError.value = "Purchase failed. Please try again."
            }
        }
    }

    // ---- State Refresh ---- //

    /**
     * Refreshes subscription state from Play's `queryPurchasesAsync()`.
     * This is the authoritative check — DataStore cache is only for offline fallback.
     */
    suspend fun refreshSubscriptionState(): Boolean {
        try {
            val activePurchases = billingClientWrapper.queryPurchases().getOrElse { error ->
                Timber.e(error, "Purchase state could not be verified")
                return false
            }
            var activeProduct: String? = null
            activePurchases
                .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                .forEach { purchase ->
                    val productId = purchase.products.firstOrNull { it in BillingConfig.ALL_PRODUCT_IDS }
                    if (productId != null) {
                        val acknowledged = purchase.isAcknowledged ||
                            billingClientWrapper.acknowledgePurchase(purchase.purchaseToken)
                        if (acknowledged && activeProduct == null) {
                            activeProduct = productId
                        }
                    }
                }

            updateCachedState(isPremium = activeProduct != null, productId = activeProduct)
            Timber.d("Subscription state refreshed: isPremium=${activeProduct != null}")
            return true
        } catch (e: Exception) {
            Timber.e(e, "Failed to refresh subscription state")
            // Fall back to cached state — don't clear it
            return false
        }
    }

    // ---- Cache ---- //

    private suspend fun updateCachedState(isPremium: Boolean, productId: String?) {
        context.billingDataStore.edit { prefs ->
            prefs[Keys.IS_PREMIUM] = isPremium
            if (productId != null) {
                prefs[Keys.PRODUCT_ID] = productId
            }
        }
        _subscriptionProduct.value = productId
    }

    /**
     * Clears the purchase error message.
     */
    fun clearError() {
        _purchaseError.value = null
    }

    /**
     * Returns whether a specific feature is available on the free tier.
     * Use this for feature gating.
     */
    fun isFeatureAvailable(feature: PremiumFeature): Boolean {
        return when (feature) {
            PremiumFeature.UNLIMITED_PROTOCOLS -> isPremium.value
            PremiumFeature.ADVANCED_ANALYTICS -> isPremium.value
            PremiumFeature.PK_VISUALIZER -> isPremium.value
            PremiumFeature.HEALTH_CONNECT -> isPremium.value
            PremiumFeature.CLOUD_BACKUP -> isPremium.value
            // Local export is always available so users can retrieve their data
            // without paying; cloud backup remains Premium-only.
            PremiumFeature.EXPORT_DATA -> true
            // Free tier features
            PremiumFeature.BASIC_LOGGING -> true
            PremiumFeature.SINGLE_PROTOCOL -> true
            PremiumFeature.CALCULATOR -> true
            PremiumFeature.ENCYCLOPEDIA -> true
        }
    }
}

data class BillingProductDisplay(
    val productId: String,
    val priceText: String,
    val hasFreeTrial: Boolean
)

private fun billingPeriodSuffix(period: String): String = when (period) {
    "P1D" -> "/day"
    "P1W" -> "/week"
    "P1M" -> "/month"
    "P1Y" -> "/year"
    else -> ""
}

/**
 * Enum of features that can be gated behind premium.
 */
enum class PremiumFeature {
    // Premium-only
    UNLIMITED_PROTOCOLS,
    ADVANCED_ANALYTICS,
    PK_VISUALIZER,
    HEALTH_CONNECT,
    CLOUD_BACKUP,
    EXPORT_DATA,

    // Free tier (always available)
    BASIC_LOGGING,
    SINGLE_PROTOCOL,
    CALCULATOR,
    ENCYCLOPEDIA
}
