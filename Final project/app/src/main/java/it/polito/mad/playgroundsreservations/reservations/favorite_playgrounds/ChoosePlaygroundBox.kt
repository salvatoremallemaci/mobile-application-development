package it.polito.mad.playgroundsreservations.reservations.favorite_playgrounds

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polito.mad.playgroundsreservations.R
import it.polito.mad.playgroundsreservations.database.Playground
import it.polito.mad.playgroundsreservations.database.PlaygroundRating
import it.polito.mad.playgroundsreservations.database.Sport
import it.polito.mad.playgroundsreservations.reservations.ViewModel
import it.polito.mad.playgroundsreservations.reservations.ui.theme.PrimaryColor
import it.polito.mad.playgroundsreservations.reservations.ui.theme.PrimaryVariantColor
import it.polito.mad.playgroundsreservations.reservations.ui.theme.SecondaryColor

@Composable
fun ChoosePlaygroundBox(
    canChoosePlayground: Boolean,
    choosePlayground: (HashMap<String, String>) -> Unit,
    seeRatings: (String) -> Unit,
    playground: Playground,
    favoritePlaygrounds: SnapshotStateList<Playground>
) {
    val viewModel: ViewModel = viewModel()
    var showAdditionalInfo by rememberSaveable { mutableStateOf(false) }
    var isFavoritePlayground by remember { mutableStateOf(favoritePlaygrounds.contains(playground)) }
    val ratingsList = remember { mutableStateListOf<PlaygroundRating>() }

    LaunchedEffect(favoritePlaygrounds.size) {
        isFavoritePlayground = favoritePlaygrounds.contains(playground)
    }

    LaunchedEffect(true) {
        if (canChoosePlayground) {  // otherwise I don't have the navController and I cannot show the other page
            viewModel.getRatingsByPlaygroundId(
                playgroundId = playground.id,
                ratingsState = ratingsList
            )
        }
    }

    val sportIcon = when (playground.sport) {
        Sport.TENNIS -> R.drawable.tennis_ball
        Sport.BASKETBALL -> R.drawable.basketball_ball
        Sport.FOOTBALL -> R.drawable.football_ball
        Sport.VOLLEYBALL -> R.drawable.volleyball_ball
        Sport.GOLF -> R.drawable.golf_ball
    }

    val sportName = when (playground.sport) {
        Sport.TENNIS -> R.string.sport_tennis
        Sport.BASKETBALL -> R.string.sport_basketball
        Sport.FOOTBALL -> R.string.sport_football
        Sport.VOLLEYBALL -> R.string.sport_volleyball
        Sport.GOLF -> R.string.sport_golf
    }

    val playgroundImage = when (playground.sport) {
        Sport.TENNIS -> R.drawable.tennis_court
        Sport.BASKETBALL -> R.drawable.basketball_court
        Sport.FOOTBALL -> R.drawable.football_pitch
        Sport.VOLLEYBALL -> R.drawable.volleyball_court
        Sport.GOLF -> R.drawable.golf_field
    }

    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(10.dp)
        .border(
            BorderStroke(2.dp, PrimaryColor),
            shape = MaterialTheme.shapes.medium
        )) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 5.dp)
                    ) {
                        Image(
                            painter = painterResource(id = playgroundImage),
                            contentDescription = "Playground image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(75.dp)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .width(IntrinsicSize.Max),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row {
                            Text(
                                text = playground.name,
                                color = PrimaryVariantColor,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Image(
                                    painter = painterResource(id = sportIcon),
                                    contentDescription = "Sport icon"
                                )
                            }

                            Column {
                                Text(text = stringResource(id = sportName))
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(IntrinsicSize.Max),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedButton(
                        onClick = {
                            if (isFavoritePlayground) {
                                viewModel.removeFavoritePlayground(playground.id)
                                favoritePlaygrounds.remove(playground)
                            } else {
                                viewModel.addFavoritePlayground(playground.id)
                                favoritePlaygrounds.add(playground)
                            }
                        }
                    ) {
                        val starIcon = if (isFavoritePlayground) R.drawable.filled_star else R.drawable.bordered_star

                        Image(
                            painter = painterResource(id = starIcon),
                            contentDescription = "Add playground to favorites",
                            modifier = Modifier
                                .size(24.dp)
                                .aspectRatio(1f)
                        )
                    }
                }
            }

            if (canChoosePlayground) {
                Row(modifier = Modifier.padding(top = 10.dp)) {
                    Button(
                        onClick = {
                            val p = hashMapOf(
                                "id" to playground.id,
                                "sport" to playground.sport.name.lowercase(),
                                "pricePerHour" to playground.pricePerHour.toString()
                            )

                            choosePlayground(p) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(id = R.string.select_playground).uppercase())
                    }
                }
            }

            Row {
                TextButton(
                    onClick = { showAdditionalInfo = !showAdditionalInfo },
                    colors = ButtonDefaults.textButtonColors(contentColor = PrimaryVariantColor),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (showAdditionalInfo)
                            stringResource(id = R.string.see_less)
                        else stringResource(id = R.string.see_more),
                        color = PrimaryVariantColor,
                        textAlign = TextAlign.Center
                    )
                }
            }

            AnimatedVisibility(visible = showAdditionalInfo) {
                Column {
                    Row {
                        Text(
                            text = stringResource(id = R.string.address),
                            color = PrimaryColor,
                            modifier = Modifier.padding(horizontal = 5.dp)
                        )
                    }

                    Row {
                        Text(
                            text = playground.address,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                    }

                    Row {
                        Text(
                            text = stringResource(id = R.string.price_per_hour),
                            color = PrimaryColor,
                            modifier = Modifier
                                .padding(top = 5.dp)
                                .padding(horizontal = 5.dp)
                        )
                    }

                    Row {
                        Text(
                            text = stringResource(id = R.string.price, playground.pricePerHour),
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                    }

                    Row {
                        Text(
                            text = stringResource(id = R.string.max_number_of_people),
                            color = PrimaryColor,
                            modifier = Modifier
                                .padding(top = 5.dp)
                                .padding(horizontal = 5.dp)
                        )
                    }

                    Row {
                        Text(
                            text = stringResource(id = R.string.n_people, playground.maxPlayers),
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                    }

                    if (canChoosePlayground) {  // otherwise I don't have the navController
                        Row {
                            OutlinedButton(
                                onClick = { seeRatings(playground.id) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryVariantColor),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                            ) {
                                Text(
                                    text = if (ratingsList.isEmpty())
                                        stringResource(id = R.string.no_ratings_yet_playground)
                                    else stringResource(id = R.string.see_ratings),
                                    color = PrimaryVariantColor,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}