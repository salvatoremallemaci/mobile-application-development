package it.polito.mad.playgroundsreservations.reservations.invite_friends

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.polito.mad.playgroundsreservations.R
import it.polito.mad.playgroundsreservations.database.Reservation
import it.polito.mad.playgroundsreservations.database.Sport
import it.polito.mad.playgroundsreservations.database.User
import it.polito.mad.playgroundsreservations.reservations.ui.theme.PrimaryColor
import it.polito.mad.playgroundsreservations.reservations.ui.theme.PrimaryVariantColor

@Composable
fun FriendsList(
    reservation: MutableState<Reservation?>,
    friends: SnapshotStateList<User>,
    recentlyInvited: SnapshotStateList<User>,
    sport: Sport?
) {
    var showAllRecentlyInvited by remember { mutableStateOf(false) }
    var showAllFriends by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        if (reservation.value != null) {
            item(key = "recently_invited_title") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.recently_invited),
                        color = PrimaryColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )

                    if (recentlyInvited.size > 2) {
                        TextButton(
                            onClick = { showAllRecentlyInvited = !showAllRecentlyInvited },
                            colors = ButtonDefaults.textButtonColors(contentColor = PrimaryVariantColor),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Text(
                                text = if (showAllRecentlyInvited)
                                    stringResource(id = R.string.see_less)
                                else stringResource(id = R.string.see_all),
                                color = PrimaryVariantColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            if (recentlyInvited.isEmpty()) {
                item(key = "recently_invited_empty") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.empty_recently_invited),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 20.dp, end = 10.dp)
                        )
                    }
                }
            }

            items(
                items = recentlyInvited.take(2),
                key = { "recently_invited_${it.id}" }
            ) { friend ->
                FriendBox(
                    friend = friend,
                    sport = sport,
                    reservation = reservation,
                    friends = friends
                )
            }

            if (recentlyInvited.size > 2) {
                items(
                    items = recentlyInvited.drop(2),
                    key = { "recently_invited_${it.id}" }
                ) { friend ->
                    AnimatedVisibility(
                        visible = showAllRecentlyInvited,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        FriendBox(
                            friend = friend,
                            sport = sport,
                            reservation = reservation,
                            friends = friends
                        )
                    }
                }
            }
        }

        item(key = "your_friends_title") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.your_friends),
                    color = PrimaryColor,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )

                if (friends.size > 2) {
                    TextButton(
                        onClick = { showAllFriends = !showAllFriends },
                        colors = ButtonDefaults.textButtonColors(contentColor = PrimaryVariantColor),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(horizontal = 10.dp)
                    ) {
                        Text(
                            text = if (showAllFriends)
                                stringResource(id = R.string.see_less)
                            else stringResource(id = R.string.see_all),
                            color = PrimaryVariantColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        if (friends.isEmpty()) {
            item(key = "friends_empty") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.empty_friends),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 10.dp)
                    )
                }
            }
        }

        items(
            items = friends.take(2),
            key = { "your_friends_${it.id}" }
        ) { friend ->
            FriendBox(
                friend = friend,
                sport = sport,
                reservation = reservation,
                friends = friends
            )
        }

        if (friends.size > 2) {
            items(
                items = friends.drop(2),
                key = { "your_friends_${it.id}" }
            ) { friend ->
                AnimatedVisibility(
                    visible = showAllFriends,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    FriendBox(
                        friend = friend,
                        sport = sport,
                        reservation = reservation,
                        friends = friends
                    )
                }
            }
        }
    }
}