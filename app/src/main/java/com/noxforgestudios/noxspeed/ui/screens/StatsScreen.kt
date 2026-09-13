package com.noxforgestudios.noxspeed.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxforgestudios.noxspeed.R
import com.noxforgestudios.noxspeed.data.db.AccelerationResultEntity
import com.noxforgestudios.noxspeed.data.db.TripEntity
import com.noxforgestudios.noxspeed.data.prefs.SpeedUnit
import com.noxforgestudios.noxspeed.gps.Units
import com.noxforgestudios.noxspeed.ui.components.BannerAd
import com.noxforgestudios.noxspeed.ui.components.Metric
import com.noxforgestudios.noxspeed.ui.components.MetricRow
import com.noxforgestudios.noxspeed.ui.components.NoxCard
import com.noxforgestudios.noxspeed.ui.formatDuration
import com.noxforgestudios.noxspeed.ui.theme.NoxCyan
import com.noxforgestudios.noxspeed.ui.theme.NoxMuted
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@Composable
fun StatsScreen(
    trips: List<TripEntity>,
    accelerationResults: List<AccelerationResultEntity>,
    unit: SpeedUnit,
    premium: Boolean,
    tripActive: Boolean,
) {
    val totalDistance = trips.sumOf { it.distanceMeters }
    val totalTime = trips.sumOf { it.durationMs }
    val topSpeed = trips.maxOfOrNull { it.maxSpeedMps } ?: 0.0
    val longest = trips.maxByOrNull { it.distanceMeters }
    val avgSmoothness = trips.map { it.smoothnessScore }.takeIf { it.isNotEmpty() }?.average()?.toInt() ?: 100
    val weekStart = ZonedDateTime.now().minusDays(6).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val weekTrips = trips.filter { it.startedAt >= weekStart }
    val bestAcceleration = accelerationResults.minByOrNull { it.elapsedMs }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp),
    ) {
        Text(stringResource(R.string.stats_title), modifier = Modifier.padding(top = 22.dp, bottom = 12.dp), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)

        NoxCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                MetricRow(
                    stringResource(R.string.total_distance) to Units.distanceLabel(totalDistance, unit),
                    stringResource(R.string.total_trips) to trips.size.toString(),
                )
                MetricRow(
                    stringResource(R.string.total_time) to formatDuration(totalTime),
                    stringResource(R.string.top_speed) to "${Units.roundedSpeed(topSpeed, unit)} ${Units.unitLabel(unit)}",
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        NoxCard(Modifier.fillMaxWidth()) {
            Column {
                Text(stringResource(R.string.this_week), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(stringResource(R.string.trips_count, weekTrips.size, Units.distanceLabel(weekTrips.sumOf { it.distanceMeters }, unit)), color = NoxMuted)
                Spacer(Modifier.height(14.dp))
                WeekBars(weekTrips)
            }
        }

        Spacer(Modifier.height(12.dp))
        NoxCard(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(stringResource(R.string.drive_score), color = Color.White, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.score_disclaimer), color = NoxMuted, fontSize = 11.sp)
                }
                Text("$avgSmoothness", color = NoxCyan, fontSize = 38.sp, fontWeight = FontWeight.Black)
            }
        }

        Spacer(Modifier.height(12.dp))
        NoxCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.records), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Metric(stringResource(R.string.longest_trip), longest?.let { Units.distanceLabel(it.distanceMeters, unit) } ?: "—")
                Metric(stringResource(R.string.best_acceleration), bestAcceleration?.let { "${it.mode} · %.2f s".format(it.elapsedMs / 1000.0) } ?: "—")
                Metric(stringResource(R.string.highest_altitude), trips.mapNotNull { it.maxAltitudeMeters }.maxOrNull()?.let { "${it.toInt()} m" } ?: "—")
            }
        }

        Spacer(Modifier.height(12.dp))
        NoxCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.milestones), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                val totalKm = totalDistance / 1000.0
                MilestoneRow(stringResource(R.string.milestone_first), trips.isNotEmpty())
                MilestoneRow(stringResource(R.string.milestone_10), trips.size >= 10)
                MilestoneRow(stringResource(R.string.milestone_100), totalKm >= 100.0)
                MilestoneRow(stringResource(R.string.milestone_500), totalKm >= 500.0)
                MilestoneRow(stringResource(R.string.milestone_1000), totalKm >= 1000.0)
            }
        }

        Spacer(Modifier.height(12.dp))
        BannerAd(visible = !premium && !tripActive)
        Spacer(Modifier.height(105.dp))
    }
}

@Composable
private fun WeekBars(trips: List<TripEntity>) {
    val now = ZonedDateTime.now()
    val distances = (6 downTo 0).map { offset ->
        val date = now.minusDays(offset.toLong()).toLocalDate()
        trips.filter {
            Instant.ofEpochMilli(it.startedAt).atZone(ZoneId.systemDefault()).toLocalDate() == date
        }.sumOf { it.distanceMeters }
    }
    val max = (distances.maxOrNull() ?: 0.0).coerceAtLeast(1.0)
    Canvas(Modifier.fillMaxWidth().height(100.dp)) {
        val step = size.width / 7f
        distances.forEachIndexed { index, meters ->
            val h = (meters / max).toFloat() * size.height * 0.82f
            val x = step * index + step / 2f
            drawLine(
                color = NoxCyan.copy(alpha = if (meters > 0) 0.95f else 0.18f),
                start = Offset(x, size.height),
                end = Offset(x, size.height - h.coerceAtLeast(5f)),
                strokeWidth = step * 0.46f,
                cap = StrokeCap.Round,
            )
        }
    }
}


@Composable
private fun MilestoneRow(label: String, unlocked: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = if (unlocked) Color.White else NoxMuted)
        Text(if (unlocked) "✓" else "○", color = if (unlocked) NoxCyan else NoxMuted, fontWeight = FontWeight.Black)
    }
}
