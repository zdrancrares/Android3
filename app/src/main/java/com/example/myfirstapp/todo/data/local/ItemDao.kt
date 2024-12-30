package com.example.myfirstapp.todo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myfirstapp.todo.data.Trip
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM Trips")
    fun getAll(): Flow<List<Trip>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Trip)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(items: List<Trip>)

    @Update
    suspend fun update(item: Trip): Int

    @Query("DELETE FROM Trips WHERE _id = :id")
    suspend fun deleteById(id: String): Int

    @Query("DELETE FROM Trips")
    suspend fun deleteAll(): Int

    @Query("DELETE FROM Trips where destination= :destination and isSaved = :isSaved")
    suspend fun deleteTripNotSaved(destination: String, isSaved: Boolean)

    @Query("SELECT * FROM Trips where isSaved = :isSaved")
    suspend fun getLocalTrips(isSaved: Boolean): List<Trip>

    @Query("DELETE FROM Trips where isSaved = :isSaved")
    suspend fun deleteNotSaved(isSaved: Boolean): Int

    @Query("SELECT COUNT(*) FROM Trips where isSaved = :isSaved")
    suspend fun getNotSaved(isSaved: Boolean): Int
}
