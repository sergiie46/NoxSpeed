package com.noxforgestudios.noxspeed.ui

import android.app.Application
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.noxforgestudios.noxspeed.NoxSpeedApplication
import com.noxforgestudios.noxspeed.data.db.AccelerationResultEntity
import com.noxforgestudios.noxspeed.data.db.TripEntity
import com.noxforgestudios.noxspeed.data.prefs.DashboardStyle
import com.noxforgestudios.noxspeed.data.prefs.AccentPreset
import com.noxforgestudios.noxspeed.data.prefs.GpsFilterMode
import com.noxforgestudios.noxspeed.data.prefs.HudStyle
import com.noxforgestudios.noxspeed.data.prefs.VehicleMode
import com.noxforgestudios.noxspeed.data.prefs.SpeedUnit
import com.noxforgestudios.noxspeed.data.prefs.UserSettings
import com.noxforgestudios.noxspeed.gps.GpsEngine
import com.noxforgestudios.noxspeed.gps.GpsSnapshot
import com.noxforgestudios.noxspeed.monetization.BillingManager
import com.noxforgestudios.noxspeed.trip.ActiveTrip
import com.noxforgestudios.noxspeed.trip.TripSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as NoxSpeedApplication
    private val gpsEngine = GpsEngine(application)
    private val tripSession = TripSession()

    val gps: StateFlow<GpsSnapshot> = gpsEngine.state
    val settings: StateFlow<UserSettings> = app.settings.settings.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        UserSettings(),
    )
    val trips: StateFlow<List<TripEntity>> = app.trips.trips.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )
    val accelerationResults = app.trips.accelerationResults.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )
    val billing: StateFlow<BillingManager.State> = app.billing.state

    private val _activeTrip = MutableStateFlow<ActiveTrip?>(null)
    val activeTrip: StateFlow<ActiveTrip?> = _activeTrip.asStateFlow()

    private val _tripCompleted = MutableSharedFlow<TripEntity>(extraBufferCapacity = 1)
    val tripCompleted = _tripCompleted.asSharedFlow()

    data class AccelerationUiState(
        val armed: Boolean = false,
        val running: Boolean = false,
        val targetKmh: Int = 100,
        val startedElapsedMs: Long = 0L,
        val elapsedMs: Long = 0L,
        val resultMs: Long? = null,
    )

    private val _acceleration = MutableStateFlow(AccelerationUiState())
    val acceleration: StateFlow<AccelerationUiState> = _acceleration.asStateFlow()

    private var lastAlertAt = 0L
    private var wasOverAlert = false
    private var autoMoveSince = 0L
    private var autoStopSince = 0L

    val premiumEffective: StateFlow<Boolean> = combine(settings, billing) { s, b ->
        b.premium || s.premiumCached
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    init {
        viewModelScope.launch {
            settings.collect { prefs -> gpsEngine.setFilterMode(prefs.filterMode) }
        }
        viewModelScope.launch {
            gps.collect { snapshot ->
                _activeTrip.value = tripSession.onGps(snapshot)
                processAutoTrip(snapshot)
                processSpeedAlert(snapshot)
                processAcceleration(snapshot)
            }
        }
    }

    fun onForeground() {
        gpsEngine.refreshPermissionState()
        gpsEngine.start()
        app.billing.connect()
        app.billing.refreshPurchases()
    }

    fun onBackground() {
        // Initial release intentionally records only while the app is foregrounded.
        // This avoids requesting background-location/foreground-service privileges.
        gpsEngine.stop()
    }

    fun refreshPermission() {
        gpsEngine.refreshPermissionState()
        gpsEngine.start()
    }

    fun injectDemoSpeed(kmh: Double) = gpsEngine.injectDemoSpeed(kmh / 3.6)

    fun startTrip() {
        tripSession.start()
        _activeTrip.value = tripSession.current()
    }

    fun pauseTrip() {
        tripSession.pause()
        _activeTrip.value = tripSession.current()
    }

    fun resumeTrip() {
        tripSession.resume()
        _activeTrip.value = tripSession.current()
    }

    fun stopTrip() {
        val trip = tripSession.stop() ?: return
        _activeTrip.value = null
        viewModelScope.launch {
            val id = app.trips.insertTrip(trip)
            _tripCompleted.emit(trip.copy(id = id))
        }
    }

    fun deleteTrip(id: Long) = viewModelScope.launch { app.trips.deleteTrip(id) }
    fun toggleFavorite(trip: TripEntity) = viewModelScope.launch { app.trips.updateTrip(trip.copy(favorite = !trip.favorite)) }

    fun completeOnboarding() = viewModelScope.launch { app.settings.setOnboardingComplete(true) }
    fun setUnit(v: SpeedUnit) = viewModelScope.launch { app.settings.setUnit(v) }
    fun setFilter(v: GpsFilterMode) = viewModelScope.launch { app.settings.setFilter(v) }
    fun setDashboard(v: DashboardStyle) = viewModelScope.launch { app.settings.setDashboard(v) }
    fun setHudMirror(v: Boolean) = viewModelScope.launch { app.settings.setHudMirror(v) }
    fun setHudStyle(v: HudStyle) = viewModelScope.launch { app.settings.setHudStyle(v) }
    fun setVehicleMode(v: VehicleMode) = viewModelScope.launch { app.settings.setVehicleMode(v) }
    fun setAccentPreset(v: AccentPreset) = viewModelScope.launch { app.settings.setAccentPreset(v) }
    fun setAutoTrip(v: Boolean) = viewModelScope.launch { app.settings.setAutoTrip(v) }
    fun setKeepScreenOn(v: Boolean) = viewModelScope.launch { app.settings.setKeepScreenOn(v) }
    fun setAlertEnabled(v: Boolean) = viewModelScope.launch { app.settings.setAlertEnabled(v) }
    fun setAlertSpeedKmh(v: Double) = viewModelScope.launch { app.settings.setAlertSpeedKmh(v) }
    fun setSound(v: Boolean) = viewModelScope.launch { app.settings.setSound(v) }
    fun setVibration(v: Boolean) = viewModelScope.launch { app.settings.setVibration(v) }
    fun markReviewRequested() = viewModelScope.launch { app.settings.setReviewRequested(true) }

    fun armAcceleration(targetKmh: Int) {
        _acceleration.value = AccelerationUiState(armed = true, targetKmh = targetKmh)
    }

    fun resetAcceleration() { _acceleration.value = AccelerationUiState(targetKmh = _acceleration.value.targetKmh) }

    private fun processAcceleration(snapshot: GpsSnapshot) {
        val state = _acceleration.value
        if (!state.armed || !snapshot.available) return
        val kmh = snapshot.speedMps * 3.6
        if (!state.running && kmh >= 2.0) {
            _acceleration.value = state.copy(running = true, startedElapsedMs = snapshot.elapsedRealtimeMs, elapsedMs = 0L)
            return
        }
        if (state.running) {
            val elapsed = (snapshot.elapsedRealtimeMs - state.startedElapsedMs).coerceAtLeast(0L)
            if (kmh >= state.targetKmh) {
                _acceleration.value = state.copy(armed = false, running = false, elapsedMs = elapsed, resultMs = elapsed)
                viewModelScope.launch {
                    app.trips.insertAcceleration(
                        AccelerationResultEntity(
                            mode = "0-${state.targetKmh} km/h",
                            targetSpeedMps = state.targetKmh / 3.6,
                            elapsedMs = elapsed,
                            createdAt = System.currentTimeMillis(),
                        )
                    )
                }
            } else {
                _acceleration.value = state.copy(elapsedMs = elapsed)
            }
        }
    }


    private fun processAutoTrip(snapshot: GpsSnapshot) {
        val prefs = settings.value
        if (!prefs.autoTripEnabled || !snapshot.available || snapshot.elapsedRealtimeMs <= 0L) {
            autoMoveSince = 0L
            autoStopSince = 0L
            return
        }
        val kmh = snapshot.speedMps * 3.6
        if (_activeTrip.value == null) {
            autoStopSince = 0L
            if (kmh >= 5.0) {
                if (autoMoveSince == 0L) autoMoveSince = snapshot.elapsedRealtimeMs
                if (snapshot.elapsedRealtimeMs - autoMoveSince >= 8_000L) {
                    startTrip()
                    autoMoveSince = 0L
                }
            } else autoMoveSince = 0L
        } else {
            autoMoveSince = 0L
            if (kmh <= 1.0) {
                if (autoStopSince == 0L) autoStopSince = snapshot.elapsedRealtimeMs
                if (snapshot.elapsedRealtimeMs - autoStopSince >= 300_000L) {
                    stopTrip()
                    autoStopSince = 0L
                }
            } else autoStopSince = 0L
        }
    }

    private fun processSpeedAlert(snapshot: GpsSnapshot) {
        val prefs = settings.value
        if (!prefs.speedAlertEnabled || !snapshot.available) {
            wasOverAlert = false
            return
        }
        val over = snapshot.speedMps * 3.6 >= prefs.alertSpeedKmh
        val now = System.currentTimeMillis()
        if (over && (!wasOverAlert || now - lastAlertAt > 15_000L)) {
            lastAlertAt = now
            if (prefs.soundEnabled) runCatching {
                ToneGenerator(AudioManager.STREAM_NOTIFICATION, 65).apply {
                    startTone(ToneGenerator.TONE_PROP_BEEP, 160)
                    release()
                }
            }
            if (prefs.vibrationEnabled) vibrate()
        }
        wasOverAlert = over
    }

    @Suppress("DEPRECATION")
    private fun vibrate() {
        val vibrator = getApplication<Application>().getSystemService(Vibrator::class.java) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(110, VibrationEffect.DEFAULT_AMPLITUDE))
        } else vibrator.vibrate(110)
    }

    fun eraseAllData() = viewModelScope.launch { app.trips.deleteAll() }

    suspend fun exportJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("schemaVersion", 1)
        root.put("app", "NoxSpeed")
        root.put("exportedAt", System.currentTimeMillis())
        val tripArray = JSONArray()
        app.trips.getTrips().forEach { t ->
            tripArray.put(JSONObject().apply {
                put("name", t.name)
                put("startedAt", t.startedAt)
                put("endedAt", t.endedAt)
                put("durationMs", t.durationMs)
                put("movingDurationMs", t.movingDurationMs)
                put("distanceMeters", t.distanceMeters)
                put("averageSpeedMps", t.averageSpeedMps)
                put("maxSpeedMps", t.maxSpeedMps)
                put("minAltitudeMeters", t.minAltitudeMeters ?: JSONObject.NULL)
                put("maxAltitudeMeters", t.maxAltitudeMeters ?: JSONObject.NULL)
                put("smoothnessScore", t.smoothnessScore)
                put("favorite", t.favorite)
            })
        }
        root.put("trips", tripArray)
        root.toString(2)
    }

    suspend fun importJson(raw: String) = withContext(Dispatchers.IO) {
        val root = JSONObject(raw)
        require(root.optInt("schemaVersion", 0) == 1) { "Unsupported schema" }
        val items = root.getJSONArray("trips")
        for (i in 0 until items.length()) {
            val o = items.getJSONObject(i)
            app.trips.insertTrip(
                TripEntity(
                    name = o.optString("name", "Imported drive"),
                    startedAt = o.getLong("startedAt"),
                    endedAt = o.getLong("endedAt"),
                    durationMs = o.getLong("durationMs"),
                    movingDurationMs = o.optLong("movingDurationMs", o.getLong("durationMs")),
                    distanceMeters = o.getDouble("distanceMeters"),
                    averageSpeedMps = o.getDouble("averageSpeedMps"),
                    maxSpeedMps = o.getDouble("maxSpeedMps"),
                    minAltitudeMeters = o.optDoubleOrNull("minAltitudeMeters"),
                    maxAltitudeMeters = o.optDoubleOrNull("maxAltitudeMeters"),
                    smoothnessScore = o.optInt("smoothnessScore", 100).coerceIn(0, 100),
                    favorite = o.optBoolean("favorite", false),
                )
            )
        }
    }

    fun shareTripText(trip: TripEntity, unit: SpeedUnit): String = buildString {
        appendLine("NoxSpeed · ${formatDate(trip.startedAt)}")
        appendLine(com.noxforgestudios.noxspeed.gps.Units.distanceLabel(trip.distanceMeters, unit))
        appendLine("AVG ${com.noxforgestudios.noxspeed.gps.Units.roundedSpeed(trip.averageSpeedMps, unit)} ${com.noxforgestudios.noxspeed.gps.Units.unitLabel(unit)}")
        append("MAX ${com.noxforgestudios.noxspeed.gps.Units.roundedSpeed(trip.maxSpeedMps, unit)} ${com.noxforgestudios.noxspeed.gps.Units.unitLabel(unit)}")
    }

    private fun formatDate(ms: Long): String = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(ms))
}

private fun JSONObject.optDoubleOrNull(name: String): Double? =
    if (!has(name) || isNull(name)) null else optDouble(name).takeIf { it.isFinite() }
