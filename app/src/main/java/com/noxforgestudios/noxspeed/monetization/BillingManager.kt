package com.noxforgestudios.noxspeed.monetization

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
import com.noxforgestudios.noxspeed.config.AppConfig
import com.noxforgestudios.noxspeed.data.prefs.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Google Play Billing 9.1 client for the lifetime entitlement.
 * Strong anti-fraud verification requires a backend; this no-account app relies on Play ownership
 * queries + acknowledgement and keeps only an offline cache for UX continuity.
 */
class BillingManager(
    context: Context,
    private val settingsRepository: SettingsRepository,
) : PurchasesUpdatedListener {
    data class State(
        val connected: Boolean = false,
        val premium: Boolean = false,
        val productDetails: ProductDetails? = null,
        val price: String? = null,
        val pending: Boolean = false,
        val message: String? = null,
    )

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private val billingClient = BillingClient.newBuilder(context.applicationContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .enableAutoServiceReconnection()
        .build()

    fun connect() {
        if (billingClient.isReady) {
            refreshPurchases()
            queryProduct()
            return
        }
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                val ok = result.responseCode == BillingClient.BillingResponseCode.OK
                _state.value = _state.value.copy(connected = ok, message = if (ok) null else result.debugMessage)
                if (ok) {
                    queryProduct()
                    refreshPurchases()
                }
            }
            override fun onBillingServiceDisconnected() {
                _state.value = _state.value.copy(connected = false)
            }
        })
    }

    private fun queryProduct() {
        if (!billingClient.isReady) return
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(AppConfig.premiumProductId)
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        val params = QueryProductDetailsParams.newBuilder().setProductList(listOf(product)).build()
        billingClient.queryProductDetailsAsync(params) { result, queryResult ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val details = queryResult.productDetailsList.firstOrNull()
                val offer = details?.oneTimePurchaseOfferDetailsList?.firstOrNull()
                    ?: details?.oneTimePurchaseOfferDetails
                _state.value = _state.value.copy(
                    productDetails = details,
                    price = offer?.formattedPrice,
                    message = if (details == null) "Premium product is not configured in this Play track yet." else null,
                )
            } else {
                _state.value = _state.value.copy(message = result.debugMessage)
            }
        }
    }

    fun launchPurchase(activity: Activity): BillingResult? {
        val details = _state.value.productDetails ?: return null
        val builder = BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(details)
        val offerToken = details.oneTimePurchaseOfferDetailsList?.firstOrNull()?.offerToken
            ?: details.oneTimePurchaseOfferDetails?.offerToken
        if (!offerToken.isNullOrBlank()) builder.setOfferToken(offerToken)
        val flow = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(builder.build()))
            .build()
        return billingClient.launchBillingFlow(activity, flow)
    }

    fun refreshPurchases() {
        if (!billingClient.isReady) {
            connect()
            return
        }
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        billingClient.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) processPurchases(purchases)
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> processPurchases(purchases.orEmpty())
            BillingClient.BillingResponseCode.USER_CANCELED -> Unit
            else -> _state.value = _state.value.copy(message = result.debugMessage)
        }
    }

    private fun processPurchases(purchases: List<Purchase>) {
        val relevant = purchases.filter { it.products.contains(AppConfig.premiumProductId) }
        val purchased = relevant.firstOrNull { it.purchaseState == Purchase.PurchaseState.PURCHASED }
        val pending = relevant.any { it.purchaseState == Purchase.PurchaseState.PENDING }
        val premium = purchased != null
        _state.value = _state.value.copy(premium = premium, pending = pending)
        scope.launch { settingsRepository.setPremiumCached(premium) }

        if (purchased != null && !purchased.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchased.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(params) { acknowledgeResult ->
                if (acknowledgeResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    _state.value = _state.value.copy(message = acknowledgeResult.debugMessage)
                }
            }
        }
    }
}
