package it.polito.mad.playgroundsreservations.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import it.polito.mad.playgroundsreservations.profile.Gender

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    var firstName: String,
    var lastName: String,
    var bio: String?,
    var gender: Gender?,
    var phone: String?,
    var location: String?,
    var rating: Double = 0.0,
)
