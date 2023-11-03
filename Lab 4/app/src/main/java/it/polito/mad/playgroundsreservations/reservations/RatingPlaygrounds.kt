package it.polito.mad.playgroundsreservations.reservations

import android.icu.number.Scale
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.modifier.modifierLocalMapOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import it.polito.mad.playgroundsreservations.R
import it.polito.mad.playgroundsreservations.database.Playground
import it.polito.mad.playgroundsreservations.database.PlaygroundRating
import it.polito.mad.playgroundsreservations.database.Sports
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
                        RatingPlaygroundsScreen(args.playgroundId, args.reservationId) {
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RatingPlaygroundsScreen(playgroundId: Int, reservationId:Int, onNavigate: () -> Unit) {
    val reservationsViewModel: ReservationsViewModel = viewModel()

    val playground: MutableState<Playground?> = remember { mutableStateOf(null) }
    reservationsViewModel.getPlayground(playgroundId, playground)

    if (playground.value == null)
        Text("Loading")
    else
        RatingPlaygroundsScreenContent(playground = playground.value!!, reservationId = reservationId, onNavigate)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingPlaygroundsScreenContent(playground: Playground, reservationId: Int, onNavigate: () -> Unit) {
    val reservationsViewModel: ReservationsViewModel = viewModel()

    var rating by remember { mutableStateOf(0) }
    var text by remember { mutableStateOf("") }
    var iconId by remember { mutableStateOf(0) }
    var sportNameId by remember { mutableStateOf(0) }
    val context=LocalContext.current;
    val image = when (playground.sport) {
        Sports.TENNIS -> R.drawable.tennis_court
        Sports.BASKETBALL -> R.drawable.basketball_court
        Sports.FOOTBALL -> R.drawable.football_pitch
        Sports.VOLLEYBALL -> R.drawable.volleyball_court
        Sports.GOLF -> R.drawable.golf_field
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
                Sports.TENNIS -> R.string.sport_tennis
                Sports.BASKETBALL -> R.string.sport_basketball
                Sports.FOOTBALL -> R.string.sport_football
                Sports.VOLLEYBALL -> R.string.sport_volleyball
                Sports.GOLF -> R.string.sport_golf
            }

            iconId = when (playground.sport) {
                Sports.TENNIS -> R.drawable.tennis_ball
                Sports.BASKETBALL -> R.drawable.basketball_ball
                Sports.FOOTBALL -> R.drawable.football_ball
                Sports.VOLLEYBALL -> R.drawable.volleyball_ball
                Sports.GOLF -> R.drawable.golf_ball
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
            RatingBar(rating = rating, onRatingChanged = { newRating -> rating = newRating })
        }
        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text(text = stringResource(id = R.string.optional_description_rating), color = colorResource(id = R.color.primary)) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        Button(
            onClick = {
                val playgroundRating=PlaygroundRating(playgroundId = playground.id, reservationId = reservationId , rating = rating, description = text);
                reservationsViewModel.savePlaygroundRating(playgroundRating)
                onNavigate()
            },
            content = { Text(stringResource(id =R.string.save_rating).toUpperCase(), color = Color.White) },
            colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.primary)),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
@Composable
fun RatingBar(rating: Int, onRatingChanged: (Int) -> Unit) {
    Row {
        for (i in 1..5) {
            val drawable = if (i <= rating) {
                R.drawable.baseline_star_24
            } else {
               R.drawable.baseline_star_border_24
            }
            Image(
                painter = painterResource(drawable),
                contentDescription = null,
                modifier = Modifier.clickable { onRatingChanged(i) }
            )
        }
    }
}