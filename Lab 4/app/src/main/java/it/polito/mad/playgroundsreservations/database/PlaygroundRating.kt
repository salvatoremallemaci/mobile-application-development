package it.polito.mad.playgroundsreservations.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playgrounds_ratings")
data class PlaygroundRating(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val playgroundId: Int,
    val reservationId: Int,
    val rating: Int,
    val description: String
)