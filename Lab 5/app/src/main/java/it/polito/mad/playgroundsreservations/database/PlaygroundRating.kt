package it.polito.mad.playgroundsreservations.database

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot

data class PlaygroundRating(
    val id: String,
    val playgroundId: DocumentReference,
    val reservationId: DocumentReference,
    val rating: Float,
    val description: String,
    val fullName: String
)

fun DocumentSnapshot.toPlaygroundRating(): PlaygroundRating {
    val playgroundId = this.get("playgroundId", DocumentReference::class.java)!!
    val reservationId = this.get("reservationId", DocumentReference::class.java)!!
    val rating = this.get("rating", Float::class.java)!!
    val description = this.get("description", String::class.java)
    val fullName = this.get("fullName", String::class.java)!!

    return PlaygroundRating(id, playgroundId, reservationId, rating, description ?: "", fullName)
}