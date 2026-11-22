package com.adebojel.dynamicislandultra.floating

import kotlin.math.atan2
import kotlin.math.sqrt

/** Converts actual accelerometer gravity readings into pitch and roll angles. */
internal object LevelSensorMath {
    /** Returns Pair(pitchDegrees, rollDegrees), or null for invalid sensor samples. */
    fun angles(values: FloatArray): Pair<Float, Float>? {
        if (values.size < 3 || values.take(3).any { !it.isFinite() }) return null
        val x = values[0].toDouble()
        val y = values[1].toDouble()
        val z = values[2].toDouble()
        val pitch = Math.toDegrees(atan2(-x, sqrt(y * y + z * z))).toFloat()
        val roll = Math.toDegrees(atan2(y, z)).toFloat()
        if (!pitch.isFinite() || !roll.isFinite()) return null
        return pitch to roll
    }
}
