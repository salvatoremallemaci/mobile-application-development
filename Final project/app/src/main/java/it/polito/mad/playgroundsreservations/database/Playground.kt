package it.polito.mad.playgroundsreservations.database

import com.google.firebase.firestore.DocumentSnapshot

data class Playground(
    val id: String,
    val name: String,
    val address: String,
    val sport: Sport,
    val pricePerHour: Int,
    val region: String,
    val city: String,
    val maxPlayers: Int
)

fun DocumentSnapshot.toPlayground(): Playground {
    val name = this.get("name", String::class.java)
    val address = this.get("address", String::class.java)
    val sport = this.get("sport", String::class.java)!!.toSport()
    val pricePerHour = this.get("pricePerHour", Int::class.java)
    val region = this.get("region", String::class.java)
    val city = this.get("city", String::class.java)
    val maxPlayers=this.get("maxPlayers",Int::class.java)

    return Playground(id,name ?: "", address ?: "", sport,pricePerHour ?: 0, region ?: "", city ?: "",maxPlayers ?: 0)
}