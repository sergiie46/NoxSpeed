package com.noxforgestudios.noxspeed.gps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.noxforgestudios.noxspeed.data.prefs.GpsFilterMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GpsEngine(private val context: Context) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val filter = GpsFilter()
    private var filterMode: GpsFilterMode = GpsFilterMode.BALANCED
    private var running = false
    private var satellitesUsed = 0

    private val _state = MutableStateFlow(GpsSnapshot())
    val state: StateFlow<GpsSnapshot> = _state.asStateFlow()

    private val listener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val accuracy = location.accuracy.takeIf { location.hasAccuracy() } ?: 99f
            val raw = when {
                location.hasSpeed() -> location.speed.toDouble()
                else -> 0.0
            }
            val elapsed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                location.elapsedRealtimeNanos / 1_000_000L
            } else {
                System.currentTimeMillis()
            }
            val speed = filter.filter(raw, accuracy, elapsed, filterMode)
            _state.value = GpsSnapshot(
                available = accuracy <= 60f,
                providerEnabled = isProviderEnabled(),
                hasPermission = hasLocationPermission(),
                speedMps = speed,
                rawSpeedMps = raw,
                latitude = location.latitude,
                longitude = location.longitude,
                altitudeMeters = location.altitude.takeIf { location.hasAltitude() },
                bearingDegrees = location.bearing.takeIf { location.hasBearing() },
                accuracyMeters = accuracy,
                satellitesUsed = satellitesUsed,
                timestampMs = location.time,
                elapsedRealtimeMs = elapsed,
            )
        }

        @Deprecated("Deprecated in Android")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
        override fun onProviderEnabled(provider: String) { refreshProviderState() }
        override fun onProviderDisabled(provider: String) { refreshProviderState() }
    }

    private val gnssCallback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        object : GnssStatus.Callback() {
            override fun onSatelliteStatusChanged(status: GnssStatus) {
                var used = 0
                for (i in 0 until status.satelliteCount) if (status.usedInFix(i)) used++
                satellitesUsed = used
                _state.value = _state.value.copy(satellitesUsed = used)
            }
        }
    } else null

    fun setFilterMode(mode: GpsFilterMode) {
        if (filterMode != mode) {
            filterMode = mode
            filter.reset()
        }
    }

    fun refreshPermissionState() {
        _state.value = _state.value.copy(
            hasPermission = hasLocationPermission(),
            providerEnabled = isProviderEnabled(),
        )
    }

    @SuppressLint("MissingPermission")
    fun start() {
        refreshPermissionState()
        if (running || !hasLocationPermission()) return
        running = true
        filter.reset()
        val providers = buildList {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                add(LocationManager.GPS_PROVIDER)
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                // Fallback only. Network locations are less suitable for speed but keep the UI informative.
                add(LocationManager.NETWORK_PROVIDER)
            }
        }
        providers.forEach { provider ->
            locationManager.requestLocationUpdates(provider, 250L, 0f, listener, Looper.getMainLooper())
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && gnssCallback != null) {
            locationManager.registerGnssStatusCallback(gnssCallback, Handler(Looper.getMainLooper()))
        }
    }

    fun stop() {
        if (!running) return
        running = false
        locationManager.removeUpdates(listener)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && gnssCallback != null) {
            locationManager.unregisterGnssStatusCallback(gnssCallback)
        }
        filter.reset()
    }

    fun injectDemoSpeed(speedMps: Double) {
        if (!com.noxforgestudios.noxspeed.BuildConfig.DEMO_GPS_AVAILABLE) return
        val now = System.currentTimeMillis()
        _state.value = _state.value.copy(
            available = true,
            providerEnabled = true,
            hasPermission = true,
            speedMps = speedMps.coerceAtLeast(0.0),
            rawSpeedMps = speedMps.coerceAtLeast(0.0),
            accuracyMeters = 3f,
            satellitesUsed = 14,
            timestampMs = now,
            elapsedRealtimeMs = now,
        )
    }

    private fun refreshProviderState() {
        _state.value = _state.value.copy(providerEnabled = isProviderEnabled())
    }

    private fun isProviderEnabled(): Boolean = runCatching {
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }.getOrDefault(false)

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
}
