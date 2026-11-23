package com.adebojel.dynamicislandultra.notifications

import android.content.Context
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import org.json.JSONArray
import kotlin.math.ln

data class EqualizerBandState(val index: Int, val centerHz: Int, val levelMb: Int)

data class EqualizerSnapshot(
    val available: Boolean,
    val enabled: Boolean,
    val bands: List<EqualizerBandState>,
    val minLevelMb: Int,
    val maxLevelMb: Int,
    val preampMb: Int,
    val preset: String,
    val message: String
)

object EqualizerPolicy {
    const val MIN_PREAMP_MB = -1200
    const val MAX_PREAMP_MB = 600
    const val PREFS = "audio_equalizer"
    const val KEY_ENABLED = "enabled"
    const val KEY_PREAMP = "preamp_mb"
    const val KEY_BANDS = "bands"
    const val KEY_PRESET = "preset"

    val presets = listOf("Flat", "Bass Boost", "Treble", "Vocal", "Rock", "Jazz", "Classical", "Custom")

    fun clampPreamp(value: Int): Int = value.coerceIn(MIN_PREAMP_MB, MAX_PREAMP_MB)

    fun presetGainMb(preset: String, frequencyHz: Int): Int {
        val f = frequencyHz.coerceAtLeast(1)
        val bass = when (preset) {
            "Bass Boost" -> 550
            "Rock" -> 350
            "Jazz" -> 250
            "Classical" -> 180
            else -> 0
        }
        val presence = when (preset) {
            "Vocal" -> 300
            "Rock" -> 120
            "Jazz" -> 100
            else -> 0
        }
        val treble = when (preset) {
            "Treble" -> 500
            "Rock" -> 300
            "Jazz" -> 220
            "Classical" -> 280
            else -> 0
        }
        val low = when {
            f < 180 -> bass
            f < 500 -> bass / 2
            else -> 0
        }
        val mid = when {
            f in 700..3500 -> presence
            f in 3501..6000 -> presence / 2
            else -> 0
        }
        val high = when {
            f > 7000 -> treble
            f > 4500 -> treble / 2
            else -> 0
        }
        return (low + mid + high).coerceIn(-1500, 1500)
    }
}

/**
 * Best-effort Android output-mix effects (audio session 0).
 * Device audio-effect implementations may reject global output effects.
 * This cannot modify arbitrary apps' private player sessions on every device.
 */
class SystemAudioEqualizer(private val context: Context) {
    private var equalizer: Equalizer? = null
    private var loudness: LoudnessEnhancer? = null
    private var minLevel = -1500
    private var maxLevel = 1500
    private var preamp = 0
    private var enabled = false
    private var currentPreset = "Flat"

    fun attach(): EqualizerSnapshot {
        release()
        val prefs = context.getSharedPreferences(EqualizerPolicy.PREFS, Context.MODE_PRIVATE)
        return try {
            val effect = Equalizer(0, 0)
            equalizer = effect
            val range = effect.bandLevelRange
            minLevel = range[0].toInt()
            maxLevel = range[1].toInt()
            preamp = EqualizerPolicy.clampPreamp(prefs.getInt(EqualizerPolicy.KEY_PREAMP, 0))
            currentPreset = prefs.getString(EqualizerPolicy.KEY_PRESET, "Flat") ?: "Flat"
            val saved = runCatching { JSONArray(prefs.getString(EqualizerPolicy.KEY_BANDS, "[]")) }.getOrNull()
            for (i in 0 until effect.numberOfBands) {
                val band = i.toShort()
                val center = effect.getCenterFreq(band).coerceAtLeast(0) / 1000
                val level = if (saved != null && i < saved.length()) {
                    saved.optInt(i, 0).coerceIn(minLevel, maxLevel)
                } else {
                    EqualizerPolicy.presetGainMb(currentPreset, center)
                        .coerceIn(minLevel, maxLevel)
                }
                effect.setBandLevel(band, level.toShort())
            }
            enabled = prefs.getBoolean(EqualizerPolicy.KEY_ENABLED, false)
            effect.enabled = enabled
            runCatching {
                LoudnessEnhancer(0).also {
                    loudness = it
                    it.setTargetGain(preamp)
                    it.enabled = enabled && preamp != 0
                }
            }
            snapshot("Equalizer output mix tersedia pada perangkat ini.")
        } catch (failure: Throwable) {
            release()
            snapshot("Equalizer output mix tidak didukung perangkat ini: ${failure.message ?: "audio effect unavailable"}")
        }
    }

    fun snapshot(message: String = ""): EqualizerSnapshot {
        val effect = equalizer
        val bands = if (effect == null) emptyList() else runCatching {
            (0 until effect.numberOfBands).map { i ->
                EqualizerBandState(
                    index = i,
                    centerHz = effect.getCenterFreq(i.toShort()).coerceAtLeast(0) / 1000,
                    levelMb = effect.getBandLevel(i.toShort()).toInt()
                )
            }
        }.getOrDefault(emptyList())
        return EqualizerSnapshot(
            available = effect != null && bands.isNotEmpty(),
            enabled = enabled,
            bands = bands,
            minLevelMb = minLevel,
            maxLevelMb = maxLevel,
            preampMb = preamp,
            preset = currentPreset,
            message = message
        )
    }

    fun setEnabled(value: Boolean): EqualizerSnapshot {
        enabled = value
        runCatching { equalizer?.enabled = value }
        runCatching { loudness?.enabled = value && preamp != 0 }
        persist()
        return snapshot()
    }

    fun setBand(index: Int, levelMb: Int): EqualizerSnapshot {
        val effect = equalizer ?: return snapshot()
        if (index in 0 until effect.numberOfBands) {
            runCatching { effect.setBandLevel(index.toShort(), levelMb.coerceIn(minLevel, maxLevel).toShort()) }
            currentPreset = "Custom"
            persist()
        }
        return snapshot()
    }

    fun applyPreset(name: String): EqualizerSnapshot {
        val effect = equalizer ?: return snapshot()
        val selected = name.takeIf { it in EqualizerPolicy.presets } ?: "Flat"
        for (i in 0 until effect.numberOfBands) {
            val frequency = effect.getCenterFreq(i.toShort()).coerceAtLeast(0) / 1000
            val level = EqualizerPolicy.presetGainMb(selected, frequency).coerceIn(minLevel, maxLevel)
            runCatching { effect.setBandLevel(i.toShort(), level.toShort()) }
        }
        currentPreset = selected
        if (selected != "Custom") preamp = if (selected == "Bass Boost") 150 else 0
        runCatching { loudness?.setTargetGain(preamp) }
        runCatching { loudness?.enabled = enabled && preamp != 0 }
        persist()
        return snapshot()
    }

    fun setPreamp(value: Int): EqualizerSnapshot {
        preamp = EqualizerPolicy.clampPreamp(value)
        runCatching { loudness?.setTargetGain(preamp) }
        runCatching { loudness?.enabled = enabled && preamp != 0 }
        currentPreset = "Custom"
        persist()
        return snapshot()
    }

    fun release() {
        runCatching { equalizer?.enabled = false }
        runCatching { loudness?.enabled = false }
        runCatching { equalizer?.release() }
        runCatching { loudness?.release() }
        equalizer = null
        loudness = null
    }

    private fun persist() {
        val effect = equalizer ?: return
        val levels = JSONArray()
        for (i in 0 until effect.numberOfBands) {
            levels.put(runCatching { effect.getBandLevel(i.toShort()).toInt() }.getOrDefault(0))
        }
        context.getSharedPreferences(EqualizerPolicy.PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(EqualizerPolicy.KEY_ENABLED, enabled)
            .putInt(EqualizerPolicy.KEY_PREAMP, preamp)
            .putString(EqualizerPolicy.KEY_BANDS, levels.toString())
            .putString(EqualizerPolicy.KEY_PRESET, currentPreset)
            .apply()
    }
}
