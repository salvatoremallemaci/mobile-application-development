package it.polito.mad.playgroundsreservations.reservations

import android.app.Application
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.polito.mad.playgroundsreservations.Global
import it.polito.mad.playgroundsreservations.database.Playground
import it.polito.mad.playgroundsreservations.database.PlaygroundRating
import it.polito.mad.playgroundsreservations.database.Reservation
import it.polito.mad.playgroundsreservations.database.Sport
import it.polito.mad.playgroundsreservations.database.User
import it.polito.mad.playgroundsreservations.database.toPlayground
import it.polito.mad.playgroundsreservations.database.toPlaygroundRating
import it.polito.mad.playgroundsreservations.database.toReservation
import it.polito.mad.playgroundsreservations.database.toUser
import java.util.Date

// AGGIUNGERE controlli di conflitti fatti dal db in precedenza
// CAMBIARE value!! con controllo != null

class ViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val TAG = "VIEW_MODEL"
        const val usersCollectionPath = "users"
        const val playgroundsCollectionPath = "playgrounds"
        const val reservationsCollectionPath = "reservations"
        const val playgroundsRatingsCollectionPath = "playgrounds_ratings"
    }

    private val db = Firebase.firestore

    val playgrounds = MutableLiveData<List<Playground>>()
    val reservations = MutableLiveData<List<Reservation>>()
    val tutorialShown = MutableLiveData<Boolean?>()

    init {
        // set listener for playgrounds
        db.collection(playgroundsCollectionPath)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.w(TAG, "Failed to read playgrounds.", error)
                    playgrounds.value = emptyList()
                    return@addSnapshotListener
                }

                val playgroundsList = mutableListOf<Playground>()
                for (doc in value!!) {
                    val playground = doc.toPlayground()
                    playgroundsList.add(playground)
                }

                playgrounds.value = playgroundsList
            }

        // set listener for reservations
        db.collection(reservationsCollectionPath)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.w(TAG, "Failed to read reservations.", error)
                    reservations.value = emptyList()
                    return@addSnapshotListener
                }

                val reservationsList = mutableListOf<Reservation>()
                for (doc in value!!) {
                    val reservation = doc.toReservation()
                    reservationsList.add(reservation)
                }

                reservations.value = reservationsList
            }

        // set listener for tutorialShown
        db.collection(usersCollectionPath)
            .document(Global.userId!!)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.w(TAG, "Failed to read the tutorial data.", error)
                    tutorialShown.value = false
                    return@addSnapshotListener
                }

                tutorialShown.value = value!!.getBoolean("alreadyShownTutorial")
            }
    }

    fun getUserReference(userId: String): DocumentReference {
        return db.collection(usersCollectionPath)
            .document(userId)
    }

    fun getPlaygroundReference(playgroundId: String): DocumentReference {
        return db.collection(playgroundsCollectionPath)
            .document(playgroundId)
    }

    fun getReservationReference(reservationId: String): DocumentReference {
        return db.collection(reservationsCollectionPath)
            .document(reservationId)
    }

    fun getReservedPlaygrounds(sport: Sport): LiveData<Map<Reservation, Playground>> {
        val reservedPlaygrounds = MutableLiveData<Map<Reservation, Playground>>()

        db.collection(reservationsCollectionPath)
            .whereEqualTo("sport", sport.name.lowercase())
            .orderBy("time")
            .orderBy("duration")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.w(TAG, "Failed to read reserved playgrounds.", error)
                    reservedPlaygrounds.value = emptyMap()
                    return@addSnapshotListener
                }

                val reservedPlaygroundsMap = mutableMapOf<Reservation, Playground>()
                for (doc in value!!) {
                    val reservation = doc.toReservation()

                    reservation.playgroundId.get()
                        .addOnSuccessListener {
                            val playground = it.toPlayground()
                            reservedPlaygroundsMap[reservation] = playground
                            reservedPlaygrounds.value = reservedPlaygroundsMap
                        }
                }
            }

        return reservedPlaygrounds
    }

    fun getUserReservations(userId: String): LiveData<List<Reservation>> {
        val userReservations = MutableLiveData<List<Reservation>>()

        val userReference = db.collection(usersCollectionPath)
            .document(userId)

        db.collection(reservationsCollectionPath)
            .whereEqualTo("userId", userReference)
            .orderBy("time")
            .orderBy("duration")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.w(TAG, "Failed to read user reservations.", error)
                    userReservations.value = emptyList()
                    return@addSnapshotListener
                }

                val userReservationsList = mutableListOf<Reservation>()
                for (doc in value!!) {
                    val reservation = doc.toReservation()
                    userReservationsList.add(reservation)
                }

                userReservations.value = userReservationsList
            }

        return userReservations
    }

    fun getPlayground(playgroundId: String, playgroundState: MutableState<Playground?>) {
        db.collection(playgroundsCollectionPath)
            .document(playgroundId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.w(TAG, "Failed to read playground.", error)
                    playgroundState.value = null
                    return@addSnapshotListener
                }

                playgroundState.value = value!!.toPlayground()
            }
    }

    fun getRatingByReservation(reservationId: String): LiveData<PlaygroundRating?> {
        val playgroundRating = MutableLiveData<PlaygroundRating?>()

        val reservationReference = db.collection(reservationsCollectionPath)
            .document(reservationId)

        db.collection(playgroundsRatingsCollectionPath)
            .whereEqualTo("reservationId", reservationReference)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.w(TAG, "Failed to read user reservations.", error)
                    playgroundRating.value = null
                    return@addSnapshotListener
                }

                if (value?.isEmpty == false)
                    playgroundRating.value = value.first().toPlaygroundRating()
                else
                    playgroundRating.value = null
            }

        return playgroundRating
    }

    fun saveReservation(reservation: Reservation) {
        val r = hashMapOf(
            "userId" to reservation.userId,
            "playgroundId" to reservation.playgroundId,
            "sport" to reservation.sport.name.lowercase(),
            "time" to Timestamp(Date.from(reservation.time.toInstant())),
            "duration" to reservation.duration.toHours(),
            "rentingEquipment" to reservation.rentingEquipment
        )

        db.collection(reservationsCollectionPath).add(r)
    }

    fun updateReservation(reservation: Reservation) {
        val r = hashMapOf(
            "id" to reservation.id,
            "userId" to reservation.userId,
            "playgroundId" to reservation.playgroundId,
            "sport" to reservation.sport.name.lowercase(),
            "time" to Timestamp(Date.from(reservation.time.toInstant())),
            "duration" to reservation.duration.toHours(),
            "rentingEquipment" to reservation.rentingEquipment
        )

        db.collection(reservationsCollectionPath)
            .document(reservation.id)
            .set(r)
    }

    fun deleteReservation(reservation: Reservation) {
        db.collection(reservationsCollectionPath)
            .document(reservation.id)
            .delete()
    }

    fun savePlaygroundRating(playgroundRating: PlaygroundRating) {
        val pr = hashMapOf(
            "playgroundId" to playgroundRating.playgroundId,
            "reservationId" to playgroundRating.reservationId,
            "rating" to playgroundRating.rating,
            "description" to playgroundRating.description,
            "fullName" to playgroundRating.fullName
        )

        db.collection(playgroundsRatingsCollectionPath).add(pr)
    }

    fun getRatingsByPlaygroundId(playgroundId: String,ratingsState: MutableState<List<PlaygroundRating>>) {
        val playgroundReference = db.collection(playgroundsCollectionPath)
            .document(playgroundId)

        db.collection(playgroundsRatingsCollectionPath)
            .whereEqualTo("playgroundId", playgroundReference)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.w(TAG, "Failed to read playground ratings.", error)
                    return@addSnapshotListener
                }

                val list = mutableListOf<PlaygroundRating>()
                for(doc in value!!)
                    list.add(doc.toPlaygroundRating())

                ratingsState.value = list
            }
    }

    fun createUserIfNotExists(id: String, displayName: String?) {
        db.collection(usersCollectionPath)
            .document(id)
            .get()
            .addOnSuccessListener {
                if (!it.exists()) {
                    val newUser = hashMapOf(
                        "fullName" to (displayName ?: "")
                    )

                    db.collection(usersCollectionPath)
                        .document(id)
                        .set(newUser)
                }
            }
    }

    fun getRatingsByPlaygroundIdFragment(playgroundId: String): LiveData<List<PlaygroundRating?>> {

        val ratingPlaygrounds = MutableLiveData<List<PlaygroundRating?>>()

        val playgroundReference = db.collection(playgroundsCollectionPath)
            .document(playgroundId)

        db.collection(playgroundsRatingsCollectionPath)
            .whereEqualTo("playgroundId", playgroundReference)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.w(TAG, "Failed to read playground rating.", error)
                    return@addSnapshotListener
                }
                val listOfRatings=mutableListOf<PlaygroundRating>()
                for(doc in value!!) {
                    val rating = doc.toPlaygroundRating()
                    listOfRatings.add(rating)
                }
                ratingPlaygrounds.value = listOfRatings
            }
        return ratingPlaygrounds
    }

    fun getUserInfo(userId: String): LiveData<User?> {
        val userInfo = MutableLiveData<User?>()

        db.collection(usersCollectionPath)
            .document(userId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.w(TAG, "Failed to read user info.", error)
                    userInfo.value = null
                    return@addSnapshotListener
                }

                userInfo.value = value!!.toUser()
            }

        return userInfo
    }

    fun tutorialHasBeenShown() {
        db.collection(usersCollectionPath)
            .document(Global.userId!!)
            .update("alreadyShownTutorial", true)
    }

    fun updateUserInfo(user: User) {
        val u = hashMapOf(
            "fullName" to user.fullName,
            "bio" to user.bio,
            "dateOfBirth" to user.dateOfBirth,
            "gender" to user.gender?.name?.lowercase(),
            "phone" to user.phone,
            "location" to user.location,
            "rating" to user.rating,
            "selectedSports" to user.selectedSports.map { it.name.lowercase() } .toList(),
            "alreadyShownTutorial" to user.alreadyShownTutorial
        )

        db.collection(usersCollectionPath)
            .document(user.id)
            .set(u)
    }
}