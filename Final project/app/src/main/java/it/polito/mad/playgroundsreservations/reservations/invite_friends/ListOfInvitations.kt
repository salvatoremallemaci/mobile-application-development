package it.polito.mad.playgroundsreservations.reservations.invite_friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polito.mad.playgroundsreservations.R
import it.polito.mad.playgroundsreservations.database.Invitation
import it.polito.mad.playgroundsreservations.database.InvitationStatus
import it.polito.mad.playgroundsreservations.database.Reservation
import it.polito.mad.playgroundsreservations.reservations.ViewModel
import it.polito.mad.playgroundsreservations.reservations.ui.theme.AcceptedColor
import it.polito.mad.playgroundsreservations.reservations.ui.theme.PendingColor
import it.polito.mad.playgroundsreservations.reservations.ui.theme.PlaygroundsReservationsTheme
import it.polito.mad.playgroundsreservations.reservations.ui.theme.PrimaryColor
import it.polito.mad.playgroundsreservations.reservations.ui.theme.RefusedColor

class ListOfInvitations: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val reservationId = arguments?.getString("reservationId")

        return ComposeView(requireContext()).apply {
            setContent {
                PlaygroundsReservationsTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.White
                    ) {
                        ListOfInvitationsContent(reservationId = reservationId)
                    }
                }
            }
        }
    }
}

@Composable
fun ListOfInvitationsContent(reservationId: String?) {
    val viewModel: ViewModel = viewModel()
    val reservation = remember { mutableStateOf<Reservation?>(null) }

    LaunchedEffect(true) {
        if (reservationId != null) {
            viewModel.getReservation(reservationId = reservationId, reservationState = reservation)
        }
    }

    if (reservation.value != null) {
        Column {
            reservation.value!!.invitations.forEach {
                InvitationBox(invitation = it)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitationBox(invitation: Invitation) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .border(
                BorderStroke(2.dp, PrimaryColor),
                shape = MaterialTheme.shapes.medium
            )
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row {
                    Column {
                        ProfileImage(friendId = invitation.userId, size = ProfileImageSize.SMALL)
                    }

                    Column {
                        Text(
                            text = invitation.fullName,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                    }
                }
            }

            Column {
                val badgeColor = when (invitation.invitationStatus) {
                    InvitationStatus.ACCEPTED -> AcceptedColor
                    InvitationStatus.REFUSED -> RefusedColor
                    InvitationStatus.PENDING -> PendingColor
                }

                val badgeText = when (invitation.invitationStatus) {
                    InvitationStatus.ACCEPTED -> R.string.accepted
                    InvitationStatus.REFUSED -> R.string.refused
                    InvitationStatus.PENDING -> R.string.pending
                }

                Badge(
                    containerColor = badgeColor,
                    contentColor = Color.White
                ) {
                    Text(
                        text = stringResource(id = badgeText).uppercase(),
                        modifier = Modifier.padding(5.dp)
                    )
                }
            }
        }
    }
}