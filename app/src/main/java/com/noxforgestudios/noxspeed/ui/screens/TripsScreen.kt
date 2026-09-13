package com.noxforgestudios.noxspeed.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxforgestudios.noxspeed.R
import com.noxforgestudios.noxspeed.data.db.TripEntity
import com.noxforgestudios.noxspeed.data.prefs.SpeedUnit
import com.noxforgestudios.noxspeed.gps.Units
import com.noxforgestudios.noxspeed.ui.components.BannerAd
import com.noxforgestudios.noxspeed.ui.components.MetricRow
import com.noxforgestudios.noxspeed.ui.components.NoxCard
import com.noxforgestudios.noxspeed.ui.formatDuration
import com.noxforgestudios.noxspeed.ui.theme.NoxCyan
import com.noxforgestudios.noxspeed.ui.theme.NoxMuted
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun TripsScreen(
    trips: List<TripEntity>,
    unit: SpeedUnit,
    premium: Boolean,
    tripActive: Boolean,
    onDelete: (Long) -> Unit,
    onFavorite: (TripEntity) -> Unit,
    onShare: (TripEntity) -> Unit,
) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 18.dp)) {
        Text(
            text = stringResource(R.string.trips_title),
            modifier = Modifier.padding(top = 22.dp, bottom = 12.dp),
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
        )
        if (trips.isEmpty()) {
            NoxCard(Modifier.fillMaxWidth()) {
                Column {
                    Text(stringResource(R.string.no_trips), color = NoxCyan, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(stringResource(R.string.no_trips_body), color = NoxMuted)
                }
            }
            Spacer(Modifier.weight(1f))
            BannerAd(visible = !premium && !tripActive)
            Spacer(Modifier.height(90.dp))
            return@Column
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(trips, key = { it.id }) { trip ->
                TripCard(trip, unit, onDelete, onFavorite, onShare)
            }
            item {
                Spacer(Modifier.height(8.dp))
                BannerAd(visible = !premium && !tripActive)
                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun TripCard(
    trip: TripEntity,
    unit: SpeedUnit,
    onDelete: (Long) -> Unit,
    onFavorite: (TripEntity) -> Unit,
    onShare: (TripEntity) -> Unit,
) {
    NoxCard(Modifier.fillMaxWidth()) {
        Column {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(
                        text = if (trip.favorite) "★ ${trip.name}" else trip.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                    )
                    Text(formatTripDate(trip.startedAt), color = NoxMuted, fontSize = 12.sp)
                }
                Text("${trip.smoothnessScore}/100", color = NoxCyan, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(14.dp))
            MetricRow(
                stringResource(R.string.distance) to Units.distanceLabel(trip.distanceMeters, unit),
                stringResource(R.string.avg) to "${Units.roundedSpeed(trip.averageSpeedMps, unit)} ${Units.unitLabel(unit)}",
                stringResource(R.string.max) to "${Units.roundedSpeed(trip.maxSpeedMps, unit)} ${Units.unitLabel(unit)}",
            )
            Spacer(Modifier.height(10.dp))
            Text(formatDuration(trip.durationMs), color = NoxMuted, fontSize = 13.sp)
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onFavorite(trip) }, modifier = Modifier.weight(1f)) { Text(if (trip.favorite) "★" else "☆") }
                OutlinedButton(onClick = { onShare(trip) }, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.share)) }
                OutlinedButton(onClick = { onDelete(trip.id) }, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.delete)) }
            }
        }
    }
}

private val tripDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM · HH:mm")
    .withZone(ZoneId.systemDefault())

private fun formatTripDate(ms: Long): String = tripDateFormatter.format(Instant.ofEpochMilli(ms))
