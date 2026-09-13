package com.noxforgestudios.noxspeed.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TripEntity::class, AccelerationResultEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class NoxSpeedDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun accelerationDao(): AccelerationDao
}
