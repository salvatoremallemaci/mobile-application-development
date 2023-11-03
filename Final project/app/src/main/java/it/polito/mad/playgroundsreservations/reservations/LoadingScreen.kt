package it.polito.mad.playgroundsreservations.reservations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import it.polito.mad.playgroundsreservations.R
import it.polito.mad.playgroundsreservations.reservations.ui.theme.SecondaryColor

@Composable
fun LoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = SecondaryColor,
            strokeWidth = 7.dp,
            modifier = Modifier
                .size(75.dp)
                .padding(bottom = 30.dp)
        )

        Text(
            text = stringResource(id = R.string.loading),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(top = 30.dp)
        )
    }
}