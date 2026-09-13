package com.noxforgestudios.noxspeed.gps

import com.noxforgestudios.noxspeed.data.prefs.GpsFilterMode
import kotlin.math.abs

/**
 * Conservative speed filter. It rejects clearly implausible GPS spikes and applies a small EMA.
 * It never fabricates movement when the source location is stale or invalid.
 */
class GpsFilter {
    private var filtered = 0.0
    private var previousRaw = 0.0
    private var previousTimeMs = 0L
    private var initialized = false

    fun reset() {
        filtered = 0.0
        previousRaw = 0.0
        previousTimeMs = 0L
        initialized = false
    }

    fun filter(
        rawSpeedMps: Double,
        accuracyMeters: Float,
        elapsedMs: Long,
        mode: GpsFilterMode,
    ): Double {
        if (!rawSpeedMps.isFinite() || rawSpeedMps < 0.0 || rawSpeedMps > 120.0) return filtered
        if (accuracyMeters > 80f) return filtered

        if (!initialized) {
            initialized = true
            filtered = if (rawSpeedMps < stationaryThreshold(accuracyMeters)) 0.0 else rawSpeedMps
            previousRaw = rawSpeedMps
            previousTimeMs = elapsedMs
            return filtered
        }

        val dt = ((elapsedMs - previousTimeMs).coerceAtLeast(1L) / 1000.0).coerceAtMost(5.0)
        val acceleration = abs(rawSpeedMps - previousRaw) / dt
        // A GPS jump above ~3 g is overwhelmingly likely to be bad data for this use case.
        if (acceleration > 30.0 && accuracyMeters > 8f) return filtered

        val candidate = if (rawSpeedMps < stationaryThreshold(accuracyMeters)) 0.0 else rawSpeedMps
        val alpha = when (mode) {
            GpsFilterMode.FAST -> 0.72
            GpsFilterMode.BALANCED -> 0.48
            GpsFilterMode.SMOOTH -> 0.28
        }

        filtered += alpha * (candidate - filtered)
        if (candidate == 0.0 && filtered < 0.35) filtered = 0.0
        previousRaw = rawSpeedMps
        previousTimeMs = elapsedMs
        return filtered.coerceAtLeast(0.0)
    }

    private fun stationaryThreshold(accuracy: Float): Double = when {
        accuracy <= 5f -> 0.35
        accuracy <= 12f -> 0.65
        else -> 1.1
    }
}
