package it.polito.mad.playgroundsreservations.database

enum class InvitationStatus {
    ACCEPTED, REFUSED, PENDING
}

fun String.toInvitationStatus(): InvitationStatus {
    return when (this) {
        "accepted" -> InvitationStatus.ACCEPTED
        "refused" -> InvitationStatus.REFUSED
        "pending" -> InvitationStatus.PENDING
        else -> throw Exception("Non-existing invitation status!")
    }
}

data class Invitation(
    val userId: String,
    val fullName: String,
    val invitationStatus: InvitationStatus
)
