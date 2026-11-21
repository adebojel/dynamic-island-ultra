package com.adebojel.dynamicislandultra.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams

data class PremiumPlanOffer(
    val title: String,
    val price: String,
    val basePlanId: String,
    val offerToken: String,
    val productDetails: ProductDetails
)

object PremiumBillingManager {
    const val PREMIUM_PRODUCT_ID = "diu_premium"
    private const val TAG = "DIUPremiumBilling"

    var offers by mutableStateOf<List<PremiumPlanOffer>>(emptyList())
        private set
    var isPremiumActive by mutableStateOf(false)
        private set
    var status by mutableStateOf("Memeriksa paket Google Play…")
        private set
    var billingAvailable by mutableStateOf(false)
        private set

    @Volatile private var client: BillingClient? = null
    private var appContext: Context? = null
    private val lock = Any()

    private val purchasesListener = PurchasesUpdatedListener { result, purchases ->
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            processPurchases(purchases)
            refreshPurchases()
        } else if (result.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            status = "Pembelian dibatalkan. Tidak ada biaya yang dikenakan."
        } else if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            Log.w(TAG, "Purchase update: ${result.debugMessage}")
            status = "Pembelian belum selesai: ${result.debugMessage}"
        }
    }

    fun connect(context: Context) {
        val app = context.applicationContext
        appContext = app

        if (client?.isReady == true) {
            client?.let {
                queryProducts(it)
                queryOwnedSubscriptions(it)
            }
            return
        }

        synchronized(lock) {
            if (client?.isReady == true || client != null) return
            val billingClient = BillingClient.newBuilder(app)
                .setListener(purchasesListener)
                .enablePendingPurchases(
                    PendingPurchasesParams.newBuilder()
                        .enableOneTimeProducts()
                        .enablePrepaidPlans()
                        .build()
                )
                .enableAutoServiceReconnection()
                .build()
            client = billingClient

            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: com.android.billingclient.api.BillingResult) {
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        billingAvailable = true
                        status = "Google Play Billing terhubung."
                        queryProducts(billingClient)
                        queryOwnedSubscriptions(billingClient)
                    } else {
                        billingAvailable = false
                        status = "Google Play Billing belum tersedia di perangkat ini."
                        Log.w(TAG, "Billing setup: ${result.debugMessage}")
                        synchronized(lock) { if (client === billingClient) client = null }
                    }
                }

                override fun onBillingServiceDisconnected() {
                    billingAvailable = false
                    status = "Google Play terputus. Koneksi akan diperbarui saat halaman Premium dibuka kembali."
                    synchronized(lock) { if (client === billingClient) client = null }
                }
            })
        }
    }

    private fun queryProducts(billingClient: BillingClient) {
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(PREMIUM_PRODUCT_ID)
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(product))
            .build()

        billingClient.queryProductDetailsAsync(params) { result, queryResult ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                Log.w(TAG, "Product details: ${result.debugMessage}")
                offers = emptyList()
                status = "Paket Premium belum tersedia di Google Play."
                return@queryProductDetailsAsync
            }

            val details = queryResult.productDetailsList.firstOrNull {
                it.productId == PREMIUM_PRODUCT_ID
            }
            val basePlanOffers = details?.subscriptionOfferDetails.orEmpty()
                .groupBy { it.basePlanId }
                .mapNotNull { (_, candidates) ->
                    candidates.firstOrNull { it.offerId == null } ?: candidates.firstOrNull()
                }

            offers = basePlanOffers.mapNotNull { offer ->
                val phase = offer.pricingPhases.pricingPhaseList.lastOrNull()
                    ?: return@mapNotNull null
                val (label, order) = labelForBasePlan(offer.basePlanId)
                PremiumPlanOffer(
                    title = label,
                    price = phase.formattedPrice,
                    basePlanId = offer.basePlanId,
                    offerToken = offer.offerToken,
                    productDetails = details ?: return@mapNotNull null
                ) to order
            }.sortedBy { it.second }.map { it.first }

            status = if (offers.isEmpty()) {
                "Belum ada paket aktif. Buat produk diu_premium dan aktifkan base plan di Play Console."
            } else {
                "Harga dan masa paket diambil langsung dari Google Play."
            }
        }
    }

    private fun labelForBasePlan(id: String): Pair<String, Int> = when (id) {
        "weekly" -> "1 Minggu" to 1
        "four-weeks" -> "4 Minggu" to 2
        "monthly" -> "1 Bulan" to 3
        "two-months" -> "2 Bulan" to 4
        "three-months" -> "3 Bulan" to 5
        "six-months" -> "6 Bulan" to 6
        "yearly" -> "1 Tahun" to 7
        else -> id.replace('-', ' ').replaceFirstChar { it.uppercase() } to 99
    }

    private fun queryOwnedSubscriptions(billingClient: BillingClient) {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        billingClient.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                processPurchases(purchases)
                val active = purchases.any {
                    it.purchaseState == Purchase.PurchaseState.PURCHASED &&
                        it.products.contains(PREMIUM_PRODUCT_ID)
                }
                isPremiumActive = active
                appContext?.let { PremiumAccess.setPlaySubscriptionActive(it, active) }
            } else {
                Log.w(TAG, "Query purchases: ${result.debugMessage}")
                status = "Status Premium belum dapat diperbarui. Periksa koneksi Google Play."
            }
        }
    }

    private fun processPurchases(purchases: List<Purchase>) {
        val owned = purchases.filter {
            it.purchaseState == Purchase.PurchaseState.PURCHASED &&
                it.products.contains(PREMIUM_PRODUCT_ID)
        }
        isPremiumActive = owned.isNotEmpty()
        appContext?.let { PremiumAccess.setPlaySubscriptionActive(it, isPremiumActive) }

        val billingClient = client ?: return
        owned.filterNot { it.isAcknowledged }.forEach { purchase ->
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(params) { result ->
                if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                    Log.w(TAG, "Acknowledge: ${result.debugMessage}")
                }
            }
        }
        if (isPremiumActive) status = "Premium aktif melalui Google Play."
    }

    fun launchPurchase(activity: Activity, offer: PremiumPlanOffer) {
        val billingClient = client
        if (billingClient?.isReady != true) {
            status = "Google Play Billing belum terhubung."
            connect(activity)
            return
        }
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(offer.productDetails)
            .setOfferToken(offer.offerToken)
            .build()
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParams))
            .build()
        val result = billingClient.launchBillingFlow(activity, params)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            status = "Tidak dapat membuka pembayaran: ${result.debugMessage}"
        }
    }

    fun refreshPurchases() {
        client?.takeIf { it.isReady }?.let { queryOwnedSubscriptions(it) }
    }
}
