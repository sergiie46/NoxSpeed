package com.noxforgestudios.noxspeed.trip

import android.location.Location
import com.noxforgestudios.noxspeed.data.db.TripEntity
import com.noxforgestudios.noxspeed.gps.GpsSnapshot
import kotlin.math.abs
import kotlin.math.roundToInt

data class ActiveTrip(
    val startedAt: Long,
    val elapsedMs: Long = 0L,
    val movingMs: Long = 0L,
    val distanceMeters: Double = 0.0,
    val maxSpeedMps: Double = 0.0,
    val averageSpeedMps: Double = 0.0,
    val minAltitudeMeters: Double? = null,
    val maxAltitudeMeters: Double? = null,
    val smoothnessScore: Int = 100,
    val paused: Boolean = false,
)

class TripSession {
    private var active: ActiveTrip? = null
    private var previous: GpsSnapshot? = null
    private var lastUpdateElapsedMs = 0L
    private var accelerationPenalty = 0.0

    fun current(): ActiveTrip? = active

    fun start(nowMs: Long = System.currentTimeMillis()) {
        active = ActiveTrip(startedAt = nowMs)
        previous = null
        lastUpdateElapsedMs = 0L
        accelerationPenalty = 0.0
    }

    fun pause() {
        active = active?.copy(paused = true)
        previous = null
        lastUpdateElapsedMs = 0L
    }
    fun resume() {
        active = active?.copy(paused = false)
        previous = null
        lastUpdateElapsedMs = 0L
    }

    fun onGps(snapshot: GpsSnapshot): ActiveTrip? {
        val trip = active ?: return null
        if (trip.paused || !snapshot.available || snapshot.accuracyMeters == null) {
            previous = snapshot
            return trip
        }

        val prev = previous
        val dtMs = if (lastUpdateElapsedMs > 0L) {
            (snapshot.elapsedRealtimeMs - lastUpdateElapsedMs).coerceIn(0L, 5_000L)
        } else 0L
        lastUpdateElapsedMs = snapshot.elapsedRealtimeMs

        var addedDistance = 0.0
        if (prev?.latitude != null && prev.longitude != null && snapshot.latitude != null && snapshot.longitude != null &&
            snapshot.accuracyMeters <= 35f && (prev.accuracyMeters ?: 99f) <= 35f && dtMs > 0L
        ) {
            val result = FloatArray(1)
            Location.distanceBetween(prev.latitude, prev.longitude, snapshot.latitude, snapshot.longitude, result)
            val segment = result[0].toDouble()
            val maxPlausible = (maxOf(snapshot.speedMps, prev.speedMps) + 12.0) * (dtMs / 1000.0) + 10.0
            if (segment in 0.0..maxPlausible) addedDistance = segment
        }

        if (prev != null && dtMs > 150L) {
            val accel = abs(snapshot.speedMps - prev.speedMps) / (dtMs / 1000.0)
            if (accel > 2.8) accelerationPenalty += (accel - 2.8) * 0.06
        }

        val newDistance = trip.distanceMeters + addedDistance
        val movingMs = trip.movingMs + if (snapshot.speedMps > 0.8) dtMs else 0L
        val elapsed = trip.elapsedMs + dtMs
        val avg = if (movingMs > 0L) newDistance / (movingMs / 1000.0) else 0.0
        val altitude = snapshot.altitudeMeters
        val score = (100.0 - accelerationPenalty.coerceAtMost(55.0)).roundToInt().coerceIn(0, 100)

        val updated = trip.copy(
            elapsedMs = elapsed,
            movingMs = movingMs,
            distanceMeters = newDistance,
            maxSpeedMps = maxOf(trip.maxSpeedMps, snapshot.speedMps),
            averageSpeedMps = avg,
            minAltitudeMeters = when {
                altitude == null -> trip.minAltitudeMeters
                trip.minAltitudeMeters == null -> altitude
                else -> minOf(trip.minAltitudeMeters, altitude)
            },
            maxAltitudeMeters = when {
                altitude == null -> trip.maxAltitudeMeters
                trip.maxAltitudeMeters == null -> altitude
                else -> maxOf(trip.maxAltitudeMeters, altitude)
            },
            smoothnessScore = score,
        )
        active = updated
        previous = snapshot
        return updated
    }

    fun stop(nowMs: Long = System.currentTimeMillis()): TripEntity? {
        val trip = active ?: return null
        active = null
        previous = null
        lastUpdateElapsedMs = 0L
        return TripEntity(
            name = "Drive",
            startedAt = trip.startedAt,
            endedAt = nowMs,
            durationMs = trip.elapsedMs,
            movingDurationMs = trip.movingMs,
            distanceMeters = trip.distanceMeters,
            averageSpeedMps = trip.averageSpeedMps,
            maxSpeedMps = trip.maxSpeedMps,
            minAltitudeMeters = trip.minAltitudeMeters,
            maxAltitudeMeters = trip.maxAltitudeMeters,
            smoothnessScore = trip.smoothnessScore,
        )
    }
}
