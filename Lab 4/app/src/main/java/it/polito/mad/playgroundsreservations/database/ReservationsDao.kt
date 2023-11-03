package it.polito.mad.playgroundsreservations.database

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.Companion.ABORT

@Dao
interface ReservationsDao {
    @Query("SELECT * FROM reservations")
    fun getAllReservations(): LiveData<List<Reservation>>

    @Query("SELECT * FROM reservations WHERE sport = :sport")
    fun getReservationsBySport(sport: Sports): LiveData<List<Reservation>>

    @Query("SELECT * FROM reservations WHERE playgroundID = :playgroundId")
    fun getReservationsByPlayground(playgroundId: Int): LiveData<List<Reservation>>

    @Query("SELECT * FROM reservations WHERE userId = :userId ORDER BY time, duration")
    fun getUserReservations(userId: Int): LiveData<List<Reservation>>

    @Query("SELECT * FROM reservations r, playgrounds p WHERE p.sport = :sport AND r.playgroundId = p.id ORDER BY r.time, r.duration")
    fun getReservedPlaygroundsBySport(sport: Sports): LiveData<Map<Reservation, Playground>>

    @Insert(onConflict = ABORT)
    suspend fun save(reservation: Reservation)

    @Update
    suspend fun update(reservation: Reservation)

    @Delete
    suspend fun delete(reservation: Reservation)
}