package it.polito.mad.playgroundsreservations.reservations.favorite_playgrounds

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.polito.mad.playgroundsreservations.R
import it.polito.mad.playgroundsreservations.database.Playground
import it.polito.mad.playgroundsreservations.database.Sport
import it.polito.mad.playgroundsreservations.database.toLocalizedStringResourceId
import it.polito.mad.playgroundsreservations.reservations.ui.theme.SecondaryVariantColor

@Composable
fun ChoosePlaygroundFilters(
    playgrounds: SnapshotStateList<Playground>,
    sportFilter: MutableState<Sport?>,
    regionFilter: MutableState<String?>,
    cityFilter: MutableState<String?>
) {
    val sportFilterExpanded = remember { mutableStateOf(false) }
    val regionFilterExpanded = remember { mutableStateOf(false) }
    val cityFilterExpanded = remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = SecondaryVariantColor),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Row (
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(sportFilter.value?.toLocalizedStringResourceId() ?: R.string.all_sports),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .clickable { sportFilterExpanded.value = true }
                        .fillMaxWidth()
                )
                SportDropDownMenu(sportFilter = sportFilter, sportFilterExpanded = sportFilterExpanded)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = regionFilter.value ?: stringResource(id = R.string.all_regions),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .clickable { regionFilterExpanded.value = true }
                        .fillMaxWidth()
                )
                RegionDropDownMenu(
                    playgrounds = playgrounds,
                    regionFilter = regionFilter,
                    regionFilterExpanded = regionFilterExpanded
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cityFilter.value ?: stringResource(id = R.string.all_cities),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .clickable { cityFilterExpanded.value = true }
                        .fillMaxWidth()
                )
                CityDropDownMenu(
                    playgrounds = playgrounds,
                    regionFilter = regionFilter,
                    cityFilter = cityFilter,
                    cityFilterExpanded = cityFilterExpanded
                )
            }
        }
    }
}

@Composable
fun SportDropDownMenu(sportFilter: MutableState<Sport?>, sportFilterExpanded: MutableState<Boolean>) {
    DropdownMenu(
        expanded = sportFilterExpanded.value,
        onDismissRequest = {
            sportFilterExpanded.value = false
        }
    ) {
        DropdownMenuItem(onClick = {
            sportFilter.value = null
            sportFilterExpanded.value = false
        }) {
            Text(text = stringResource(id = R.string.all_sports))
        }

        Sport.values().forEach { item ->
            DropdownMenuItem(onClick = {
                sportFilter.value = item
                sportFilterExpanded.value = false
            }) {
                Text(text = stringResource(item.toLocalizedStringResourceId()))
            }
        }
    }
}

@Composable
fun RegionDropDownMenu(
    playgrounds: SnapshotStateList<Playground>,
    regionFilter: MutableState<String?>,
    regionFilterExpanded: MutableState<Boolean>
) {
    DropdownMenu(
        expanded = regionFilterExpanded.value,
        onDismissRequest = {
            regionFilterExpanded.value = false
        }
    ) {
        DropdownMenuItem(onClick = {
            regionFilter.value = null
            regionFilterExpanded.value = false
        }) {
            Text(text = stringResource(id = R.string.all_regions))
        }

        playgrounds.map { it.region }.toSet().sorted().forEach { item ->
            DropdownMenuItem(onClick = {
                regionFilter.value = item
                regionFilterExpanded.value = false
            }) {
                Text(text = item)
            }
        }
    }
}

@Composable
fun CityDropDownMenu(
    playgrounds: SnapshotStateList<Playground>,
    regionFilter: MutableState<String?>,
    cityFilter: MutableState<String?>,
    cityFilterExpanded: MutableState<Boolean>
) {
    DropdownMenu(
        expanded = cityFilterExpanded.value,
        onDismissRequest = {
            cityFilterExpanded.value = false
        }
    ) {
        DropdownMenuItem(onClick = {
            cityFilter.value = null
            cityFilterExpanded.value = false
        }) {
            Text(text = stringResource(id = R.string.all_cities))
        }

        playgrounds.filter {
            regionFilter.value == null || it.region == regionFilter.value
        }.map { it.city }.toSet().sorted().forEach { item ->
            DropdownMenuItem(onClick = {
                cityFilter.value = item
                cityFilterExpanded.value = false
            }) {
                Text(text = item)
            }
        }
    }
}
