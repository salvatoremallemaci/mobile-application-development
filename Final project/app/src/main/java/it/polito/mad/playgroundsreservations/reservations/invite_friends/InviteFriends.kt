package it.polito.mad.playgroundsreservations.reservations.invite_friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import it.polito.mad.playgroundsreservations.Global
import it.polito.mad.playgroundsreservations.R
import it.polito.mad.playgroundsreservations.database.Reservation
import it.polito.mad.playgroundsreservations.database.User
import it.polito.mad.playgroundsreservations.reservations.LoadingScreen
import it.polito.mad.playgroundsreservations.reservations.ViewModel
import it.polito.mad.playgroundsreservations.reservations.ui.theme.LightGrayColor
import it.polito.mad.playgroundsreservations.reservations.ui.theme.PlaygroundsReservationsTheme
import it.polito.mad.playgroundsreservations.reservations.ui.theme.SecondaryColor

class InviteFriends: Fragment() {
    private val args by navArgs<InviteFriendsArgs>()
    lateinit var reservation: MutableState<Reservation?>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // ACTIVITY TITLE
        activity?.title = activity?.resources?.getString(R.string.invite_friends)

        setHasOptionsMenu(true)

        return ComposeView(requireContext()).apply {
            setContent {
                reservation = remember { mutableStateOf(null) }

                PlaygroundsReservationsTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.White
                    ) {
                        InviteFriendsScreen(args.reservationId, reservation)
                    }
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_invite_friends, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = view?.findNavController()

        // Handle presses on the action bar menu items
        when (item.itemId) {
            R.id.invite_friends -> {
                val viewModel by viewModels<ViewModel>()

                viewModel.updateReservation(reservation.value!!)
                viewModel.updateRecentlyInvited(reservation.value!!.invitations)
                viewModel.invite(reservation.value!!.id, reservation.value!!.invitations.map { it.userId })
                navController?.popBackStack()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

class InviteFriendsFromProfile: Fragment() {
    lateinit var reservation: MutableState<Reservation?>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // ACTIVITY TITLE
        activity?.title = activity?.resources?.getString(R.string.your_friends)

        setHasOptionsMenu(true)

        return ComposeView(requireContext()).apply {
            setContent {
                reservation = remember { mutableStateOf(null) }

                PlaygroundsReservationsTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.White
                    ) {
                        InviteFriendsScreen(null, reservation)
                    }
                }
            }
        }
    }
}

@Composable
fun InviteFriendsScreen(reservationId: String?, reservation: MutableState<Reservation?>) {
    val viewModel: ViewModel = viewModel()

    var stillLoading by remember { mutableStateOf(true) }
    val user = remember { mutableStateOf<User?>(null) }
    val friends = remember { mutableStateListOf<User>() }
    val recentlyInvited = remember { mutableStateListOf<User>() }
    val users = remember { mutableStateListOf<User>() }

    LaunchedEffect(true) {
        if (reservationId != null) {
            viewModel.getReservation(reservationId, reservation)
        }
        viewModel.getUser(Global.userId!!, user, friends, recentlyInvited)
        viewModel.getUsers(users)
    }

    if (stillLoading && (   // prevents from going back into loading
            (reservationId != null && reservation.value == null) ||
            user.value == null ||
            (user.value?.friends?.size != null && user.value!!.friends.isNotEmpty() && friends.isEmpty()) ||
            (user.value?.recentlyInvited?.size != null && user.value!!.recentlyInvited.isNotEmpty() && recentlyInvited.isEmpty()) ||
            users.isEmpty()
        )
    )
        LoadingScreen()
    else {
        stillLoading = false
        InviteFriendsScreenContent(
            reservation = reservation,
            friends = friends,
            recentlyInvited = recentlyInvited,
            users = users
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteFriendsScreenContent(
    reservation: MutableState<Reservation?>,
    friends: SnapshotStateList<User>,
    recentlyInvited: SnapshotStateList<User>,
    users: SnapshotStateList<User>
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(Modifier.fillMaxWidth()) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = searchQuery,
            onValueChange = { searchQuery = it },
            maxLines = 1,
            label = { Text(stringResource(id = R.string.search_users)) },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = LightGrayColor,
                focusedLabelColor = SecondaryColor,
                focusedIndicatorColor = SecondaryColor
            ),
            trailingIcon = {
                IconButton(
                    onClick = { searchQuery = "" },
                    modifier = Modifier.padding(horizontal = 5.dp)
                ) {
                    Icon(Icons.Filled.Clear, contentDescription = "Clear")
                }
            }
        )

        if (reservation.value != null && reservation.value!!.invitations.isNotEmpty()) {
            InvitedBox(reservation = reservation)
        }

        if (searchQuery == "") {
            FriendsList(
                reservation = reservation,
                friends = friends,
                recentlyInvited = recentlyInvited,
                sport = reservation.value?.sport
            )
        } else {
            LazyColumn(Modifier.fillMaxWidth()) {
                items(
                    items = users.filter { friend ->
                        friend.fullName.contains(searchQuery, ignoreCase = true)
                    }, key = { it.id }) { friend ->
                    FriendBox(
                        friend = friend,
                        sport = reservation.value?.sport,
                        reservation = reservation,
                        friends = friends
                    )
                }
            }
        }
    }
}