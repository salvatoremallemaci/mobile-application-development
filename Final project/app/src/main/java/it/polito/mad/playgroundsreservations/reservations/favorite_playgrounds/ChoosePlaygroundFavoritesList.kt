package it.polito.mad.playgroundsreservations.reservations.favorite_playgrounds

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.polito.mad.playgroundsreservations.R
import it.polito.mad.playgroundsreservations.database.Playground
import it.polito.mad.playgroundsreservations.reservations.ui.theme.PrimaryColor
import it.polito.mad.playgroundsreservations.reservations.ui.theme.PrimaryVariantColor

@Composable
fun ChoosePlaygroundFavoritesList(
    canChoosePlayground: Boolean,
    choosePlayground: (HashMap<String, String>) -> Unit,
    seeRatings: (String) -> Unit,
    recentPlaygrounds: SnapshotStateList<Playground>,
    favoritePlaygrounds: SnapshotStateList<Playground>
) {
    var showAllRecentPlaygrounds by rememberSaveable { mutableStateOf(false) }
    var showAllFavoritePlaygrounds by rememberSaveable { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        if (canChoosePlayground) {
            item(key = "recent_playgrounds_title") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.recently_reserved),
                        color = PrimaryColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )

                    if (recentPlaygrounds.size > 2) {
                        TextButton(
                            onClick = { showAllRecentPlaygrounds = !showAllRecentPlaygrounds },
                            colors = ButtonDefaults.textButtonColors(contentColor = PrimaryVariantColor),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Text(
                                text = if (showAllRecentPlaygrounds)
                                    stringResource(id = R.string.see_less)
                                else stringResource(id = R.string.see_all),
                                color = PrimaryVariantColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            if (recentPlaygrounds.isEmpty()) {
                item(key = "recent_playgrounds_empty") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.empty_recent_playgrounds),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 20.dp, end = 10.dp)
                        )
                    }
                }
            }

            items(
                items = recentPlaygrounds.take(2),
                key = { "recent_playgrounds_${it.id}" }
            ) { playground ->
                ChoosePlaygroundBox(
                    canChoosePlayground = true,
                    choosePlayground = choosePlayground,
                    seeRatings = seeRatings,
                    playground = playground,
                    favoritePlaygrounds = favoritePlaygrounds
                )
            }

            if (recentPlaygrounds.size > 2) {
                items(
                    items = recentPlaygrounds.drop(2),
                    key = { "recent_playgrounds_${it.id}" }
                ) { playground ->
                    AnimatedVisibility(
                        visible = showAllRecentPlaygrounds,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        ChoosePlaygroundBox(
                            canChoosePlayground = true,
                            choosePlayground = choosePlayground,
                            seeRatings = seeRatings,
                            playground = playground,
                            favoritePlaygrounds = favoritePlaygrounds
                        )
                    }
                }
            }
        }

        item(key = "your_favorite_playgrounds_title") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Favorite playgrounds",
                    color = PrimaryColor,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )

                if (favoritePlaygrounds.size > 2) {
                    TextButton(
                        onClick = { showAllFavoritePlaygrounds = !showAllFavoritePlaygrounds },
                        colors = ButtonDefaults.textButtonColors(contentColor = PrimaryVariantColor),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(horizontal = 10.dp)
                    ) {
                        Text(
                            text = if (showAllFavoritePlaygrounds)
                                stringResource(id = R.string.see_less)
                            else stringResource(id = R.string.see_all),
                            color = PrimaryVariantColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        if (favoritePlaygrounds.isEmpty()) {
            item(key = "favorite_playgrounds_empty") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.empty_favorite_playgrounds),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 10.dp)
                    )
                }
            }
        }

        items(
            items = favoritePlaygrounds.take(2),
            key = { "your_favorite_playgrounds_${it.id}" }
        ) { playground ->
            ChoosePlaygroundBox(
                canChoosePlayground = canChoosePlayground,
                choosePlayground = choosePlayground,
                seeRatings = seeRatings,
                playground = playground,
                favoritePlaygrounds = favoritePlaygrounds
            )
        }

        if (favoritePlaygrounds.size > 2) {
            items(
                items = favoritePlaygrounds.drop(2),
                key = { "your_favorite_playgrounds_${it.id}" }
            ) { playground ->
                AnimatedVisibility(
                    visible = showAllFavoritePlaygrounds,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    ChoosePlaygroundBox(
                        canChoosePlayground = canChoosePlayground,
                        choosePlayground = choosePlayground,
                        seeRatings = seeRatings,
                        playground = playground,
                        favoritePlaygrounds = favoritePlaygrounds
                    )
                }
            }
        }
    }
}