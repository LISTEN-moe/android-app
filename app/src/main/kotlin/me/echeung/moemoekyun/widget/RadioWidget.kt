package me.echeung.moemoekyun.widget

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap
import me.echeung.moemoekyun.R

class RadioWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override val sizeMode = SizeMode.Responsive(
        setOf(
            SIZE_SMALL, // album art + play button
            SIZE_MEDIUM, // + song title
            SIZE_LARGE, // + artist + favorite button
        ),
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Load album art before composition – Coil's in-memory cache makes re-fetches cheap.
        val prefs = getAppWidgetState(context, PreferencesGlanceStateDefinition, id)
        val albumArt = loadAlbumArt(context, prefs[keyAlbumArtUrl])

        provideContent {
            GlanceTheme {
                Content(albumArt)
            }
        }
    }

    @Composable
    private fun Content(albumArt: Bitmap?) {
        val prefs = currentState<Preferences>()
        val size = LocalSize.current

        val title = prefs[keyTitle]
        val artists = prefs[keyArtists]
        val isPlaying = prefs[keyIsPlaying] ?: false
        val isFavorited = prefs[keyIsFavorited] ?: false
        val isAuthenticated = prefs[keyIsAuthenticated] ?: false
        val songId = prefs[keySongId]

        val showTitle = size.width >= SIZE_MEDIUM.width
        val showArtistAndFav = size.width >= SIZE_LARGE.width

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .clickable(actionRunCallback<OpenAppAction>()),
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(
                modifier = GlanceModifier.fillMaxSize().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AlbumArtImage(albumArt)

                Spacer(GlanceModifier.width(8.dp))

                if (showTitle) {
                    Column(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight()
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (title != null) {
                            Text(
                                text = title,
                                style = TextStyle(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = GlanceTheme.colors.onSurface,
                                ),
                                maxLines = 1,
                            )
                            if (showArtistAndFav && artists != null) {
                                Text(
                                    text = artists,
                                    style = TextStyle(
                                        fontSize = 11.sp,
                                        color = GlanceTheme.colors.secondary,
                                    ),
                                    maxLines = 1,
                                )
                            }
                        } else {
                            Text(
                                text = "…",
                                style = TextStyle(color = GlanceTheme.colors.onSurface),
                            )
                        }
                    }
                } else {
                    Spacer(GlanceModifier.defaultWeight())
                }

                PlayPauseButton(isPlaying)

                if (showArtistAndFav && isAuthenticated && songId != null) {
                    Spacer(GlanceModifier.width(4.dp))
                    FavoriteButton(songId, isFavorited)
                }
            }
        }
    }

    @Composable
    private fun AlbumArtImage(bitmap: Bitmap?) {
        val mod = GlanceModifier.size(40.dp).cornerRadius(6.dp)
        if (bitmap != null) {
            Image(
                provider = ImageProvider(bitmap),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = mod,
            )
        } else {
            Image(
                provider = ImageProvider(R.drawable.default_album_art),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = mod,
            )
        }
    }

    @Composable
    private fun PlayPauseButton(isPlaying: Boolean) {
        Image(
            provider = ImageProvider(if (isPlaying) R.drawable.ic_pause_24dp else R.drawable.ic_play_arrow_24dp),
            contentDescription = null,
            colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface),
            modifier = GlanceModifier
                .size(40.dp)
                .clickable(actionRunCallback<PlayPauseAction>()),
        )
    }

    @Composable
    private fun FavoriteButton(songId: Int, isFavorited: Boolean) {
        Image(
            provider = ImageProvider(if (isFavorited) R.drawable.ic_star_24dp else R.drawable.ic_star_border_24dp),
            contentDescription = null,
            colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface),
            modifier = GlanceModifier
                .size(40.dp)
                .clickable(
                    actionRunCallback<FavoriteAction>(
                        actionParametersOf(songIdParam to songId),
                    ),
                ),
        )
    }

    companion object {
        private val SIZE_SMALL = DpSize(110.dp, 56.dp)
        private val SIZE_MEDIUM = DpSize(180.dp, 56.dp)
        private val SIZE_LARGE = DpSize(270.dp, 56.dp)
    }
}

private suspend fun loadAlbumArt(context: Context, url: String?): Bitmap? {
    if (url == null) return null
    return try {
        val request = ImageRequest.Builder(context).data(url).build()
        val result = context.imageLoader.execute(request)
        (result as? SuccessResult)?.image?.toBitmap()
    } catch (_: Exception) {
        null
    }
}
