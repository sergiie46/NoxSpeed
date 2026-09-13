package com.noxforgestudios.noxspeed.data.db

import kotlinx.coroutines.flow.Flow

class TripRepository(
    private val tripDao: TripDao,
    private val accelerationDao: AccelerationDao,
) {
    val trips: Flow<List<TripEntity>> = tripDao.observeAll()
    val accelerationResults: Flow<List<AccelerationResultEntity>> = accelerationDao.observeAll()

    suspend fun insertTrip(trip: TripEntity): Long = tripDao.insert(trip)
    suspend fun updateTrip(trip: TripEntity) = tripDao.update(trip)
    suspend fun deleteTrip(id: Long) = tripDao.delete(id)
    suspend fun deleteAll() {
        tripDao.deleteAll()
        accelerationDao.deleteAll()
    }

    suspend fun insertAcceleration(result: AccelerationResultEntity) = accelerationDao.insert(result)
    suspend fun getTrips(): List<TripEntity> = tripDao.getAll()
    suspend fun getAccelerationResults(): List<AccelerationResultEntity> = accelerationDao.getAll()
}
