package com.noxforgestudios.noxspeed.gps

import com.noxforgestudios.noxspeed.data.prefs.SpeedUnit
import kotlin.math.roundToInt

data class GpsSnapshot(
    val available: Boolean = false,
    val providerEnabled: Boolean = true,
    val hasPermission: Boolean = false,
    val speedMps: Double = 0.0,
    val rawSpeedMps: Double = 0.0,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitudeMeters: Double? = null,
    val bearingDegrees: Float? = null,
    val accuracyMeters: Float? = null,
    val satellitesUsed: Int = 0,
    val timestampMs: Long = 0L,
    val elapsedRealtimeMs: Long = 0L,
)

object Units {
    fun speedFromMps(mps: Double, unit: SpeedUnit): Double = when (unit) {
        SpeedUnit.KMH -> mps * 3.6
        SpeedUnit.MPH -> mps * 2.2369362921
        SpeedUnit.KNOTS -> mps * 1.9438444924
    }

    fun unitLabel(unit: SpeedUnit): String = when (unit) {
        SpeedUnit.KMH -> "km/h"
        SpeedUnit.MPH -> "mph"
        SpeedUnit.KNOTS -> "kn"
    }

    fun distanceLabel(meters: Double, unit: SpeedUnit): String = when (unit) {
        SpeedUnit.MPH -> "%.1f mi".format(meters / 1609.344)
        SpeedUnit.KNOTS -> "%.1f nm".format(meters / 1852.0)
        SpeedUnit.KMH -> "%.1f km".format(meters / 1000.0)
    }

    fun roundedSpeed(mps: Double, unit: SpeedUnit): Int = speedFromMps(mps, unit).roundToInt().coerceAtLeast(0)
}
