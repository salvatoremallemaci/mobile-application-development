package it.polito.mad.playgroundsreservations.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Duration
import java.time.ZonedDateTime

enum class Sports {
    TENNIS, BASKETBALL, FOOTBALL, VOLLEYBALL, GOLF
}

@Entity(tableName = "reservations")
data class Reservation (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    var playgroundId: Int,
    var sport: Sports,
    var time: ZonedDateTime,
    var duration: Duration,
    var rentingEquipment: Boolean = false
)