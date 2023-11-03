package it.polito.mad.playgroundsreservations.reservations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.smarttoolfactory.ratingbar.RatingBar
import it.polito.mad.playgroundsreservations.Global
import it.polito.mad.playgroundsreservations.R
import it.polito.mad.playgroundsreservations.database.Playground
import it.polito.mad.playgroundsreservations.database.PlaygroundRating
import it.polito.mad.playgroundsreservations.database.Sport
import it.polito.mad.playgroundsreservations.reservations.ui.theme.PlaygroundsReservationsTheme

class RatingPlaygrounds: Fragment() {
    private val args by navArgs<RatingPlaygroundsArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // ACTIVITY TITLE
        activity?.title = activity?.resources?.getString(R.string.rate_court)

        return ComposeView(requireContext()).apply {
            setContent {
                PlaygroundsReservationsTheme {
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        RatingPlaygroundsScreen(args.playgroundId, args.reservationId,  findNavController())
                    }
                }
            }
        }
    }
}

@Composable
fun RatingPlaygroundsScreen(playgroundId: String, reservationId: String, navController: NavController) {
    val viewModel: ViewModel = viewModel()

    val playground: MutableState<Playground?> = remember { mutableStateOf(null) }

    LaunchedEffect(true) {
        viewModel.getPlayground(playgroundId, playground)
    }

    if (playground.value == null)
        LoadingScreen()
    else
        RatingPlaygroundsScreenContent(playground = playground.value!!, reservationId = reservationId, navController)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingPlaygroundsScreenContent(playground: Playground, reservationId: String, navController: NavController) {
    val viewModel: ViewModel = viewModel()

    var rating by remember { mutableStateOf(1f) }
    var text by remember { mutableStateOf("") }
    var iconId by remember { mutableStateOf(0) }
    var sportNameId by remember { mutableStateOf(0) }
    val image = when (playground.sport) {
        Sport.TENNIS -> R.drawable.tennis_court
        Sport.BASKETBALL -> R.drawable.basketball_court
        Sport.FOOTBALL -> R.drawable.football_pitch
        Sport.VOLLEYBALL -> R.drawable.volleyball_court
        Sport.GOLF -> R.drawable.golf_field
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = playground.name, fontWeight = FontWeight.Normal, fontSize = 32.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(bottom = 16.dp))
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .border(1.dp, Color.Black, CircleShape)
        ) {
            Image(
                painter = painterResource(id = image),
                contentDescription = null,
                contentScale = ContentScale.FillBounds
            )
        }

        Text(text = playground.address, fontWeight = FontWeight.Normal, fontSize = 20.sp, textAlign = TextAlign.Center, modifier = Modifier
            .padding(bottom = 8.dp)
            .padding(top = 8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {

            sportNameId = when (playground.sport) {
                Sport.TENNIS -> R.string.sport_tennis
                Sport.BASKETBALL -> R.string.sport_basketball
                Sport.FOOTBALL -> R.string.sport_football
                Sport.VOLLEYBALL -> R.string.sport_volleyball
                Sport.GOLF -> R.string.sport_golf
            }

            iconId = when (playground.sport) {
                Sport.TENNIS -> R.drawable.tennis_ball
                Sport.BASKETBALL -> R.drawable.basketball_ball
                Sport.FOOTBALL -> R.drawable.football_ball
                Sport.VOLLEYBALL -> R.drawable.volleyball_ball
                Sport.GOLF -> R.drawable.golf_ball
            }

            Icon(
                painter = painterResource(id = iconId),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
            Text(text = stringResource(id = sportNameId), Modifier.padding(start = 8.dp), style = TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 20.sp
            )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(text = stringResource(id = R.string.rating_label), fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(end = 8.dp))
            RatingBar(
                rating = rating,
                space = 2.dp,
                imageVectorEmpty = ImageVector.vectorResource(id = R.drawable.bordered_star),
                imageVectorFFilled = ImageVector.vectorResource(id = R.drawable.filled_star),
                tintEmpty = Color(0xff00668b),
                tintFilled = Color(0xff00668b),
                itemSize = 36.dp
            ) {
                rating = it
            }
        }
        TextField(
            value = text,
            onValueChange = { text = it },
            maxLines = 5,
            label = { Text(text = stringResource(id = R.string.optional_description_rating), color = colorResource(id = R.color.primary)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        Button(
            onClick = {
                val playgroundRating = PlaygroundRating(
                    "",
                    playgroundId = viewModel.getPlaygroundReference(playground.id),
                    reservationId = viewModel.getReservationReference(reservationId),
                    rating = rating,
                    description = text,
                    fullName = Global.fullName ?: ""
                )
                viewModel.savePlaygroundRating(playgroundRating)
                navController.popBackStack()
            },
            content = { Text(stringResource(id =R.string.save_rating).uppercase(), color = Color.White) },
            colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.primary)),
            modifier = Modifier.padding(top = 8.dp)
        )
        /*
        Button(
            onClick = {
               // val playgroundRating=PlaygroundRating(playgroundId = playground.id, reservationId = reservationId , rating = rating, description = text);
                //reservationsViewModel.savePlaygroundRating(playgroundRating)
                val action = RatingPlaygroundsDirections.actionRatingPlaygroundsToSeeRatings(playground.id)
                navController.navigate(action)
            },
            content = { Text(stringResource(id =R.string.see_ratings).uppercase(), color = Color.White) },
            colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.primary)),
            modifier = Modifier.padding(top = 8.dp)
        ) */
    }
}