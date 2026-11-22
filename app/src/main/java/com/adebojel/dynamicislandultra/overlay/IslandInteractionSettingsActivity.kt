package com.adebojel.dynamicislandultra.overlay

import android.content.Intent
import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

class IslandInteractionSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("island", MODE_PRIVATE)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFF43E8FF))) {
                var snap by remember { mutableStateOf(prefs.getBoolean("edge_docking", true)) }
                var haptics by remember { mutableStateOf(prefs.getBoolean("haptic_enabled", false)) }
                var autoDim by remember { mutableStateOf(prefs.getBoolean("auto_dim_enabled", false)) }
                var bubbleSize by remember { mutableFloatStateOf(prefs.getFloat("bubble_size", 1f).coerceIn(0.8f, 1.4f)) }
                var bubbleOpacity by remember { mutableFloatStateOf(prefs.getFloat("bubble_opacity", 1f).coerceIn(0.4f, 1f)) }
                val initialHsv = remember {
                    FloatArray(3).also {
                        AndroidColor.colorToHSV(prefs.getInt("custom_accent_color", AndroidColor.rgb(67, 232, 255)), it)
                    }
                }
                var accentHue by remember { mutableFloatStateOf(initialHsv[0]) }
                var accentSaturation by remember { mutableFloatStateOf(initialHsv[1]) }
                var accentBrightness by remember { mutableFloatStateOf(initialHsv[2]) }
                val customAccent = Color(AndroidColor.HSVToColor(floatArrayOf(accentHue, accentSaturation, accentBrightness)))
                var tapAction by remember { mutableStateOf(BubbleGestureConfig.resolve("tap", prefs.getString("gesture_tap", null))) }
                var doubleTapAction by remember { mutableStateOf(BubbleGestureConfig.resolve("double_tap", prefs.getString("gesture_double_tap", null))) }
                var longPressAction by remember { mutableStateOf(BubbleGestureConfig.resolve("long_press", prefs.getString("gesture_long_press", null))) }
                var swipeUpAction by remember { mutableStateOf(BubbleGestureConfig.resolve("swipe_up", prefs.getString("gesture_swipe_up", null))) }
                var swipeDownAction by remember { mutableStateOf(BubbleGestureConfig.resolve("swipe_down", prefs.getString("gesture_swipe_down", null))) }
                var swipeLeftAction by remember { mutableStateOf(BubbleGestureConfig.resolve("swipe_left", prefs.getString("gesture_swipe_left", null))) }
                var swipeRightAction by remember { mutableStateOf(BubbleGestureConfig.resolve("swipe_right", prefs.getString("gesture_swipe_right", null))) }
                var favorite1 by remember { mutableStateOf(BubbleFavoriteConfig.resolve(0, prefs.getString("favorite_1", null))) }
                var favorite2 by remember { mutableStateOf(BubbleFavoriteConfig.resolve(1, prefs.getString("favorite_2", null))) }
                var favorite3 by remember { mutableStateOf(BubbleFavoriteConfig.resolve(2, prefs.getString("favorite_3", null))) }
                var favorite4 by remember { mutableStateOf(BubbleFavoriteConfig.resolve(3, prefs.getString("favorite_4", null))) }

                Column(
                    Modifier.fillMaxSize()
                        .background(Color(0xFF0B1222))
                        .verticalScroll(rememberScrollState())
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("ISLAND INTERACTION", color = Color(0xFF43E8FF), fontSize = 18.sp)
                    Text(
                        "Atur posisi, ukuran, transparansi, dan aksi gesture bubble. Semua preferensi disimpan lokal.",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )

                    SettingSwitch(
                        title = "Snap ke tepi layar",
                        description = "Pill menempel ke sisi kiri atau kanan setelah digeser.",
                        checked = snap,
                        onCheckedChange = {
                            snap = it
                            prefs.edit().putBoolean("edge_docking", it).apply()
                        }
                    )
                    SettingSwitch(
                        title = "Haptic feedback",
                        description = "Getaran singkat saat pill dilepas atau long-press dijalankan.",
                        checked = haptics,
                        onCheckedChange = {
                            haptics = it
                            prefs.edit().putBoolean("haptic_enabled", it).apply()
                        }
                    )

                    SettingSwitch(
                        title = "Auto-dim setelah 8 detik",
                        description = "Bubble meredup saat tidak disentuh, tetapi tetap terlihat dan dapat diketuk.",
                        checked = autoDim,
                        onCheckedChange = {
                            autoDim = it
                            prefs.edit().putBoolean("auto_dim_enabled", it).apply()
                            refreshBubbleAppearance()
                        }
                    )

                    Text("UKURAN BUBBLE · ${(bubbleSize * 100).roundToInt()}%", color = Color.White)
                    Slider(
                        value = bubbleSize,
                        onValueChange = {
                            bubbleSize = it
                            prefs.edit().putFloat("bubble_size", it).apply()
                        },
                        valueRange = 0.8f..1.4f,
                        onValueChangeFinished = { refreshBubbleAppearance() }
                    )
                    Text("OPASITAS · ${(bubbleOpacity * 100).roundToInt()}%", color = Color.White)
                    Slider(
                        value = bubbleOpacity,
                        onValueChange = {
                            bubbleOpacity = it
                            prefs.edit().putFloat("bubble_opacity", it).apply()
                        },
                        valueRange = 0.4f..1f,
                        onValueChangeFinished = { refreshBubbleAppearance() }
                    )

                    Text("CUSTOM RGB ACCENT", color = Color(0xFF43E8FF), fontSize = 14.sp)
                    Text(
                        "Atur rona, saturasi dan kecerahan aksen Floating Island. Warna disimpan lokal pada perangkat.",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                    Surface(
                        color = customAccent,
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "#%06X".format(0xFFFFFF and AndroidColor.HSVToColor(floatArrayOf(accentHue, accentSaturation, accentBrightness))),
                                color = if (accentBrightness > 0.72f) Color.Black else Color.White
                            )
                        }
                    }
                    Text("HUE · ${accentHue.toInt()}°", color = Color.White)
                    Slider(
                        value = accentHue,
                        onValueChange = {
                            accentHue = it
                            saveCustomAccent(prefs, accentHue, accentSaturation, accentBrightness)
                        },
                        valueRange = 0f..360f,
                        onValueChangeFinished = { refreshBubbleAppearance() }
                    )
                    Text("SATURATION · ${(accentSaturation * 100).toInt()}%", color = Color.White)
                    Slider(
                        value = accentSaturation,
                        onValueChange = {
                            accentSaturation = it
                            saveCustomAccent(prefs, accentHue, accentSaturation, accentBrightness)
                        },
                        valueRange = 0.2f..1f,
                        onValueChangeFinished = { refreshBubbleAppearance() }
                    )
                    Text("BRIGHTNESS · ${(accentBrightness * 100).toInt()}%", color = Color.White)
                    Slider(
                        value = accentBrightness,
                        onValueChange = {
                            accentBrightness = it
                            saveCustomAccent(prefs, accentHue, accentSaturation, accentBrightness)
                        },
                        valueRange = 0.4f..1f,
                        onValueChangeFinished = { refreshBubbleAppearance() }
                    )
                    OutlinedButton(
                        onClick = {
                            accentHue = 190f
                            accentSaturation = 0.86f
                            accentBrightness = 0.91f
                            saveCustomAccent(prefs, accentHue, accentSaturation, accentBrightness)
                            refreshBubbleAppearance()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Reset warna aksen") }

                    Text("GESTURE MAPPING", color = Color(0xFF43E8FF), fontSize = 14.sp)
                    Text(
                        "Swipe hanya mengambil alih gerakan saat tindakan swipe terkait diaktifkan. Jika tidak, bubble tetap dapat digeser.",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                    GestureSettingRow("Tap", "Sentuhan satu kali", "tap", tapAction) {
                        tapAction = it
                        saveGesture("tap", it)
                    }
                    GestureSettingRow("Double tap", "Dua sentuhan cepat", "double_tap", doubleTapAction) {
                        doubleTapAction = it
                        saveGesture("double_tap", it)
                    }
                    GestureSettingRow("Long press", "Tekan dan tahan", "long_press", longPressAction) {
                        longPressAction = it
                        saveGesture("long_press", it)
                    }
                    GestureSettingRow("Swipe up", "Geser ke atas", "swipe_up", swipeUpAction) {
                        swipeUpAction = it
                        saveGesture("swipe_up", it)
                    }
                    GestureSettingRow("Swipe down", "Geser ke bawah", "swipe_down", swipeDownAction) {
                        swipeDownAction = it
                        saveGesture("swipe_down", it)
                    }
                    GestureSettingRow("Swipe left", "Geser ke kiri", "swipe_left", swipeLeftAction) {
                        swipeLeftAction = it
                        saveGesture("swipe_left", it)
                    }
                    GestureSettingRow("Swipe right", "Geser ke kanan", "swipe_right", swipeRightAction) {
                        swipeRightAction = it
                        saveGesture("swipe_right", it)
                    }


                    Text("QUICK FAVORITES", color = Color(0xFF43E8FF), fontSize = 14.sp)
                    Text(
                        "Pilih empat pintasan yang selalu muncul di bagian atas menu bubble.",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                    FavoriteSettingRow("Slot favorit 1", favorite1) {
                        favorite1 = it
                        saveFavorite(0, it)
                    }
                    FavoriteSettingRow("Slot favorit 2", favorite2) {
                        favorite2 = it
                        saveFavorite(1, it)
                    }
                    FavoriteSettingRow("Slot favorit 3", favorite3) {
                        favorite3 = it
                        saveFavorite(2, it)
                    }
                    FavoriteSettingRow("Slot favorit 4", favorite4) {
                        favorite4 = it
                        saveFavorite(3, it)
                    }

                    OutlinedButton(onClick = { finish() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Selesai")
                    }
                }
            }
        }
    }

    private fun saveCustomAccent(
        prefs: android.content.SharedPreferences,
        hue: Float,
        saturation: Float,
        brightness: Float
    ) {
        val color = AndroidColor.HSVToColor(floatArrayOf(
            hue.coerceIn(0f, 360f),
            saturation.coerceIn(0.2f, 1f),
            brightness.coerceIn(0.4f, 1f)
        ))
        prefs.edit().putInt("custom_accent_color", color).apply()
    }

    private fun saveGesture(gesture: String, action: String) {
        getSharedPreferences("island", MODE_PRIVATE).edit()
            .putString("gesture_$gesture", BubbleGestureConfig.resolve(gesture, action))
            .apply()
    }

    private fun saveFavorite(slot: Int, action: String) {
        getSharedPreferences("island", MODE_PRIVATE).edit()
            .putString("favorite_${slot + 1}", BubbleFavoriteConfig.resolve(slot, action))
            .apply()
    }

    private fun refreshBubbleAppearance() {
        sendBroadcast(Intent(FloatingIslandService.ACTION_APPEARANCE_CHANGED).setPackage(packageName))
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White)
            Text(description, color = Color.LightGray, fontSize = 10.sp)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}


@Composable
private fun FavoriteSettingRow(
    title: String,
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, color = Color.White)
        Box {
            OutlinedButton(onClick = { expanded = true }) {
                Text(BubbleFavoriteConfig.optionFor(selected).label)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                BubbleFavoriteConfig.options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = {
                            onSelect(option.key)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun GestureSettingRow(
    title: String,
    description: String,
    gesture: String,
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, color = Color.White)
        Text(description, color = Color.LightGray, fontSize = 10.sp)
        Box {
            OutlinedButton(onClick = { expanded = true }) {
                Text(BubbleGestureConfig.labelFor(selected))
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                BubbleGestureConfig.optionsFor(gesture).forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = {
                            onSelect(option.key)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
