package it.polito.mad.playgroundsreservations.reservations

import android.app.Application
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.polito.mad.playgroundsreservations.Global
import it.polito.mad.playgroundsreservations.database.Invitation
import it.polito.mad.playgroundsreservations.database.InvitationStatus
import it.polito.mad.playgroundsreservations.database.Playground
import it.polito.mad.playgroundsreservations.database.PlaygroundRating
import it.polito.mad.playgroundsreservations.database.Reservation
import it.polito.mad.playgroundsreservations.database.User
import it.polito.mad.playgroundsreservations.database.toPlayground
import it.polito.mad.playgroundsreservations.database.toPlaygroundRating
import it.polito.mad.playgroundsreservations.database.toReservation
import it.polito.mad.playgroundsreservations.database.toUser
import java.util.Date

// should add controls for conflicts previously done by the Room database
// should change value!! with check != null

class ViewModel(application: Application) : AndroidViewModel(application) {
    override fun onCleared() {
        reservations.removeObserver(userReservationsObserver)
        super.onCleared()
    }

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
    val userReservations = MutableLiveData<List<Reservation>>()
    val tutorialShown = MutableLiveData<Boolean?>()
    val nPendingInvitations = MutableLiveData(0)

    private val userReservationsObserver = Observer<List<Reservation>> { reservationsList ->
        reservationsList.filter { r ->
            r.userId.id == Global.userId ||
                    r.invitations
                        .filter { it.invitationStatus == InvitationStatus.ACCEPTED }
                        .map { it.userId }
                        .contains(Global.userId)
        }.also {
            userReservations.value = it
        }
    }

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

        // start observing user reservations
        reservations.observeForever(userReservationsObserver)

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

        // set listener for number of pending notifications
        db.collection(usersCollectionPath)
            .document(Global.userId!!)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.w(TAG, "Failed to read user's notifications.", error)
                    return@addSnapshotListener
                }

                nPendingInvitations.value = (value!!.get("invitations") as? List<*>)?.size ?: 0
            }
    }

    // RESERVATIONS FUNCTIONS
    fun getReservationReference(reservationId: String): DocumentReference {
        return db.collection(reservationsCollectionPath)
            .document(reservationId)
    }

    fun getReservation(reservationId: String, reservationState: MutableState<Reservation?>) {
        db.collection(reservationsCollectionPath)
            .document(reservationId)
            .get()
            .addOnSuccessListener {
                reservationState.value = it.toReservation()
            }
    }

    fun saveReservation(reservation: Reservation): Pair<String, MutableLiveData<Boolean>> {
        // update recently reserved playgrounds
        // remove if already present (avoid duplication)
        db.collection(usersCollectionPath)
            .document(Global.userId!!)
            .update("recentPlaygrounds", FieldValue.arrayRemove(reservation.playgroundId))

        // properly add
        db.collection(usersCollectionPath)
            .document(Global.userId!!)
            .update("recentPlaygrounds", FieldValue.arrayUnion(reservation.playgroundId))

        // save reservation
        val r = hashMapOf(
            "userId" to reservation.userId,
            "userFullName" to reservation.userFullName,
            "playgroundId" to reservation.playgroundId,
            "sport" to reservation.sport.name.lowercase(),
            "time" to Timestamp(Date.from(reservation.time.toInstant())),
            "duration" to reservation.duration.toHours(),
            "rentingEquipment" to reservation.rentingEquipment,
            "invitations" to reservation.invitations.associate {
                Pair(
                    it.userId,
                    hashMapOf(
                        "fullName" to it.fullName,
                        "status" to it.invitationStatus.name.lowercase()
                    )
                )
            }
        )

        val saved = MutableLiveData(false)
        val newReservationReference = db.collection(reservationsCollectionPath).document()

        newReservationReference.set(r).addOnSuccessListener {
            saved.value = true
        }

        return Pair(newReservationReference.id, saved)
    }

    fun updateReservation(reservation: Reservation) {
        val r = hashMapOf(
            "userId" to reservation.userId,
            "userFullName" to reservation.userFullName,
            "playgroundId" to reservation.playgroundId,
            "sport" to reservation.sport.name.lowercase(),
            "time" to Timestamp(Date.from(reservation.time.toInstant())),
            "duration" to reservation.duration.toHours(),
            "rentingEquipment" to reservation.rentingEquipment,
            "invitations" to reservation.invitations.associate {
                Pair(
                    it.userId,
                    hashMapOf(
                        "fullName" to it.fullName,
                        "status" to it.invitationStatus.name.lowercase()
                    )
                )
            }
        )

        db.collection(reservationsCollectionPath)
            .document(reservation.id)
            .set(r)
    }

    fun deleteReservation(reservation: Reservation) {
        val reservationReference = db.collection(reservationsCollectionPath).document(reservation.id)

        // remove invitation notifications for this reservation from users
        for (invitation in reservation.invitations) {
            db.collection(usersCollectionPath)
                .document(invitation.userId)
                .update("invitations", FieldValue.arrayRemove(reservationReference))
        }

        reservationReference.delete()
    }

    // PLAYGROUNDS FUNCTIONS
    fun getPlaygroundReference(playgroundId: String): DocumentReference {
        return db.collection(playgroundsCollectionPath)
            .document(playgroundId)
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

    fun getPlaygrounds(playgroundsState: SnapshotStateList<Playground>) {
        db.collection(playgroundsCollectionPath)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.w(TAG, "Failed to read playgrounds.", error)
                    return@addSnapshotListener
                }

                playgroundsState.clear()

                for (doc in value!!) {
                    val p = doc.toPlayground()
                    playgroundsState.add(p)
                }
            }
    }

    // PLAYGROUNDS RATING FUNCTIONS
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

    fun getRatingsByPlaygroundId(playgroundId: String, ratingsState: SnapshotStateList<PlaygroundRating>) {
        val playgroundReference = db.collection(playgroundsCollectionPath)
            .document(playgroundId)

        db.collection(playgroundsRatingsCollectionPath)
            .whereEqualTo("playgroundId", playgroundReference)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.w(TAG, "Failed to read playground ratings.", error)
                    return@addSnapshotListener
                }

                ratingsState.clear()

                for(doc in value!!)
                    ratingsState.add(doc.toPlaygroundRating())
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

    // USER FUNCTIONS
    fun getUserReference(userId: String): DocumentReference {
        return db.collection(usersCollectionPath)
            .document(userId)
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

    fun getUsers(usersState: SnapshotStateList<User>) {
        db.collection(usersCollectionPath)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.w(TAG, "Failed to read users.", error)
                    return@addSnapshotListener
                }

                usersState.clear()

                for (doc in value!!) {
                    val u = doc.toUser()
                    if (u.id != Global.userId)  // return all users except current
                        usersState.add(u)
                }
            }
    }

    fun getUser(
        userId: String,
        userState: MutableState<User?>,
        friendsState: SnapshotStateList<User>,
        recentlyInvitedState: SnapshotStateList<User>
    ) {
        db.collection(usersCollectionPath)
            .document(userId)
            .get().addOnSuccessListener { userDoc ->
                val u = userDoc.toUser()
                userState.value = u

                friendsState.clear()
                recentlyInvitedState.clear()

                for (doc in u.friends) {
                    doc.get()
                        .addOnSuccessListener {
                            friendsState.add(it.toUser())
                        }
                }

                for (doc in u.recentlyInvited) {
                    doc.get()
                        .addOnSuccessListener {
                            recentlyInvitedState.add(it.toUser())
                        }
                }
            }
    }

    fun getUserPlaygrounds(
        userId: String,
        recentPlaygroundsState: SnapshotStateList<Playground>,
        favoritePlaygroundsState: SnapshotStateList<Playground>,
        stillLoading: MutableState<Boolean>
    ) {
        db.collection(usersCollectionPath)
            .document(userId)
            .get()
            .addOnSuccessListener { userDoc ->
                val u = userDoc.toUser()

                recentPlaygroundsState.clear()
                favoritePlaygroundsState.clear()

                if (u.recentPlaygrounds.isEmpty() && u.myCourts.isEmpty()) {
                    stillLoading.value = false
                    return@addOnSuccessListener
                }

                for (doc in u.recentPlaygrounds) {
                    doc.get()
                        .addOnSuccessListener {
                            recentPlaygroundsState.add(it.toPlayground())

                            // done loading recent playgrounds
                            if (recentPlaygroundsState.size == u.recentPlaygrounds.size && u.myCourts.isEmpty()) {
                                stillLoading.value = false
                            }
                        }
                }

                for (doc in u.myCourts) {
                    doc.get()
                        .addOnSuccessListener {
                            favoritePlaygroundsState.add(it.toPlayground())

                            // done loading favorite playgrounds
                            if (favoritePlaygroundsState.size == u.myCourts.size && u.recentPlaygrounds.isEmpty()) {
                                stillLoading.value = false
                            }
                        }
                }
            }
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
            "mySports" to user.mySports.map { (s, r) -> s.name.lowercase() to r }.toMap(),
            "friends" to user.friends,
            "recentlyInvited" to user.recentlyInvited,
            "invitations" to user.invitations,
            "alreadyShownTutorial" to user.alreadyShownTutorial,
            "recentPlaygrounds" to user.recentPlaygrounds,
            "myCourts" to user.myCourts
        )

        db.collection(usersCollectionPath)
            .document(user.id)
            .set(u)
    }

    fun addFavoritePlayground(playgroundId: String) {
        val playgroundReference = db.collection(playgroundsCollectionPath).document(playgroundId)

        // remove if already present (avoid duplication)
        db.collection(usersCollectionPath)
            .document(Global.userId!!)
            .update("myCourts", FieldValue.arrayRemove(playgroundReference))

        // properly add
        db.collection(usersCollectionPath)
            .document(Global.userId!!)
            .update("myCourts", FieldValue.arrayUnion(playgroundReference))
    }

    fun removeFavoritePlayground(playgroundId: String) {
        val playgroundReference = db.collection(playgroundsCollectionPath).document(playgroundId)

        db.collection(usersCollectionPath)
            .document(Global.userId!!)
            .update("myCourts", FieldValue.arrayRemove(playgroundReference))
    }

    fun befriend(friend: User) {
        val friendReference = db.collection(usersCollectionPath).document(friend.id)

        db.collection(usersCollectionPath)
            .document(Global.userId!!)
            .update("friends", FieldValue.arrayUnion(friendReference))
    }

    fun unfriend(friend: User) {
        val friendReference = db.collection(usersCollectionPath).document(friend.id)

        db.collection(usersCollectionPath)
            .document(Global.userId!!)
            .update("friends", FieldValue.arrayRemove(friendReference))
    }

    // saves only 5 most recent invited users
    fun updateRecentlyInvited(invitations: SnapshotStateList<Invitation>) {
        db.collection(usersCollectionPath)
            .document(Global.userId!!)
            .get()
            .addOnSuccessListener {
                val oldList = it.toUser().recentlyInvited.reversed()

                val newList = invitations.map { i ->
                    db.collection(usersCollectionPath).document(i.userId)
                }.plus(oldList).toSet().take(5)


                db.collection(usersCollectionPath)
                    .document(Global.userId!!)
                    .update("recentlyInvited", newList)
            }
    }

    fun invite(reservationId: String, invitedIds: List<String>) {
        val reservationReference = db.collection(reservationsCollectionPath)
            .document(reservationId)

        invitedIds.forEach {
            // remove previous pending invitations (avoid duplication)
            db.collection(usersCollectionPath)
                .document(it)
                .update("invitations", FieldValue.arrayRemove(reservationReference))

            // invite
            db.collection(usersCollectionPath)
                .document(it)
                .update("invitations", FieldValue.arrayUnion(reservationReference))
        }
    }

    fun getInvitedToReservations(
        invitedToReservationsState: SnapshotStateList<Pair<Reservation, Playground>>,
        stillLoading: MutableState<Boolean>
    ) {
        db.collection(usersCollectionPath)
            .document(Global.userId!!)
            .get()
            .addOnSuccessListener { doc ->
                invitedToReservationsState.clear()

                val invitationsList = doc.toUser().invitations

                if (invitationsList.isEmpty()) {
                    stillLoading.value = false
                    return@addOnSuccessListener
                }

                invitationsList.forEach { reservationReference ->
                    reservationReference
                        .get()
                        .addOnSuccessListener { r ->
                            val reservation = r.toReservation()

                            reservation.playgroundId
                                .get()
                                .addOnSuccessListener { p ->
                                    invitedToReservationsState.add(Pair(reservation, p.toPlayground()))
                                }
                        }
                }
            }
    }

    fun updateInvitationStatus(reservationId: String, newStatus: InvitationStatus) {
        db.collection(reservationsCollectionPath)
            .document(reservationId)
            .update("invitations.${Global.userId}.status", newStatus.name.lowercase())
    }

    fun deleteInvitationNotification(reservationId: String) {
        val reservationReference = db.collection(reservationsCollectionPath).document(reservationId)

        db.collection(usersCollectionPath)
            .document(Global.userId!!)
            .update("invitations", FieldValue.arrayRemove(reservationReference))
    }
}