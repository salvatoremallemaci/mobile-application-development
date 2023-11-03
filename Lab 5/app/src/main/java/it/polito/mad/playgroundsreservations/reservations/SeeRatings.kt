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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.smarttoolfactory.ratingbar.RatingBar
import it.polito.mad.playgroundsreservations.R
import it.polito.mad.playgroundsreservations.database.Playground
import it.polito.mad.playgroundsreservations.database.PlaygroundRating
import it.polito.mad.playgroundsreservations.database.Sport
import it.polito.mad.playgroundsreservations.reservations.ui.theme.PlaygroundsReservationsTheme

class SeeRatings : Fragment() {
    private val args by navArgs<SeeRatingsArgs>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // ACTIVITY TITLE
        activity?.title = activity?.resources?.getString(R.string.ratings_playground)

        return ComposeView(requireContext()).apply {
            setContent {
                PlaygroundsReservationsTheme {
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        SeeRatingsScreen(
                            playgroundId = args.playgroundId,
                            navController = findNavController()
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun SeeRatingsScreen(playgroundId: String, navController: NavController) {
    val viewModel: ViewModel = viewModel()

    val playground: MutableState<Playground?> = remember { mutableStateOf(null) }
    val ratingsList = remember { mutableStateOf(listOf<PlaygroundRating>()) }
    // Get all ratings of the playground
    viewModel.getRatingsByPlaygroundId(playgroundId, ratingsList)
    // Get all info about that playground
    viewModel.getPlayground(playgroundId, playground)


    if (playground.value == null && ratingsList.value.isEmpty())
        MyLoadingSeeRatings()
    else
        SeeRatingsScreenContent(playground = playground.value!!, ratingsList)
}

@Composable
fun SeeRatingsScreenContent(playground: Playground, ratingList: MutableState<List<PlaygroundRating>>) {

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
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = playground.name, fontWeight = FontWeight.Normal, fontSize = 32.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(bottom = 16.dp))
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .border(1.dp, Color.Black, CircleShape)
        ){
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
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn{
            items(ratingList.value){item: PlaygroundRating ->
                ListItemComponent(item = item)
            }
        }
    }

}


@Composable
fun ListItemComponent(item: PlaygroundRating) {
    // var isExpanded by remember { mutableStateOf(false) }
    var starCount = item.rating

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
            // .clickable { if (item.description != "") isExpanded = !isExpanded },
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            RatingBar(
                rating = starCount,
                space = 2.dp,
                imageVectorEmpty = ImageVector.vectorResource(id = R.drawable.bordered_star),
                imageVectorFFilled = ImageVector.vectorResource(id = R.drawable.filled_star),
                tintEmpty = Color(0xff00668b),
                tintFilled = Color(0xff00668b),
                itemSize = 36.dp,
                gestureEnabled = false
            ) {
                starCount = it
            }
            /* IconButton(onClick = { isExpanded = !isExpanded }) {
                val res: Int = if (isExpanded)
                    R.drawable.baseline_arrow_circle_up_24
                else
                    R.drawable.baseline_arrow_circle_down_24
                Icon(
                        painter = painterResource(res),
                        contentDescription = null
                    )
                } */
        }

        // if (isExpanded) {
            val descriptionText = if (item.description == "") {
                "${stringResource(id = R.string.optional_description_entered)} - ${item.fullName}"
            } else {
                "\"${item.description}\" - ${item.fullName}"
            }
            Text(
                text = descriptionText,
                modifier = Modifier.padding(top = 8.dp),
                style = TextStyle(fontStyle = FontStyle.Italic)
            )
        // }
    }
}

@Composable
fun MyLoadingSeeRatings() {
    AndroidView(
        factory = { context ->
            LayoutInflater.from(context).inflate(R.layout.spinner_fragment, null)
        }
    )
}