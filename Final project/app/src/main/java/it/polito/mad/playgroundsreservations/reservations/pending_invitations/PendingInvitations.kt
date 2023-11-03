package it.polito.mad.playgroundsreservations.reservations.pending_invitations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polito.mad.playgroundsreservations.R
import it.polito.mad.playgroundsreservations.database.InvitationStatus
import it.polito.mad.playgroundsreservations.database.Playground
import it.polito.mad.playgroundsreservations.database.Reservation
import it.polito.mad.playgroundsreservations.reservations.LoadingScreen
import it.polito.mad.playgroundsreservations.reservations.ViewModel
import it.polito.mad.playgroundsreservations.reservations.ui.theme.PlaygroundsReservationsTheme
import it.polito.mad.playgroundsreservations.reservations.ui.theme.PrimaryColor

class PendingInvitations: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // ACTIVITY TITLE
        activity?.title = activity?.resources?.getString(R.string.pending_invitations)

        return ComposeView(requireContext()).apply {
            setContent {
                PlaygroundsReservationsTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.White
                    ) {
                        PendingInvitationsScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun PendingInvitationsScreen() {
    val viewModel: ViewModel = viewModel()

    val stillLoading = remember { mutableStateOf(true) }
    val invitedToReservations = remember { mutableStateListOf<Pair<Reservation, Playground>>() }
    val invitedToReservationsStatuses = remember { mutableStateListOf<Pair<String, InvitationStatus>>() }

    LaunchedEffect(true) {
        viewModel.getInvitedToReservations(invitedToReservations, stillLoading)
    }

    val dealWithInvitation: (String, InvitationStatus) -> Unit = { reservationId: String, newStatus: InvitationStatus ->
        invitedToReservationsStatuses.removeIf {
            it.first == reservationId
        }
        invitedToReservationsStatuses.add(Pair(reservationId, newStatus))

        viewModel.updateInvitationStatus(reservationId, newStatus)
        viewModel.deleteInvitationNotification(reservationId)
        // commented to leave the invitation displayed and only change the buttons
        //invitedToReservations.removeIf { it.first.id == reservationId }
    }

    if (stillLoading.value && invitedToReservations.isEmpty()) {
        LoadingScreen()
    } else if (invitedToReservations.isEmpty()) {
        ZeroPendingInvitations()
    } else {
        stillLoading.value = false
        invitedToReservations.forEach {
            invitedToReservationsStatuses.add(Pair(it.first.id, InvitationStatus.PENDING))
        }

        PendingInvitationsScreenContent(
            invitedToReservations = invitedToReservations,
            invitedToReservationsStatuses,
            dealWithInvitation = dealWithInvitation
        )
    }
}

@Composable
fun PendingInvitationsScreenContent(
    invitedToReservations: SnapshotStateList<Pair<Reservation, Playground>>,
    invitedToReservationsState: SnapshotStateList<Pair<String, InvitationStatus>>,
    dealWithInvitation: (String, InvitationStatus) -> Unit
) {
    LazyColumn {
        items(
            items = invitedToReservations,
            key = { it.first.id }
        ) { invitedToReservation ->
            invitedToReservationsState.find {
                it.first == invitedToReservation.first.id
            }?.let { invitedToReservationState ->
                PendingInvitationBox(
                    invitedToReservation,
                    invitedToReservationState.second,
                    dealWithInvitation
                )
            }
        }
    }
}

@Composable
fun ZeroPendingInvitations() {
    Column (
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Image(
            painter = painterResource(id = R.drawable.notification_bell_off),
            contentDescription = "Zero notifications",
            alignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .size(100.dp)
        )
        Text(
            text = stringResource(id = R.string.zero_invitations),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = PrimaryColor,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 40.dp)
        )
        Spacer(modifier = Modifier.weight(3f))
    }
}