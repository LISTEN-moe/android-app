package me.echeung.moemoekyun.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import me.echeung.moemoekyun.R

private val AlbumArtModifier = Modifier
    .aspectRatio(1f)
    .clip(RoundedCornerShape(8.dp))

@Composable
fun AlbumArt(albumArtUrl: String?, modifier: Modifier = Modifier, openUrlOnClick: Boolean = true) {
    val uriHandler = LocalUriHandler.current

    if (albumArtUrl == null) {
        Image(
            modifier = AlbumArtModifier.then(modifier),
            painter = painterResource(R.drawable.default_album_art),
            contentDescription = null,
        )
    } else {
        AsyncImage(
            modifier = AlbumArtModifier
                .then(modifier)
                .clickable { if (openUrlOnClick) uriHandler.openUri(albumArtUrl) },
            model = albumArtUrl,
            placeholder = painterResource(R.drawable.default_album_art),
            contentDescription = null,
        )
    }
}
