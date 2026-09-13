package com.noxforgestudios.noxspeed.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Query("SELECT * FROM trips ORDER BY startedAt DESC")
    fun observeAll(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips ORDER BY startedAt DESC")
    suspend fun getAll(): List<TripEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(trip: TripEntity): Long

    @Update
    suspend fun update(trip: TripEntity)

    @Query("DELETE FROM trips WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM trips")
    suspend fun deleteAll()
}

@Dao
interface AccelerationDao {
    @Query("SELECT * FROM acceleration_results ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<AccelerationResultEntity>>

    @Query("SELECT * FROM acceleration_results ORDER BY createdAt DESC")
    suspend fun getAll(): List<AccelerationResultEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: AccelerationResultEntity): Long

    @Query("DELETE FROM acceleration_results")
    suspend fun deleteAll()
}
