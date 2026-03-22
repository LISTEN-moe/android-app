package me.echeung.moemoekyun.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import me.echeung.moemoekyun.R

@Composable
fun AlbumArt(
    albumArtUrl: String?,
    modifier: Modifier = Modifier,
    openUrlOnClick: Boolean = true,
    cornerRadius: Dp = 16.dp,
) {
    val uriHandler = LocalUriHandler.current
    val albumArtModifier = Modifier
        .aspectRatio(1f)
        .clip(RoundedCornerShape(cornerRadius))

    if (albumArtUrl == null) {
        Image(
            modifier = albumArtModifier.then(modifier),
            painter = painterResource(R.drawable.default_album_art),
            contentDescription = null,
        )
    } else {
//        val request = ImageRequest.Builder(LocalContext.current)
//            .data(albumArtUrl)
//            .transformations(BlurTransformation())
//            .build()

        AsyncImage(
            modifier = albumArtModifier
                .then(modifier)
                .clickable { if (openUrlOnClick) uriHandler.openUri(albumArtUrl) },
            model = albumArtUrl,
            placeholder = painterResource(R.drawable.default_album_art),
            contentScale = ContentScale.Crop,
            contentDescription = null,
        )
    }
}
