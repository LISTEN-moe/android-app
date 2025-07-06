package me.echeung.moemoekyun.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.compose.runtime.Immutable
import androidx.core.graphics.BitmapCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import androidx.palette.graphics.Target.MUTED
import androidx.palette.graphics.Target.VIBRANT
import androidx.palette.graphics.get
import coil3.asDrawable
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.size.Scale
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import logcat.LogPriority
import logcat.asLog
import logcat.logcat
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.domain.radio.interactor.CurrentSong
import me.echeung.moemoekyun.domain.songs.model.DomainSong
import me.echeung.moemoekyun.util.ext.launchIO
import me.echeung.moemoekyun.util.ext.withIOContext
import javax.inject.Inject

class AlbumArtUtil @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val currentSong: CurrentSong,
) {

    private val defaultAlbumArt: Bitmap by lazy {
        BitmapFactory.decodeResource(context.resources, R.drawable.default_album_art)
    }

    private val _flow = MutableStateFlow<State?>(null)
    val flow = _flow.asStateFlow()

    private val scope = MainScope()

    init {
        scope.launchIO {
            currentSong.albumArtFlow().collectLatest(::updateAlbumArt)
        }
    }

    fun getCurrentAlbumArt(size: Int): Bitmap? {
        if (_flow.value?.bitmap == null) {
            return null
        }

        return try {
            BitmapCompat.createScaledBitmap(_flow.value!!.bitmap!!, size, size, null, false)
        } catch (e: Throwable) {
            // Typically OutOfMemoryError or NullPointerException
            e.printStackTrace()
            null
        }
    }

    private suspend fun updateAlbumArt(albumArtUrl: String?) {
        val bitmap = getAlbumArtBitmap(albumArtUrl)
        val accentColor = extractAccentColor(bitmap)

        scope.launch {
            _flow.value = State(
                bitmap = bitmap,
                accentColor = accentColor,
            )
        }
    }

    private suspend fun getAlbumArtBitmap(url: String?): Bitmap = withIOContext {
        if (url == null) {
            return@withIOContext defaultAlbumArt
        }

        val request = ImageRequest.Builder(context)
            .data(url)
            .scale(Scale.FILL)
            .build()

        val result = context.imageLoader.execute(request)
        if (result !is SuccessResult) {
            return@withIOContext defaultAlbumArt
        }

        result.image.asDrawable(context.resources).toBitmap()
    }

    private fun extractAccentColor(resource: Bitmap): Int? {
        return try {
            val palette = Palette.from(resource).generate()
            val swatch: Palette.Swatch? = palette[VIBRANT] ?: palette[MUTED]

            if (swatch != null) {
                var color = swatch.rgb

                // Darken if needed
                if (ColorUtils.calculateLuminance(color) >= 0.5) {
                    color = ColorUtils.blendARGB(color, Color.BLACK, 0.2f)
                }

                return color
            }
            null
        } catch (e: Exception) {
            logcat(LogPriority.WARN) { e.asLog() }
            null
        }
    }

    @Immutable
    data class State(
        val bitmap: Bitmap?,
        val accentColor: Int?,
    )
}
