package com.noxforgestudios.noxspeed.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "noxspeed_settings")

enum class SpeedUnit { KMH, MPH, KNOTS }
enum class GpsFilterMode { FAST, BALANCED, SMOOTH }
enum class HudStyle { BASIC, NIGHT, SPORT, MINIMAL, CYBER, CLASSIC }
enum class VehicleMode { CAR, MOTORCYCLE, BIKE, OTHER }
enum class AccentPreset { CYAN, RED, GREEN, ORANGE, PURPLE, BLUE, YELLOW, WHITE }
enum class DashboardStyle {
    DIGITAL, CLASSIC, SPORT, NIGHT, TRACK, CYBER, NEON, OLED,
    PERFORMANCE, RETRO, RALLY, SUPERCAR, MINIMAL_PRO
}

data class UserSettings(
    val onboardingComplete: Boolean = false,
    val speedUnit: SpeedUnit = SpeedUnit.KMH,
    val filterMode: GpsFilterMode = GpsFilterMode.BALANCED,
    val dashboard: DashboardStyle = DashboardStyle.SPORT,
    val hudMirror: Boolean = true,
    val hudStyle: HudStyle = HudStyle.BASIC,
    val vehicleMode: VehicleMode = VehicleMode.CAR,
    val accentPreset: AccentPreset = AccentPreset.CYAN,
    val autoTripEnabled: Boolean = false,
    val keepScreenOn: Boolean = true,
    val speedAlertEnabled: Boolean = false,
    val alertSpeedKmh: Double = 120.0,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val premiumCached: Boolean = false,
    val reviewRequested: Boolean = false,
)

class SettingsRepository(private val context: Context) {
    private object Keys {
        val onboarding = booleanPreferencesKey("onboarding")
        val unit = stringPreferencesKey("unit")
        val filter = stringPreferencesKey("filter")
        val dashboard = stringPreferencesKey("dashboard")
        val hudMirror = booleanPreferencesKey("hud_mirror")
        val hudStyle = stringPreferencesKey("hud_style")
        val vehicleMode = stringPreferencesKey("vehicle_mode")
        val accentPreset = stringPreferencesKey("accent_preset")
        val autoTrip = booleanPreferencesKey("auto_trip")
        val keepScreenOn = booleanPreferencesKey("keep_screen_on")
        val alertEnabled = booleanPreferencesKey("alert_enabled")
        val alertSpeed = doublePreferencesKey("alert_speed_kmh")
        val sound = booleanPreferencesKey("sound")
        val vibration = booleanPreferencesKey("vibration")
        val premiumCached = booleanPreferencesKey("premium_cached")
        val reviewRequested = booleanPreferencesKey("review_requested")
    }

    val settings: Flow<UserSettings> = context.dataStore.data.map { p ->
        UserSettings(
            onboardingComplete = p[Keys.onboarding] ?: false,
            speedUnit = enumValueOrDefault(p[Keys.unit], SpeedUnit.KMH),
            filterMode = enumValueOrDefault(p[Keys.filter], GpsFilterMode.BALANCED),
            dashboard = enumValueOrDefault(p[Keys.dashboard], DashboardStyle.SPORT),
            hudMirror = p[Keys.hudMirror] ?: true,
            hudStyle = enumValueOrDefault(p[Keys.hudStyle], HudStyle.BASIC),
            vehicleMode = enumValueOrDefault(p[Keys.vehicleMode], VehicleMode.CAR),
            accentPreset = enumValueOrDefault(p[Keys.accentPreset], AccentPreset.CYAN),
            autoTripEnabled = p[Keys.autoTrip] ?: false,
            keepScreenOn = p[Keys.keepScreenOn] ?: true,
            speedAlertEnabled = p[Keys.alertEnabled] ?: false,
            alertSpeedKmh = p[Keys.alertSpeed] ?: 120.0,
            soundEnabled = p[Keys.sound] ?: true,
            vibrationEnabled = p[Keys.vibration] ?: true,
            premiumCached = p[Keys.premiumCached] ?: false,
            reviewRequested = p[Keys.reviewRequested] ?: false,
        )
    }

    suspend fun setOnboardingComplete(value: Boolean) = context.dataStore.edit { it[Keys.onboarding] = value }
    suspend fun setUnit(value: SpeedUnit) = context.dataStore.edit { it[Keys.unit] = value.name }
    suspend fun setFilter(value: GpsFilterMode) = context.dataStore.edit { it[Keys.filter] = value.name }
    suspend fun setDashboard(value: DashboardStyle) = context.dataStore.edit { it[Keys.dashboard] = value.name }
    suspend fun setHudMirror(value: Boolean) = context.dataStore.edit { it[Keys.hudMirror] = value }
    suspend fun setHudStyle(value: HudStyle) = context.dataStore.edit { it[Keys.hudStyle] = value.name }
    suspend fun setVehicleMode(value: VehicleMode) = context.dataStore.edit { it[Keys.vehicleMode] = value.name }
    suspend fun setAccentPreset(value: AccentPreset) = context.dataStore.edit { it[Keys.accentPreset] = value.name }
    suspend fun setAutoTrip(value: Boolean) = context.dataStore.edit { it[Keys.autoTrip] = value }
    suspend fun setKeepScreenOn(value: Boolean) = context.dataStore.edit { it[Keys.keepScreenOn] = value }
    suspend fun setAlertEnabled(value: Boolean) = context.dataStore.edit { it[Keys.alertEnabled] = value }
    suspend fun setAlertSpeedKmh(value: Double) = context.dataStore.edit { it[Keys.alertSpeed] = value.coerceIn(10.0, 400.0) }
    suspend fun setSound(value: Boolean) = context.dataStore.edit { it[Keys.sound] = value }
    suspend fun setVibration(value: Boolean) = context.dataStore.edit { it[Keys.vibration] = value }
    suspend fun setPremiumCached(value: Boolean) = context.dataStore.edit { it[Keys.premiumCached] = value }
    suspend fun setReviewRequested(value: Boolean) = context.dataStore.edit { it[Keys.reviewRequested] = value }

    private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String?, fallback: T): T =
        runCatching { enumValueOf<T>(value.orEmpty()) }.getOrDefault(fallback)
}
