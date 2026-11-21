package com.adebojel.dynamicislandultra.ads

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
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
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView

private val AdNight = Color(0xFF080B14)
private val AdPanel = Color(0xFF121A2B)
private val AdCyan = Color(0xFF43E8FF)
private val AdGold = Color(0xFFFFC857)
private val AdEmerald = Color(0xFF41DC91)

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
                    onClose = {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
private fun AdCenterScreen(activity: Activity, onClose: () -> Unit) {
    val context = LocalContext.current
    val settings = remember(context) {
        context.getSharedPreferences("ad_center_settings", Context.MODE_PRIVATE)
    }
    var adsReady by remember { mutableStateOf(AdsSdkManager.isInitialized && AdsSdkManager.canRequestAds) }
    var status by remember { mutableStateOf("Memeriksa persetujuan privasi dan layanan iklan…") }
    var showInterstitialDisclosure by remember { mutableStateOf(false) }
    var showPrivacyOptions by remember { mutableStateOf(AdsSdkManager.privacyOptionsRequired) }
    var showBanner by remember { mutableStateOf(settings.getBoolean("show_banner", true)) }
    var showNative by remember { mutableStateOf(settings.getBoolean("show_native", true)) }
    var accessRefresh by remember { mutableStateOf(0) }

    LaunchedEffect(activity) {
        AdsSdkManager.start(activity) { ready ->
            adsReady = ready
            showPrivacyOptions = AdsSdkManager.privacyOptionsRequired
            status = if (ready) "Iklan siap. Video hanya diputar setelah Anda memilihnya."
            else "Iklan belum tersedia atau belum diizinkan oleh pengaturan privasi."
        }
        PremiumBillingManager.connect(activity)
    }

    val premiumUnlocked = accessRefresh >= 0 &&
        (PremiumBillingManager.isPremiumActive || PremiumAccess.hasPremiumAccess(context))
    val rewardExpiry = PremiumAccess.rewardedExpiry(context)
    val rewardHours = if (rewardExpiry > System.currentTimeMillis()) {
        ((rewardExpiry - System.currentTimeMillis()) / 3_600_000L).coerceAtLeast(1L)
    } else 0L

    Column(
        modifier = Modifier.fillMaxSize()
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
                Text("BONUS & PREMIUM", color = AdCyan, fontSize = 11.sp, letterSpacing = 2.sp)
                Text("Dynamic Island Ultra", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = AdPanel), shape = RoundedCornerShape(20.dp)) {
                    Column(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.material3.Icon(
                                if (premiumUnlocked) Icons.Default.CheckCircle else Icons.Default.Star,
                                contentDescription = null,
                                tint = if (premiumUnlocked) AdEmerald else AdGold,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Akses Premium", color = Color.White, fontWeight = FontWeight.Bold)
                                Text(
                                    when {
                                        PremiumBillingManager.isPremiumActive -> "Aktif melalui Google Play"
                                        rewardHours > 0 -> "Akses iklan bonus aktif sekitar $rewardHours jam"
                                        else -> "Satu paket untuk seluruh fitur tambahan"
                                    },
                                    color = if (premiumUnlocked) AdEmerald else Color(0xFFB7C2D5),
                                    fontSize = 11.sp
                                )
                            }
                            if (premiumUnlocked) Text("AKTIF", color = AdEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "Fitur inti tetap gratis. Premium membuka opsi personalisasi tambahan tanpa iklan.",
                            color = Color(0xFFB7C2D5),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            item {
                Text("FITUR PREMIUM", color = AdCyan, fontSize = 11.sp, letterSpacing = 2.sp)
                Text("Peningkatan yang dapat dikonfigurasi langsung dari aplikasi.", color = Color(0xFF9BA8C1), fontSize = 11.sp)
                PremiumControlsCard(
                    context = context,
                    enabled = premiumUnlocked,
                    onChanged = {
                        accessRefresh += 1
                        context.sendBroadcast(
                            Intent("com.adebojel.dynamicislandultra.PREMIUM_APPEARANCE_CHANGED")
                                .setPackage(context.packageName)
                        )
                    }
                )
            }

            if (!premiumUnlocked) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = AdPanel), shape = RoundedCornerShape(18.dp)) {
                        Column(
                            Modifier.fillMaxWidth().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("BUKA PREMIUM 24 JAM DENGAN IKLAN", color = AdGold, fontWeight = FontWeight.Bold)
                            Text(
                                "Pilih sendiri untuk menonton iklan. Seluruh fitur tambahan dibuka selama 24 jam setelah SDK mengonfirmasi hadiah.",
                                color = Color(0xFFB7C2D5),
                                fontSize = 12.sp
                            )
                            Button(
                                onClick = {
                                    AdsSdkManager.showRewarded(
                                        activity,
                                        onRewardEarned = {
                                            PremiumAccess.grantRewardedAccess(context)
                                            accessRefresh += 1
                                            status = "Berhasil! Semua fitur Premium dibuka selama 24 jam."
                                        },
                                        onStatus = { status = it }
                                    )
                                },
                                enabled = adsReady,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                androidx.compose.material3.Icon(Icons.Default.PlayCircle, null)
                                Spacer(Modifier.width(6.dp))
                                Text("Tonton Rewarded Ads")
                            }
                            OutlinedButton(
                                onClick = { showInterstitialDisclosure = true },
                                enabled = adsReady,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Rewarded Interstitial")
                            }
                            Text(
                                "Rewarded Interstitial tidak muncul otomatis. Pengantar dengan pilihan Lanjutkan/Lewati selalu ditampilkan.",
                                color = Color(0xFF8998B2),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = AdPanel), shape = RoundedCornerShape(18.dp)) {
                    Column(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        Text("PAKET PREMIUM GOOGLE PLAY", color = AdCyan, fontWeight = FontWeight.Bold)
                        Text(
                            "Harga dan masa paket akan diambil langsung dari Google Play. Tidak ada harga atau transaksi simulasi di aplikasi.",
                            color = Color(0xFFB7C2D5),
                            fontSize = 11.sp
                        )
                        val offers = PremiumBillingManager.offers
                        if (offers.isEmpty()) {
                            Text(
                                PremiumBillingManager.status.ifBlank {
                                    "Belum ada paket aktif. Buat produk diu_premium dan aktifkan base plan di Play Console."
                                },
                                color = Color(0xFFFFD58A),
                                fontSize = 12.sp
                            )
                        } else {
                            offers.forEach { offer ->
                                Row(
                                    Modifier.fillMaxWidth()
                                        .background(Color(0xFF1A263D), RoundedCornerShape(12.dp))
                                        .padding(11.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(offer.title, color = Color.White, fontWeight = FontWeight.SemiBold)
                                        Text(offer.price, color = AdGold, fontSize = 13.sp)
                                    }
                                    Button(onClick = { PremiumBillingManager.launchPurchase(activity, offer) }) {
                                        Text("Pilih")
                                    }
                                }
                            }
                        }
                        if (PremiumBillingManager.isPremiumActive) {
                            OutlinedButton(
                                onClick = {
                                    activity.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://play.google.com/store/account/subscriptions?sku=${PremiumBillingManager.PREMIUM_PRODUCT_ID}&package=com.adebojel.dynamicislandultra")
                                        )
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Kelola paket di Google Play") }
                        }
                        Text(
                            "Target harga awal: mulai Rp2.000. Harga final mengikuti pengaturan Play Console dan layar pembayaran resmi Google Play.",
                            color = Color(0xFF8998B2),
                            fontSize = 10.sp
                        )
                        Text(
                            "Play Console Indonesia menyediakan base plan prabayar standar untuk 1 minggu, 4 minggu, 1/2/3/4/6/8 bulan, dan 1 tahun. Paket tepat 2 minggu, 3 minggu, serta 2 tahun memerlukan rancangan pass khusus dan validasi entitlement tambahan; ketiganya belum ditawarkan sebagai paket aktif.",
                            color = Color(0xFF8998B2),
                            fontSize = 10.sp
                        )
                    }
                }
            }

            item {
                Text("REKOMENDASI SPONSOR", color = AdCyan, fontSize = 11.sp, letterSpacing = 2.sp)
                Text(
                    "Satu kartu Native Advanced dengan label IKLAN. Tidak ditempatkan pada floating island atau kontrol sistem.",
                    color = Color(0xFFB7C2D5),
                    fontSize = 11.sp
                )
                if (showNative && adsReady && !premiumUnlocked)
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = AdPanel), shape = RoundedCornerShape(18.dp)) {
                    Column(
                        Modifier.fillMaxWidth().padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("KONTROL IKLAN & PRIVASI", color = Color.White, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Banner bawah pada halaman ini", color = Color.White, fontSize = 13.sp)
                                Text("Slot terpisah dan tidak menutupi navigasi.", color = Color(0xFF9BA8C1), fontSize = 10.sp)
                            }
                            Switch(
                                checked = showBanner,
                                onCheckedChange = {
                                    showBanner = it
                                    settings.edit().putBoolean("show_banner", it).apply()
                                }
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Kartu Native Advanced", color = Color.White, fontSize = 13.sp)
                                Text("Maksimal satu kartu di halaman ini.", color = Color(0xFF9BA8C1), fontSize = 10.sp)
                            }
                            Switch(
                                checked = showNative,
                                onCheckedChange = {
                                    showNative = it
                                    settings.edit().putBoolean("show_native", it).apply()
                                }
                            )
                        }
                        if (showPrivacyOptions) {
                            TextButton(
                                onClick = {
                                    AdsSdkManager.showPrivacyOptions(activity) { allowed ->
                                        adsReady = allowed
                                        showPrivacyOptions = AdsSdkManager.privacyOptionsRequired
                                        status = "Pilihan privasi diperbarui."
                                    }
                                }
                            ) { Text("Kelola pilihan privasi iklan") }
                        }
                        Text(PremiumBillingManager.status, color = Color(0xFF9BA8C1), fontSize = 10.sp)
                    }
                }
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = AdPanel), shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("INFORMASI & KEBIJAKAN", color = Color.White, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { openPolicyPage(activity, "privacy.html") }) { Text("Kebijakan Privasi") }
                        TextButton(onClick = { openPolicyPage(activity, "terms.html") }) { Text("Syarat & Ketentuan") }
                        TextButton(onClick = { openPolicyPage(activity, "ads.html") }) { Text("Pengungkapan Iklan") }
                        TextButton(onClick = { openPolicyPage(activity, "premium.html") }) { Text("Informasi Premium & Harga") }
                        TextButton(onClick = { openPolicyPage(activity, "support.html") }) { Text("Bantuan & Dukungan") }
                    }
                }
            }

            item {
                Text(status, color = Color(0xFFB7C2D5), fontSize = 12.sp)
                Text(
                    "Tidak ada iklan dari floating island, asisten, mikrofon, notifikasi, browser, permission center, atau kontrol sistem. Durasi video mengikuti materi iklan AdMob dan tidak dapat dijamin maksimal 30 detik.",
                    color = Color(0xFF7F8DA7),
                    fontSize = 10.sp
                )
            }
        }

        if (showBanner && adsReady && !premiumUnlocked) {
            AdBannerSlot(
                modifier = Modifier.fillMaxWidth().background(Color(0xFF0C1322)).padding(vertical = 4.dp),
                enabled = true
            )
        }
    }

    if (showInterstitialDisclosure) {
        AlertDialog(
            onDismissRequest = { showInterstitialDisclosure = false },
            title = { Text("Buka Premium selama 24 jam") },
            text = {
                Text(
                    "Jika Anda memilih Lanjutkan, iklan layar penuh akan ditampilkan. Anda boleh memilih Lewati. Seluruh fitur Premium hanya dibuka setelah SDK mengonfirmasi bahwa hadiah telah diperoleh."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showInterstitialDisclosure = false
                    AdsSdkManager.showRewardedInterstitial(
                        activity,
                        onRewardEarned = {
                            PremiumAccess.grantRewardedAccess(context)
                            accessRefresh += 1
                            status = "Berhasil! Semua fitur Premium dibuka selama 24 jam."
                        },
                        onStatus = { status = it }
                    )
                }) { Text("Lanjutkan") }
            },
            dismissButton = {
                TextButton(onClick = { showInterstitialDisclosure = false }) { Text("Lewati") }
            }
        )
    }
}

@Composable
private fun PremiumControlsCard(context: Context, enabled: Boolean, onChanged: () -> Unit) {
    var accent by remember(enabled) { mutableStateOf(PremiumAccess.accentColor(context)) }
    var pulse by remember(enabled) { mutableStateOf(PremiumAccess.pulseStyle(context)) }
    var scale by remember(enabled) { mutableStateOf(PremiumAccess.islandScale(context)) }
    var compact by remember(enabled) { mutableStateOf(PremiumAccess.compactPanel(context)) }

    Card(colors = CardDefaults.cardColors(containerColor = AdPanel), shape = RoundedCornerShape(18.dp)) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Advanced RGB & Island Controls", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                androidx.compose.material3.Icon(
                    if (enabled) Icons.Default.CheckCircle else Icons.Default.Lock,
                    null,
                    tint = if (enabled) AdEmerald else AdGold
                )
            }
            if (!enabled) {
                Text("Kustomisasi terkunci. Buka akses dengan iklan 24 jam atau paket Premium Google Play.", color = Color(0xFFB7C2D5), fontSize = 11.sp)
            }

            Text("1. RGB Theme", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(
                    Color(0xFF43E8FF) to AndroidColor.rgb(67, 232, 255),
                    Color(0xFFB36BFF) to AndroidColor.rgb(179, 107, 255),
                    Color(0xFF41DC91) to AndroidColor.rgb(65, 220, 145),
                    Color(0xFFFFC857) to AndroidColor.rgb(255, 200, 87)
                ).forEach { (color, value) ->
                    Box(
                        Modifier.size(34.dp)
                            .background(color, CircleShape)
                            .clickable(enabled = enabled) {
                                accent = value
                                PremiumAccess.saveAccent(context, value)
                                onChanged()
                            }
                    ) {
                        if (accent == value) {
                            androidx.compose.material3.Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Warna dipilih",
                                tint = Color.Black,
                                modifier = Modifier.align(Alignment.Center).size(18.dp)
                            )
                        }
                    }
                }
            }

            Text("2. Island Pulse FX", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                listOf("soft" to "Soft", "standard" to "Normal", "fast" to "Fast", "off" to "Off").forEach { (id, label) ->
                    FilterChip(
                        selected = pulse == id,
                        onClick = {
                            pulse = id
                            PremiumAccess.savePulseStyle(context, id)
                            onChanged()
                        },
                        enabled = enabled,
                        label = { Text(label, fontSize = 9.sp) }
                    )
                }
            }

            Text("3. Island Size", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(
                when {
                    scale < 0.95f -> "Small"
                    scale > 1.08f -> "Large"
                    else -> "Normal"
                },
                color = AdCyan,
                fontSize = 11.sp
            )
            Slider(
                value = scale,
                onValueChange = { value ->
                    scale = value
                    PremiumAccess.saveIslandScale(context, value)
                    onChanged()
                },
                valueRange = 0.85f..1.2f,
                enabled = enabled
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("4. Compact Panel", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("Mengurangi tinggi panel floating tools.", color = Color(0xFF9BA8C1), fontSize = 10.sp)
                }
                Switch(
                    checked = compact,
                    onCheckedChange = {
                        compact = it
                        PremiumAccess.saveCompactPanel(context, it)
                        onChanged()
                    },
                    enabled = enabled
                )
            }
        }
    }
}

@Composable
private fun AdBannerSlot(modifier: Modifier = Modifier, enabled: Boolean) {
    val context = LocalContext.current
    val widthDp = LocalConfiguration.current.screenWidthDp
    if (!enabled || LocalInspectionMode.current) return

    val adView = remember(context) { AdView(context) }
    AndroidView(factory = { adView }, modifier = modifier.fillMaxWidth())
    DisposableEffect(adView, widthDp) {
        val size = AdSize.getLargeAnchoredAdaptiveBannerAdSize(context, widthDp)
        val request = BannerAdRequest.Builder(AdsSdkManager.bannerUnitId, size).build()
        adView.loadAd(request, object : AdLoadCallback<BannerAd> {
            override fun onAdLoaded(ad: BannerAd) {
                android.util.Log.d("DynamicIslandAds", "Banner loaded.")
            }
            override fun onAdFailedToLoad(adError: LoadAdError) {
                android.util.Log.w("DynamicIslandAds", "Banner failed: ${adError.message}")
            }
        })
        onDispose {
            (adView.parent as? ViewGroup)?.removeView(adView)
            adView.destroy()
        }
    }
}

@Composable
private fun NativeAdSlot(enabled: Boolean) {
    val context = LocalContext.current
    if (!enabled || LocalInspectionMode.current) return

    var loaded by remember { mutableStateOf(false) }
    var loadFailed by remember { mutableStateOf(false) }
    val container = remember(context) { FrameLayout(context) }
    val currentAd = remember { mutableStateOf<NativeAd?>(null) }

    AndroidView(
        factory = { container },
        modifier = Modifier.fillMaxWidth().height(if (loaded) 280.dp else 1.dp)
    )

    DisposableEffect(container) {
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
                        currentAd.value?.destroy()
                        currentAd.value = nativeAd
                        container.removeAllViews()
                        container.addView(createNativeAdView(context, nativeAd))
                        loaded = true
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
            currentAd.value?.destroy()
            currentAd.value = null
        }
    }

    if (loadFailed && !loaded) {
        Text("Iklan sponsor belum tersedia.", color = Color(0xFF9BA8C1), fontSize = 11.sp)
    }
}

private fun createNativeAdView(context: Context, ad: NativeAd): NativeAdView {
    val density = context.resources.displayMetrics.density
    fun dp(value: Int) = (value * density).toInt()

    val nativeView = NativeAdView(context).apply {
        background = GradientDrawable().apply {
            setColor(AndroidColor.rgb(18, 26, 43))
            cornerRadius = dp(18).toFloat()
            setStroke(dp(1), AndroidColor.rgb(54, 76, 104))
        }
        setPadding(dp(14), dp(12), dp(14), dp(12))
    }
    val content = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
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
    val icon = ImageView(context).apply {
        layoutParams = LinearLayout.LayoutParams(dp(42), dp(42)).apply { bottomMargin = dp(6) }
        scaleType = ImageView.ScaleType.CENTER_CROP
    }
    val headline = TextView(context).apply {
        textSize = 15f
        setTextColor(AndroidColor.WHITE)
        setTypeface(typeface, Typeface.BOLD)
        setPadding(0, dp(5), 0, dp(5))
    }
    val media = MediaView(context).apply {
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(132))
            .apply { topMargin = dp(5); bottomMargin = dp(6) }
    }
    val body = TextView(context).apply {
        textSize = 12f
        setTextColor(AndroidColor.LTGRAY)
        maxLines = 3
    }
    val cta = TextView(context).apply {
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
    icon.setImageDrawable(ad.icon?.drawable)
    headline.text = ad.headline.orEmpty()
    body.text = ad.body.orEmpty()
    cta.text = ad.callToAction.orEmpty()
    advertiser.visibility = if (ad.advertiser.isNullOrBlank()) View.GONE else View.VISIBLE
    icon.visibility = if (ad.icon?.drawable == null) View.GONE else View.VISIBLE
    body.visibility = if (ad.body.isNullOrBlank()) View.GONE else View.VISIBLE
    cta.visibility = if (ad.callToAction.isNullOrBlank()) View.GONE else View.VISIBLE

    content.addView(attribution)
    content.addView(advertiser)
    content.addView(icon)
    content.addView(headline)
    content.addView(media)
    content.addView(body)
    content.addView(cta)
    nativeView.addView(
        content,
        FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    )
    nativeView.advertiserView = advertiser
    nativeView.iconView = icon
    nativeView.headlineView = headline
    nativeView.bodyView = body
    nativeView.callToActionView = cta
    nativeView.registerNativeAd(ad, media)
    return nativeView
}

private fun openPolicyPage(activity: Activity, page: String) {
    val url = "https://adebojel.github.io/dynamic-island-ultra/$page"
    runCatching { activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
}
