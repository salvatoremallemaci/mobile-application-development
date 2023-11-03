package it.polito.mad.playgroundsreservations.database

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

data class Reservation(
    val id: String,
    val userId: DocumentReference,
    var playgroundId: DocumentReference,
    var sport: Sport,
    var time: ZonedDateTime,
    var duration: Duration,
    var rentingEquipment: Boolean = false
)

fun DocumentSnapshot.toReservation(): Reservation {
    val userId = this.get("userId", DocumentReference::class.java)!!
    val playgroundId = this.get("playgroundId", DocumentReference::class.java)!!
    val sport = this.get("sport", String::class.java)!!.toSport()
    val time = this.get("time", Timestamp::class.java)!!
        .toDate().toInstant().atZone(ZoneId.systemDefault())
    val duration = Duration.ofHours(this.get("duration", Long::class.java)!!)
    val rentingEquipment = this.get("rentingEquipment", Boolean::class.java)

    return Reservation(id, userId, playgroundId, sport, time, duration, rentingEquipment ?: false)
}