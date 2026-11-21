package com.adebojel.dynamicislandultra.ads

import android.app.Activity
import android.content.Context
import android.graphics.Color as AndroidColor
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.nativead.MediaView
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoader
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoaderCallback
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdRequest

private val AdNight = Color(0xFF080B14)
private val AdPanel = Color(0xFF121A2B)
private val AdCyan = Color(0xFF43E8FF)
private val AdGold = Color(0xFFFFC857)
private const val REWARD_PREFS = "ad_rewards"
private const val THEME_CREDITS = "theme_credits"
private const val THEME_BONUS_UNTIL = "theme_bonus_until"
private const val WINDOW_THEME_MS = 24L * 60L * 60L * 1000L

class AdCenterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = androidx.compose.material3.darkColorScheme(
                    primary = AdCyan,
                    secondary = AdGold,
                    background = AdNight,
                    surface = AdPanel,
                    onSurface = Color.White
                )
            ) {
                AdCenterScreen(
                    activity = this,
                    onClose = { finish() },
                    onThemeActivated = {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
private fun AdCenterScreen(
    activity: Activity,
    onClose: () -> Unit,
    onThemeActivated: () -> Unit
) {
    val context = LocalContext.current
    val preferences = remember(context) {
        context.getSharedPreferences(REWARD_PREFS, Context.MODE_PRIVATE)
    }
    var credits by remember { mutableIntStateOf(preferences.getInt(THEME_CREDITS, 0)) }
    var themeBonusUntil by remember { mutableStateOf(preferences.getLong(THEME_BONUS_UNTIL, 0L)) }
    var adsReady by remember { mutableStateOf(AdsSdkManager.isInitialized && AdsSdkManager.canRequestAds) }
    var status by remember { mutableStateOf("Menyiapkan privasi dan layanan iklan…") }
    var showRewardedInterstitialIntro by remember { mutableStateOf(false) }
    var showPrivacyButton by remember { mutableStateOf(AdsSdkManager.privacyOptionsRequired) }
    var showBanner by remember { mutableStateOf(preferences.getBoolean("show_banner_slot", true)) }
    var showNative by remember { mutableStateOf(preferences.getBoolean("show_native_slot", true)) }

    LaunchedEffect(activity) {
        AdsSdkManager.start(activity) { ready ->
            adsReady = ready
            showPrivacyButton = AdsSdkManager.privacyOptionsRequired
            status = if (ready) {
                "Iklan siap. Video hanya diputar setelah Anda memilihnya."
            } else {
                "Iklan belum dapat dimuat. Fitur utama aplikasi tetap bisa digunakan."
            }
        }
    }

    val now = System.currentTimeMillis()
    val themeActive = themeBonusUntil > now
    val remainingHours = if (themeActive) {
        ((themeBonusUntil - now) / (60L * 60L * 1000L)).coerceAtLeast(1L)
    } else 0L

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF101A31), AdNight)))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onClose) {
                androidx.compose.material3.Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                Spacer(Modifier.width(6.dp))
                Text("Kembali")
            }
            Spacer(Modifier.width(6.dp))
            Column {
                Text("BONUS & MONETISASI", color = AdCyan, fontSize = 11.sp, letterSpacing = 2.sp)
                Text("Dynamic Island Ultra", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AdPanel),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.material3.Icon(
                                Icons.Default.CardGiftcard,
                                contentDescription = null,
                                tint = AdGold,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("Kredit Tema Bonus", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Text("$credits kredit tersedia", color = AdGold, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text(
                            if (themeActive) "Tema bonus sedang aktif (sisa sekitar $remainingHours jam)."
                            else "Kumpulkan kredit dari iklan pilihan Anda, lalu aktifkan aksen tema bonus selama 24 jam.",
                            color = Color(0xFFB7C2D5),
                            fontSize = 12.sp
                        )
                        Button(
                            onClick = {
                                if (credits > 0) {
                                    val until = System.currentTimeMillis() + WINDOW_THEME_MS
                                    preferences.edit()
                                        .putInt(THEME_CREDITS, credits - 1)
                                        .putLong(THEME_BONUS_UNTIL, until)
                                        .apply()
                                    credits -= 1
                                    themeBonusUntil = until
                                    onThemeActivated()
                                } else {
                                    status = "Belum ada kredit tema. Pilih salah satu bonus iklan di bawah."
                                }
                            },
                            enabled = credits > 0,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Aktifkan tema bonus (1 kredit)")
                        }
                    }
                }
            }

            item { Text("IKLAN BONUS SUKARELA", color = AdCyan, fontSize = 11.sp, letterSpacing = 2.sp) }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = AdPanel), shape = RoundedCornerShape(18.dp)) {
                    Column(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.material3.Icon(Icons.Default.PlayCircle, null, tint = AdCyan)
                            Spacer(Modifier.width(8.dp))
                            Text("Rewarded Ads", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "Anda memilih sendiri untuk menonton video. Satu kredit tema diberikan hanya setelah SDK mengonfirmasi reward.",
                            color = Color(0xFFB7C2D5),
                            fontSize = 12.sp
                        )
                        Button(
                            onClick = {
                                AdsSdkManager.showRewarded(
                                    activity,
                                    onRewardEarned = {
                                        credits = grantThemeCredit(preferences)
                                        status = "Berhasil! 1 kredit tema ditambahkan."
                                    },
                                    onStatus = { status = it }
                                )
                            },
                            enabled = adsReady,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Tonton iklan untuk 1 kredit tema")
                        }
                    }
                }
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = AdPanel), shape = RoundedCornerShape(18.dp)) {
                    Column(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.material3.Icon(Icons.Default.Lock, null, tint = AdGold)
                            Spacer(Modifier.width(8.dp))
                            Text("Rewarded Interstitial", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "Iklan layar penuh hanya dimulai setelah Anda memilih Lanjutkan pada pemberitahuan ini. Anda boleh memilih Lewati. Bonus diberikan hanya setelah reward dikonfirmasi.",
                            color = Color(0xFFB7C2D5),
                            fontSize = 12.sp
                        )
                        OutlinedButton(
                            onClick = { showRewardedInterstitialIntro = true },
                            enabled = adsReady,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Lihat pengantar bonus interstitial")
                        }
                    }
                }
            }

            item {
                Text("REKOMENDASI SPONSOR", color = AdCyan, fontSize = 11.sp, letterSpacing = 2.sp)
                Text(
                    "Native Advanced ditampilkan sebagai kartu terpisah dan diberi label Iklan.",
                    color = Color(0xFFB7C2D5),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )
                if (showNative && adsReady) NativeAdSlot(enabled = true)
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = AdPanel), shape = RoundedCornerShape(18.dp)) {
                    Column(
                        Modifier.fillMaxWidth().padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("KONTROL IKLAN", color = Color.White, fontWeight = FontWeight.Bold)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("Banner bawah di halaman ini", color = Color.White, fontSize = 13.sp)
                                Text("Slot banner tidak menutupi kontrol aplikasi.", color = Color(0xFF9BA8C1), fontSize = 11.sp)
                            }
                            Switch(
                                checked = showBanner,
                                onCheckedChange = {
                                    showBanner = it
                                    preferences.edit().putBoolean("show_banner_slot", it).apply()
                                }
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("Kartu Native Advanced", color = Color.White, fontSize = 13.sp)
                                Text("Maksimal satu kartu pada halaman bonus.", color = Color(0xFF9BA8C1), fontSize = 11.sp)
                            }
                            Switch(
                                checked = showNative,
                                onCheckedChange = {
                                    showNative = it
                                    preferences.edit().putBoolean("show_native_slot", it).apply()
                                }
                            )
                        }
                        if (showPrivacyButton) {
                            TextButton(
                                onClick = {
                                    AdsSdkManager.showPrivacyOptions(activity) { ready ->
                                        adsReady = ready
                                        showPrivacyButton = AdsSdkManager.privacyOptionsRequired
                                        status = "Pengaturan privasi telah diperbarui."
                                    }
                                }
                            ) { Text("Kelola pilihan privasi iklan") }
                        }
                    }
                }
            }

            item {
                Text(status, color = Color(0xFFB7C2D5), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                Text(
                    "Banner dan Native hanya ditempatkan di halaman bonus. Tidak ada iklan otomatis pada floating island, asisten, mikrofon, notifikasi, browser, atau kontrol sistem.",
                    color = Color(0xFF7F8DA7),
                    fontSize = 11.sp
                )
            }
        }

        if (showBanner && adsReady) {
            AdBannerSlot(
                modifier = Modifier.fillMaxWidth().background(Color(0xFF0C1322)).padding(vertical = 4.dp),
                enabled = true
            )
        }
    }

    if (showRewardedInterstitialIntro) {
        AlertDialog(
            onDismissRequest = { showRewardedInterstitialIntro = false },
            title = { Text("Bonus Tema melalui iklan") },
            text = {
                Text(
                    "Iklan layar penuh akan ditampilkan hanya jika Anda memilih Lanjutkan. Anda boleh memilih Lewati. 1 kredit tema hanya diberikan setelah iklan mengirim konfirmasi reward."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showRewardedInterstitialIntro = false
                    AdsSdkManager.showRewardedInterstitial(
                        activity,
                        onRewardEarned = {
                            credits = grantThemeCredit(preferences)
                            status = "Berhasil! 1 kredit tema ditambahkan."
                        },
                        onStatus = { status = it }
                    )
                }) { Text("Lanjutkan") }
            },
            dismissButton = {
                TextButton(onClick = { showRewardedInterstitialIntro = false }) { Text("Lewati") }
            }
        )
    }
}

private fun grantThemeCredit(preferences: android.content.SharedPreferences): Int {
    val updated = preferences.getInt(THEME_CREDITS, 0) + 1
    preferences.edit().putInt(THEME_CREDITS, updated).apply()
    return updated
}

@Composable
private fun AdBannerSlot(modifier: Modifier = Modifier, enabled: Boolean) {
    val context = LocalContext.current
    val widthDp = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp
    val isPreview = LocalInspectionMode.current
    if (!enabled || isPreview) return

    val adView = remember(context) { AdView(context) }
    AndroidView(factory = { adView }, modifier = modifier.fillMaxWidth())
    DisposableEffect(adView, widthDp) {
        val adSize = AdSize.getLargeAnchoredAdaptiveBannerAdSize(context, widthDp)
        val request = BannerAdRequest.Builder(AdsSdkManager.bannerUnitId, adSize).build()
        adView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        adView.loadAd(request, object : AdLoadCallback<BannerAd> {
            override fun onAdLoaded(ad: BannerAd) = Unit
            override fun onAdFailedToLoad(adError: LoadAdError) {
                android.util.Log.w("DynamicIslandAds", "Banner failed: ${adError.message}")
            }
        })
        onDispose { adView.destroy() }
    }
}

@Composable
private fun NativeAdSlot(enabled: Boolean) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    var loadFailed by remember { mutableStateOf(false) }
    var loaded by remember { mutableStateOf(false) }
    if (!enabled || isPreview) return

    val container = remember(context) { FrameLayout(context) }
    val currentNativeAd = remember { mutableStateOf<NativeAd?>(null) }

    AndroidView(
        factory = { container },
        modifier = Modifier.fillMaxWidth().height(if (loaded) 250.dp else 1.dp)
    )

    DisposableEffect(container, enabled) {
        var disposed = false
        val request = NativeAdRequest.Builder(
            AdsSdkManager.nativeUnitId,
            listOf(NativeAd.NativeAdType.NATIVE)
        ).build()

        NativeAdLoader.load(request, object : NativeAdLoaderCallback {
            override fun onNativeAdLoaded(nativeAd: NativeAd) {
                container.post {
                    if (disposed) {
                        nativeAd.destroy()
                    } else {
                        currentNativeAd.value?.destroy()
                        currentNativeAd.value = nativeAd
                        container.removeAllViews()
                        container.addView(createNativeAdView(context, nativeAd))
                        loaded = true
                        loadFailed = false
                    }
                }
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                android.util.Log.w("DynamicIslandAds", "Native failed: ${adError.message}")
                container.post { if (!disposed) loadFailed = true }
            }
        })

        onDispose {
            disposed = true
            container.removeAllViews()
            currentNativeAd.value?.destroy()
            currentNativeAd.value = null
        }
    }

    if (loadFailed && !loaded) {
        Text("Iklan sponsor belum tersedia saat ini.", color = Color(0xFF9BA8C1), fontSize = 11.sp)
    }
}

private fun createNativeAdView(context: Context, ad: NativeAd): com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView {
    val density = context.resources.displayMetrics.density
    fun dp(value: Int) = (value * density).toInt()

    val nativeView = com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView(context).apply {
        background = GradientDrawable().apply {
            setColor(AndroidColor.rgb(18, 26, 43))
            cornerRadius = dp(18).toFloat()
            setStroke(dp(1), AndroidColor.rgb(54, 76, 104))
        }
        setPadding(dp(14), dp(12), dp(14), dp(12))
    }

    val content = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(2), dp(2), dp(2), dp(2))
    }
    val attribution = TextView(context).apply {
        text = "IKLAN"
        textSize = 10f
        setTextColor(AndroidColor.rgb(67, 232, 255))
        setTypeface(typeface, Typeface.BOLD)
    }
    val advertiser = TextView(context).apply {
        textSize = 11f
        setTextColor(AndroidColor.LTGRAY)
    }
    val headline = TextView(context).apply {
        textSize = 15f
        setTextColor(AndroidColor.WHITE)
        setTypeface(typeface, Typeface.BOLD)
        setPadding(0, dp(4), 0, dp(4))
    }
    val media = MediaView(context).apply {
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(132)
        ).apply { topMargin = dp(6); bottomMargin = dp(6) }
    }
    val body = TextView(context).apply {
        textSize = 12f
        setTextColor(AndroidColor.LTGRAY)
        maxLines = 3
    }
    val callToAction = TextView(context).apply {
        textSize = 12f
        setTextColor(AndroidColor.BLACK)
        setTypeface(typeface, Typeface.BOLD)
        setPadding(dp(12), dp(8), dp(12), dp(8))
        background = GradientDrawable().apply {
            setColor(AndroidColor.rgb(67, 232, 255))
            cornerRadius = dp(10).toFloat()
        }
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(10) }
    }

    advertiser.text = ad.advertiser.orEmpty()
    headline.text = ad.headline.orEmpty()
    body.text = ad.body.orEmpty()
    callToAction.text = ad.callToAction.orEmpty()
    advertiser.visibility = if (ad.advertiser.isNullOrBlank()) View.GONE else View.VISIBLE
    body.visibility = if (ad.body.isNullOrBlank()) View.GONE else View.VISIBLE
    callToAction.visibility = if (ad.callToAction.isNullOrBlank()) View.GONE else View.VISIBLE

    content.addView(attribution)
    content.addView(advertiser)
    content.addView(headline)
    content.addView(media)
    content.addView(body)
    content.addView(callToAction)

    nativeView.addView(
        content,
        FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    )
    nativeView.advertiserView = advertiser
    nativeView.headlineView = headline
    nativeView.bodyView = body
    nativeView.callToActionView = callToAction
    nativeView.registerNativeAd(ad, media)
    return nativeView
}
