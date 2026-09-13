package com.noxforgestudios.noxspeed.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val startedAt: Long,
    val endedAt: Long,
    val durationMs: Long,
    val movingDurationMs: Long,
    val distanceMeters: Double,
    val averageSpeedMps: Double,
    val maxSpeedMps: Double,
    val minAltitudeMeters: Double?,
    val maxAltitudeMeters: Double?,
    val smoothnessScore: Int,
    val favorite: Boolean = false,
)

@Entity(tableName = "acceleration_results")
data class AccelerationResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mode: String,
    val targetSpeedMps: Double,
    val elapsedMs: Long,
    val createdAt: Long,
)
