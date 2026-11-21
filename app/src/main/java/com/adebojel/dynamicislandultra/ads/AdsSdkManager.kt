package com.adebojel.dynamicislandultra.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAd
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.libraries.ads.mobile.sdk.rewardedinterstitial.RewardedInterstitialAdEventCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Google Mobile Ads Next-Gen + UMP orchestration.
 * Debug uses Google's official test ad units; release uses the publisher's production IDs.
 * Video ads are always user-initiated and subject to local frequency caps.
 */
object AdsSdkManager {
    const val APP_ID = "ca-app-pub-9305273212801233~4511201352"

    private const val PROD_BANNER = "ca-app-pub-9305273212801233/4319629668"
    private const val PROD_REWARDED_INTERSTITIAL = "ca-app-pub-9305273212801233/5189858206"
    private const val PROD_REWARDED = "ca-app-pub-9305273212801233/7624449854"
    private const val PROD_NATIVE = "ca-app-pub-9305273212801233/4886605875"

    private const val TEST_BANNER = "ca-app-pub-3940256099942544/9214589741"
    private const val TEST_REWARDED_INTERSTITIAL = "ca-app-pub-3940256099942544/5354046379"
    private const val TEST_REWARDED = "ca-app-pub-3940256099942544/5224354917"
    private const val TEST_NATIVE = "ca-app-pub-3940256099942544/2247696110"

    val bannerUnitId: String get() = if (BuildConfig.DEBUG) TEST_BANNER else PROD_BANNER
    val rewardedInterstitialUnitId: String
        get() = if (BuildConfig.DEBUG) TEST_REWARDED_INTERSTITIAL else PROD_REWARDED_INTERSTITIAL
    val rewardedUnitId: String get() = if (BuildConfig.DEBUG) TEST_REWARDED else PROD_REWARDED
    val nativeUnitId: String get() = if (BuildConfig.DEBUG) TEST_NATIVE else PROD_NATIVE

    private const val TAG = "DynamicIslandAds"
    private const val PREFS = "ad_frequency"
    private const val IMPRESSIONS_KEY = "video_impressions"
    private const val WINDOW_MS = 24L * 60L * 60L * 1000L
    private const val GLOBAL_VIDEO_LIMIT = 2
    private const val REWARDED_INTERSTITIAL_LIMIT = 1
    private const val REWARDED_LIMIT = 2

    private val mainHandler = Handler(Looper.getMainLooper())
    private val videoRequestInFlight = AtomicBoolean(false)
    private val callbacks = mutableListOf<(Boolean) -> Unit>()

    @Volatile var isInitialized: Boolean = false
        private set
    @Volatile var canRequestAds: Boolean = false
        private set
    @Volatile var privacyOptionsRequired: Boolean = false
        private set

    private var consentFlowStarted = false
    private var consentFlowFinished = false
    private var sdkInitializationStarted = false

    /** Refresh UMP status for every new app process before requesting any ad. */
    fun start(activity: Activity, onComplete: (Boolean) -> Unit = {}) {
        synchronized(this) {
            if (isInitialized) {
                mainHandler.post { onComplete(true) }
                return
            }
            if (consentFlowStarted) {
                if (consentFlowFinished && !canRequestAds) {
                    mainHandler.post { onComplete(false) }
                } else {
                    callbacks.add(onComplete)
                }
                return
            }
            callbacks.add(onComplete)
            consentFlowStarted = true
        }

        val information = UserMessagingPlatform.getConsentInformation(activity)
        information.requestConsentInfoUpdate(
            activity,
            ConsentRequestParameters.Builder().build(),
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) Log.w(TAG, "UMP form: ${formError.message}")
                    finishConsent(activity.applicationContext, information)
                }
            },
            { requestError ->
                Log.w(TAG, "UMP update: ${requestError.message}")
                // UMP retains its own consent state; request only if canRequestAds() confirms it.
                finishConsent(activity.applicationContext, information)
            }
        )
    }

    private fun finishConsent(appContext: Context, information: ConsentInformation) {
        canRequestAds = information.canRequestAds()
        privacyOptionsRequired =
            information.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
        synchronized(this) { consentFlowFinished = true }
        if (canRequestAds) initializeSdk(appContext) else notifyReady(false)
    }

    private fun initializeSdk(appContext: Context) {
        synchronized(this) {
            if (isInitialized) {
                notifyReady(true)
                return
            }
            if (sdkInitializationStarted) return
            sdkInitializationStarted = true
        }

        Thread({
            try {
                MobileAds.initialize(
                    appContext,
                    InitializationConfig.Builder(APP_ID).build()
                ) {
                    isInitialized = true
                    notifyReady(true)
                }
            } catch (error: Exception) {
                Log.e(TAG, "GMA Next-Gen initialization failed", error)
                synchronized(this) { sdkInitializationStarted = false }
                notifyReady(false)
            }
        }, "gma-nextgen-init").start()
    }

    private fun notifyReady(ready: Boolean) {
        val pending = synchronized(this) {
            callbacks.toList().also { callbacks.clear() }
        }
        mainHandler.post { pending.forEach { it(ready) } }
    }

    fun showPrivacyOptions(activity: Activity, onComplete: (Boolean) -> Unit = {}) {
        val information = UserMessagingPlatform.getConsentInformation(activity)
        if (
            information.privacyOptionsRequirementStatus !=
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
        ) {
            onComplete(isInitialized && information.canRequestAds())
            return
        }

        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            if (formError != null) Log.w(TAG, "UMP privacy options: ${formError.message}")
            canRequestAds = information.canRequestAds()
            privacyOptionsRequired =
                information.privacyOptionsRequirementStatus ==
                    ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
            if (canRequestAds && !isInitialized) {
                initializeSdk(activity.applicationContext)
                mainHandler.postDelayed({ onComplete(isInitialized && canRequestAds) }, 750L)
            } else {
                onComplete(isInitialized && canRequestAds)
            }
        }
    }

    fun showRewarded(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onStatus: (String) -> Unit
    ) {
        if (!isInitialized || !canRequestAds) {
            onStatus("Iklan belum siap atau belum diizinkan oleh pengaturan privasi.")
            return
        }
        if (!canShowVideo(activity, "rewarded", REWARDED_LIMIT)) {
            onStatus("Batas iklan video 24 jam sudah tercapai. Coba lagi nanti.")
            return
        }
        if (!videoRequestInFlight.compareAndSet(false, true)) {
            onStatus("Permintaan iklan sebelumnya masih berjalan.")
            return
        }

        RewardedAd.load(
            AdRequest.Builder(rewardedUnitId).build(),
            object : AdLoadCallback<RewardedAd> {
                override fun onAdLoaded(ad: RewardedAd) {
                    ad.adEventCallback = object : RewardedAdEventCallback {
                        override fun onAdImpression() {
                            recordVideoImpression(activity, "rewarded")
                        }

                        override fun onAdDismissedFullScreenContent() {
                            videoRequestInFlight.set(false)
                            onStatus("Sesi iklan selesai.")
                        }

                        override fun onAdFailedToShowFullScreenContent(
                            fullScreenContentError: FullScreenContentError
                        ) {
                            videoRequestInFlight.set(false)
                            Log.w(TAG, "Rewarded show: ${fullScreenContentError.message}")
                            onStatus("Iklan tidak dapat ditampilkan. Silakan coba lagi nanti.")
                        }
                    }
                    try {
                        ad.show(activity) { onRewardEarned() }
                    } catch (error: Exception) {
                        videoRequestInFlight.set(false)
                        Log.e(TAG, "Rewarded show failed", error)
                        onStatus("Iklan gagal dibuka.")
                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    videoRequestInFlight.set(false)
                    Log.w(TAG, "Rewarded load: ${adError.message}")
                    onStatus("Belum ada iklan yang tersedia. Fitur utama tetap bisa digunakan.")
                }
            }
        )
    }

    /** Caller must first show an intro with the reward details and Continue/Skip options. */
    fun showRewardedInterstitial(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onStatus: (String) -> Unit
    ) {
        if (!isInitialized || !canRequestAds) {
            onStatus("Iklan belum siap atau belum diizinkan oleh pengaturan privasi.")
            return
        }
        if (!canShowVideo(activity, "rewarded_interstitial", REWARDED_INTERSTITIAL_LIMIT)) {
            onStatus("Bonus interstitial sudah mencapai batas 24 jam.")
            return
        }
        if (!videoRequestInFlight.compareAndSet(false, true)) {
            onStatus("Permintaan iklan sebelumnya masih berjalan.")
            return
        }

        RewardedInterstitialAd.load(
            AdRequest.Builder(rewardedInterstitialUnitId).build(),
            object : AdLoadCallback<RewardedInterstitialAd> {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    ad.adEventCallback = object : RewardedInterstitialAdEventCallback {
                        override fun onAdImpression() {
                            recordVideoImpression(activity, "rewarded_interstitial")
                        }

                        override fun onAdDismissedFullScreenContent() {
                            videoRequestInFlight.set(false)
                            onStatus("Sesi iklan selesai.")
                        }

                        override fun onAdFailedToShowFullScreenContent(
                            fullScreenContentError: FullScreenContentError
                        ) {
                            videoRequestInFlight.set(false)
                            Log.w(TAG, "Rewarded interstitial show: ${fullScreenContentError.message}")
                            onStatus("Iklan tidak dapat ditampilkan. Silakan coba lagi nanti.")
                        }
                    }
                    try {
                        ad.show(activity) { onRewardEarned() }
                    } catch (error: Exception) {
                        videoRequestInFlight.set(false)
                        Log.e(TAG, "Rewarded interstitial show failed", error)
                        onStatus("Iklan gagal dibuka.")
                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    videoRequestInFlight.set(false)
                    Log.w(TAG, "Rewarded interstitial load: ${adError.message}")
                    onStatus("Belum ada iklan yang tersedia. Silakan coba lagi nanti.")
                }
            }
        )
    }

    private fun canShowVideo(context: Context, format: String, formatLimit: Int): Boolean {
        val recent = recentVideoImpressions(context)
        return recent.size < GLOBAL_VIDEO_LIMIT &&
            recent.count { it.first == format } < formatLimit
    }

    private fun recordVideoImpression(context: Context, format: String) {
        val now = System.currentTimeMillis()
        val recent = recentVideoImpressions(context).toMutableList()
        recent.add(format to now)
        val encoded = recent.joinToString("|") { "${it.first}:${it.second}" }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(IMPRESSIONS_KEY, encoded).apply()
    }

    private fun recentVideoImpressions(context: Context): List<Pair<String, Long>> {
        val now = System.currentTimeMillis()
        val stored = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(IMPRESSIONS_KEY, "").orEmpty()
        return stored.split("|").mapNotNull { entry ->
            val parts = entry.split(":")
            if (parts.size != 2) return@mapNotNull null
            val timestamp = parts[1].toLongOrNull() ?: return@mapNotNull null
            if (timestamp <= now && now - timestamp < WINDOW_MS) parts[0] to timestamp else null
        }
    }
}
