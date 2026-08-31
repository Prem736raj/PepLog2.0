package com.appvexis.peptidetracker.core.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Wraps the Google Play BillingClient to provide a clean Kotlin coroutine API.
 * Handles connection lifecycle, product details queries, purchase flows,
 * and purchase acknowledgement.
 *
 * Uses Play Billing Library 8.x suspend extension functions.
 */
@Singleton
class BillingClientWrapper @Inject constructor(
    @ApplicationContext private val context: Context
) : PurchasesUpdatedListener {

    private var billingClient: BillingClient? = null

    private val _productDetails = MutableStateFlow<List<ProductDetails>>(emptyList())
    val productDetails: StateFlow<List<ProductDetails>> = _productDetails.asStateFlow()

    private val _purchases = MutableStateFlow<List<Purchase>>(emptyList())
    val purchases: StateFlow<List<Purchase>> = _purchases.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    /** Callback for purchase updates — forwarded to SubscriptionManager. */
    var onPurchasesUpdated: ((BillingResult, List<Purchase>?) -> Unit)? = null

    // ---- PurchasesUpdatedListener ---- //

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        Timber.d("onPurchasesUpdated: responseCode=${result.responseCode}, purchases=${purchases?.size}")
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            _purchases.value = purchases
        }
        onPurchasesUpdated?.invoke(result, purchases)
    }

    // ---- Connection ---- //

    /**
     * Establishes a connection to Google Play Billing. Suspends until connected or fails.
     */
    suspend fun connect(): Boolean = suspendCancellableCoroutine { cont ->
        if (billingClient?.isReady == true) {
            _isConnected.value = true
            cont.resume(true)
            return@suspendCancellableCoroutine
        }

        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .enablePrepaidPlans()
                    .build()
            )
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                val success = result.responseCode == BillingClient.BillingResponseCode.OK
                _isConnected.value = success
                Timber.d("BillingClient connected: $success (code=${result.responseCode})")
                if (cont.isActive) cont.resume(success)
            }

            override fun onBillingServiceDisconnected() {
                _isConnected.value = false
                Timber.w("BillingClient disconnected")
            }
        })
    }

    fun disconnect() {
        billingClient?.endConnection()
        billingClient = null
        _isConnected.value = false
    }

    // ---- Product Details ---- //

    /**
     * Queries subscription product details for all configured SKUs.
     * Uses Billing Library 8 Kotlin suspend extension.
     */
    suspend fun queryProductDetails(): List<ProductDetails> {
        val client = billingClient ?: return emptyList()
        if (!client.isReady) {
            Timber.w("queryProductDetails called but BillingClient not ready")
            return emptyList()
        }

        val productList = BillingConfig.ALL_PRODUCT_IDS.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        return try {
            val result = client.queryProductDetails(params)
            if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val details = result.productDetailsList ?: emptyList()
                _productDetails.value = details
                Timber.d("Fetched ${details.size} product details")
                details
            } else {
                Timber.e("queryProductDetails failed: code=${result.billingResult.responseCode}")
                emptyList()
            }
        } catch (e: Exception) {
            Timber.e(e, "queryProductDetails exception")
            emptyList()
        }
    }

    // ---- Purchase Flow ---- //

    /**
     * Launches a billing flow for the given product + offer.
     * Must be called from an Activity context.
     *
     * @param activity The activity to anchor the billing sheet to.
     * @param productDetails The product to purchase.
     * @param offerToken The specific offer (e.g., free trial base plan) to use.
     */
    fun launchPurchaseFlow(
        activity: Activity,
        productDetails: ProductDetails,
        offerToken: String
    ): BillingResult {
        val client = billingClient ?: run {
            Timber.e("launchPurchaseFlow: BillingClient is null")
            return BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.SERVICE_DISCONNECTED)
                .build()
        }

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .setOfferToken(offerToken)
            .build()

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        return client.launchBillingFlow(activity, flowParams)
    }

    // ---- Query Existing Purchases ---- //

    /**
     * Queries currently active subscription purchases.
     * This is the single source of truth for subscription state.
     * Uses Billing Library 8 Kotlin suspend extension.
     */
    suspend fun queryPurchases(): Result<List<Purchase>> {
        val client = billingClient
            ?: return Result.failure(IllegalStateException("Billing service is not connected"))
        if (!client.isReady) {
            return Result.failure(IllegalStateException("Billing service is not ready"))
        }

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        return try {
            val result = client.queryPurchasesAsync(params)
            if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _purchases.value = result.purchasesList
                Timber.d("Active purchases: ${result.purchasesList.size}")
                Result.success(result.purchasesList)
            } else {
                Timber.e("queryPurchases failed: code=${result.billingResult.responseCode}")
                Result.failure(
                    IllegalStateException(
                        "Billing purchase query failed (code=${result.billingResult.responseCode})"
                    )
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "queryPurchases exception")
            Result.failure(e)
        }
    }

    // ---- Acknowledge ---- //

    /**
     * Acknowledges a purchase. Required within 3 days of purchase or it gets refunded.
     */
    suspend fun acknowledgePurchase(purchaseToken: String): Boolean {
        val client = billingClient ?: return false
        if (!client.isReady) return false

        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()

        return suspendCancellableCoroutine { cont ->
            client.acknowledgePurchase(params) { result ->
                val success = result.responseCode == BillingClient.BillingResponseCode.OK
                Timber.d("acknowledgePurchase: success=$success (code=${result.responseCode})")
                cont.resume(success)
            }
        }
    }
}
