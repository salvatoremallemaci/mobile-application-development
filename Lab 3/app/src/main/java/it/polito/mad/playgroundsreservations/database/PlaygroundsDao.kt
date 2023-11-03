package it.polito.mad.playgroundsreservations.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.OnConflictStrategy.Companion.ABORT
import androidx.room.Query

@Dao
interface PlaygroundsDao {
    @Query("SELECT * FROM playgrounds")
    fun getAllPlaygrounds(): LiveData<List<Playground>>

    @Query("SELECT * FROM playgrounds WHERE sport = :sport")
    fun getPlaygroundsBySport(sport: Sports): LiveData<List<Playground>>

    @Insert(onConflict = ABORT)
    suspend fun save(playground: Playground)
}