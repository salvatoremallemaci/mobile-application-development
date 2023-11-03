package it.polito.mad.playgroundsreservations.reservations.invite_friends

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.polito.mad.playgroundsreservations.R
import it.polito.mad.playgroundsreservations.reservations.ui.theme.SecondaryVariantColor

enum class ProfileImageSize {
    SMALL, MEDIUM, BIG
}

@Composable
fun ProfileImage(friendId: String, size: ProfileImageSize) {
    val storageReference = Firebase.storage.reference.child("profileImages/${friendId}")
    var imageUrl: Uri? by remember { mutableStateOf(null) }
    var showDefaultImage by remember { mutableStateOf(false) }

    storageReference.downloadUrl.addOnSuccessListener {
        imageUrl = it
    }.addOnFailureListener {
        showDefaultImage = true
        Log.d("IMAGE URL", it.message ?: "")
    }

    val width = when (size) {
        ProfileImageSize.SMALL -> 25.dp
        ProfileImageSize.MEDIUM -> 75.dp
        ProfileImageSize.BIG -> 200.dp
    }

    val strokeWidth = when (size) {
        ProfileImageSize.SMALL -> 2.dp
        ProfileImageSize.MEDIUM -> 4.dp
        ProfileImageSize.BIG -> 4.dp
    }

    val loadingModifier = when (size) {
        ProfileImageSize.SMALL -> Modifier.size(25.dp).padding(5.dp)
        ProfileImageSize.MEDIUM -> Modifier.padding(15.dp)
        ProfileImageSize.BIG -> Modifier.padding(45.dp)
    }

    if (imageUrl != null) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Profile Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(width)
                .aspectRatio(1f)
                .clip(CircleShape)
        ) {
            val state = painter.state
            if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Error) {
                CircularProgressIndicator(
                    color = SecondaryVariantColor,
                    strokeWidth = strokeWidth,
                    modifier = loadingModifier
                )
            } else {
                SubcomposeAsyncImageContent()
            }
        }
    } else if (showDefaultImage) {
        Image(
            painter = painterResource(id = R.drawable.user_profile),
            contentDescription = "Profile image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(width)
                .aspectRatio(1f)
                .clip(CircleShape)
        )
    } else {
        CircularProgressIndicator(
            color = SecondaryVariantColor,
            strokeWidth = strokeWidth,
            modifier = loadingModifier
        )
    }
}