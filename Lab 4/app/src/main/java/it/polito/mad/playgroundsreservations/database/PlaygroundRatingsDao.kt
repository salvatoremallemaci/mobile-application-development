package it.polito.mad.playgroundsreservations.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PlaygroundRatingsDao {
    @Query("SELECT * FROM playgrounds_ratings WHERE id = :id")
    suspend fun getRating(id: Int): PlaygroundRating

    @Query("SELECT * FROM playgrounds_ratings WHERE reservationId = :reservationId")
    fun getRatingByReservation(reservationId: Int): LiveData<PlaygroundRating?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun save(rating: PlaygroundRating)
}