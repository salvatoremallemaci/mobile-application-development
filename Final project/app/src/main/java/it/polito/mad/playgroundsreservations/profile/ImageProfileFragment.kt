package it.polito.mad.playgroundsreservations.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import it.polito.mad.playgroundsreservations.R
import it.polito.mad.playgroundsreservations.reservations.favorite_playgrounds.ChoosePlaygroundScreen
import it.polito.mad.playgroundsreservations.reservations.invite_friends.ProfileImage
import it.polito.mad.playgroundsreservations.reservations.invite_friends.ProfileImageSize
import it.polito.mad.playgroundsreservations.reservations.ui.theme.PlaygroundsReservationsTheme

class ImageProfileFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val userId = arguments?.getString("userId")

        return ComposeView(requireContext()).apply {
            setContent {
                PlaygroundsReservationsTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.White
                    ) {
                        if (userId != null) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ProfileImage(friendId = userId, size = ProfileImageSize.BIG)
                            }
                        }
                    }
                }
            }
        }
    }
}