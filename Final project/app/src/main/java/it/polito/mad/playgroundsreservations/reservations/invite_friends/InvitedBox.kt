package it.polito.mad.playgroundsreservations.reservations.invite_friends

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.polito.mad.playgroundsreservations.database.Reservation
import it.polito.mad.playgroundsreservations.reservations.ui.theme.PrimaryColor
import it.polito.mad.playgroundsreservations.reservations.ui.theme.PrimaryVariantColor
import it.polito.mad.playgroundsreservations.reservations.ui.theme.SecondaryColor
import it.polito.mad.playgroundsreservations.reservations.ui.theme.SecondaryTransparentColor

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun InvitedBox(reservation: MutableState<Reservation?>) {
    Row(modifier = Modifier.padding(top = 10.dp)) {
        Text(
            text = "Invited for this reservation",
            color = PrimaryColor,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
    }

    Row {
        FlowRow(
            modifier = Modifier
                .padding(horizontal = 5.dp)
                .padding(top = 5.dp)
                .fillMaxWidth()
                .wrapContentHeight(align = Alignment.CenterVertically)
        ) {
            reservation.value!!.invitations.forEach { invitation ->
                InputChip(
                    selected = false,
                    onClick = { },
                    modifier = Modifier
                        .padding(horizontal = 5.dp),
                    colors = InputChipDefaults.inputChipColors(
                        containerColor = SecondaryTransparentColor,
                        labelColor = PrimaryVariantColor
                    ),
                    border = InputChipDefaults.inputChipBorder(borderColor = SecondaryColor),
                    label = { Text(
                        text = invitation.fullName,
                        modifier = Modifier
                    ) },
                    leadingIcon = {
                        ProfileImage(friendId = invitation.userId, size = ProfileImageSize.SMALL)
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                reservation.value!!.invitations.removeIf {
                                    it.userId == invitation.userId
                                }
                            },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                Icons.Filled.Clear,
                                tint = SecondaryColor,
                                contentDescription = "Disinvite"
                            )
                        }
                    }
                )
            }
        }
    }
}