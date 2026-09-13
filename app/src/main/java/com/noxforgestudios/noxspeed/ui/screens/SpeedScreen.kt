package com.noxforgestudios.noxspeed.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxforgestudios.noxspeed.BuildConfig
import com.noxforgestudios.noxspeed.R
import com.noxforgestudios.noxspeed.data.prefs.UserSettings
import com.noxforgestudios.noxspeed.data.prefs.DashboardStyle
import com.noxforgestudios.noxspeed.data.prefs.AccentPreset
import com.noxforgestudios.noxspeed.gps.GpsSnapshot
import com.noxforgestudios.noxspeed.gps.Units
import com.noxforgestudios.noxspeed.trip.ActiveTrip
import com.noxforgestudios.noxspeed.ui.components.MetricRow
import com.noxforgestudios.noxspeed.ui.components.NoxCard
import com.noxforgestudios.noxspeed.ui.components.DashboardGauge
import com.noxforgestudios.noxspeed.ui.components.accentPresetColor
import com.noxforgestudios.noxspeed.ui.formatDuration
import com.noxforgestudios.noxspeed.ui.headingLabel
import com.noxforgestudios.noxspeed.ui.theme.NoxCyan
import com.noxforgestudios.noxspeed.ui.theme.NoxMuted
import com.noxforgestudios.noxspeed.ui.theme.NoxRed

@Composable
fun SpeedScreen(
    gps: GpsSnapshot,
    settings: UserSettings,
    trip: ActiveTrip?,
    premium: Boolean,
    onStartTrip: () -> Unit,
    onStopTrip: () -> Unit,
    onPauseTrip: () -> Unit,
    onResumeTrip: () -> Unit,
    onAcceleration: () -> Unit,
    onPremium: () -> Unit,
    onDemoSpeed: (Double) -> Unit,
) {
    val speed = Units.speedFromMps(gps.speedMps, settings.speedUnit)
    val unit = Units.unitLabel(settings.speedUnit)
    val alertOver = settings.speedAlertEnabled && gps.speedMps * 3.6 >= settings.alertSpeedKmh
    val scale = when {
        speed <= 120 -> 160f
        speed <= 180 -> 220f
        else -> 320f
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (alertOver) NoxRed.copy(alpha = 0.10f) else MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("NOXSPEED", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp, letterSpacing = 1.4.sp)
                Text(stringResource(R.string.brand_subtitle), color = NoxMuted, fontSize = 12.sp)
            }
            Text(if (premium) "PRO" else if (gps.satellitesUsed > 0) "GPS ${gps.satellitesUsed}" else "GPS", color = NoxCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        Spacer(Modifier.height(12.dp))
        when {
            !gps.hasPermission -> Text(stringResource(R.string.gps_permission_required), color = NoxRed, fontWeight = FontWeight.Bold)
            !gps.providerEnabled -> Text(stringResource(R.string.gps_disabled), color = NoxRed, fontWeight = FontWeight.Bold)
            !gps.available -> Text(stringResource(R.string.searching_gps), color = NoxMuted, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            gps.accuracyMeters != null && gps.accuracyMeters > 30f -> Text(stringResource(R.string.poor_gps), color = NoxRed, fontWeight = FontWeight.Bold)
        }

        DashboardGauge(
            style = if (premium || settings.dashboard.ordinal <= DashboardStyle.NIGHT.ordinal) settings.dashboard else DashboardStyle.SPORT,
            speed = speed.toFloat(),
            maxScale = scale,
            unit = unit,
            accentOverride = accentPresetColor(if (premium || settings.accentPreset.ordinal <= AccentPreset.ORANGE.ordinal) settings.accentPreset else AccentPreset.CYAN),
        )

        NoxCard(Modifier.fillMaxWidth()) {
            MetricRow(
                stringResource(R.string.heading) to headingLabel(gps.bearingDegrees),
                stringResource(R.string.altitude) to gps.altitudeMeters?.let { "${it.toInt()} m" }.orEmpty().ifBlank { "—" },
                stringResource(R.string.accuracy) to gps.accuracyMeters?.let { "±${it.toInt()} m" }.orEmpty().ifBlank { "—" },
            )
        }

        Spacer(Modifier.height(12.dp))
        NoxCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                MetricRow(
                    stringResource(R.string.distance) to Units.distanceLabel(trip?.distanceMeters ?: 0.0, settings.speedUnit),
                    stringResource(R.string.avg) to "${Units.roundedSpeed(trip?.averageSpeedMps ?: 0.0, settings.speedUnit)} $unit",
                    stringResource(R.string.max) to "${Units.roundedSpeed(trip?.maxSpeedMps ?: gps.speedMps, settings.speedUnit)} $unit",
                )
                MetricRow(
                    stringResource(R.string.time) to formatDuration(trip?.elapsedMs ?: 0L),
                    stringResource(R.string.drive_score) to (trip?.smoothnessScore?.let { "$it/100" } ?: "—"),
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        if (trip == null) {
            Button(
                onClick = onStartTrip,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NoxCyan, contentColor = Color.Black),
            ) { Text(stringResource(R.string.start_trip), fontWeight = FontWeight.Black) }
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = if (trip.paused) onResumeTrip else onPauseTrip, modifier = Modifier.weight(1f).height(52.dp)) {
                    Text(stringResource(if (trip.paused) R.string.resume_trip else R.string.pause_trip))
                }
                Button(onClick = onStopTrip, modifier = Modifier.weight(1f).height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = NoxRed)) {
                    Text(stringResource(R.string.stop_trip), fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onAcceleration, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.acceleration)) }
            OutlinedButton(onClick = onPremium, modifier = Modifier.weight(1f)) { Text(if (premium) "PRO ✓" else stringResource(R.string.premium)) }
        }

        if (BuildConfig.DEMO_GPS_AVAILABLE) {
            Spacer(Modifier.height(12.dp))
            NoxCard(Modifier.fillMaxWidth()) {
                Column {
                    Text(stringResource(R.string.demo_gps), fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.demo_gps_body), color = NoxMuted, fontSize = 12.sp)
                    Slider(value = (gps.speedMps * 3.6).toFloat().coerceIn(0f, 220f), onValueChange = { onDemoSpeed(it.toDouble()) }, valueRange = 0f..220f)
                }
            }
        }
        Spacer(Modifier.height(110.dp))
    }
}
