package me.echeung.moemoekyun.util

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import androidx.palette.graphics.Target.MUTED
import androidx.palette.graphics.Target.VIBRANT
import androidx.palette.graphics.get
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Scale
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import logcat.LogPriority
import logcat.asLog
import logcat.logcat
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.client.api.model.Song
import me.echeung.moemoekyun.util.ext.launchIO
import me.echeung.moemoekyun.viewmodel.RadioViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.math.max

class AlbumArtUtil(
    private val context: Context
) : KoinComponent {

    private val radioViewModel: RadioViewModel by inject()

    private val defaultAlbumArt: Bitmap by lazy {
        BitmapFactory.decodeResource(context.resources, R.drawable.default_album_art)
    }

    val flow = MutableSharedFlow<Bitmap>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    var isDefaultAlbumArt = true
        private set
    var currentAlbumArt: Bitmap? = null
        private set
    var currentAccentColor: Int = 0
        private set

    private val maxSize: Int by lazy {
        val displayMetrics = Resources.getSystem().displayMetrics
        max(displayMetrics.widthPixels, displayMetrics.heightPixels)
    }

    fun getCurrentAlbumArt(maxSize: Int): Bitmap? {
        if (currentAlbumArt == null) {
            return null
        }

        return try {
            Bitmap.createScaledBitmap(currentAlbumArt!!, maxSize, maxSize, false)
        } catch (e: Throwable) {
            // Typically OutOfMemoryError or NullPointerException
            e.printStackTrace()
            null
        }
    }

    fun updateAlbumArt(song: Song?) {
        if (song != null) {
            val albumArtUrl = song.albumArtUrl

            // Get event image if available when there's no regular album art
            if (albumArtUrl == null && radioViewModel.event != null) {
                val eventImageUrl = radioViewModel.event!!.image
                if (eventImageUrl != null) {
                    downloadAlbumArtBitmap(eventImageUrl)
                    return
                }
            }

            if (albumArtUrl != null) {
                downloadAlbumArtBitmap(albumArtUrl)
                return
            }
        }

        isDefaultAlbumArt = true
        setDefaultColors()
        updateListeners(defaultAlbumArt)
    }

    private fun updateListeners(bitmap: Bitmap) {
        currentAlbumArt = bitmap
        launchIO {
            flow.emit(bitmap)
        }
    }

    private fun downloadAlbumArtBitmap(url: String) = launchIO {
        val request = ImageRequest.Builder(context)
            .data(url)
            .scale(Scale.FILL)
            .size(maxSize, maxSize)
            .allowHardware(false)
            .build()

        val result = context.imageLoader.execute(request)
        if (result !is SuccessResult) {
            return@launchIO
        }

        val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
        if (bitmap != null) {
            isDefaultAlbumArt = false
            extractAccentColor(bitmap)
            updateListeners(bitmap)
        }
    }

    private fun extractAccentColor(resource: Bitmap) {
        try {
            val palette = Palette.from(resource).generate()
            val swatch: Palette.Swatch? = palette[VIBRANT] ?: palette[MUTED]

            if (swatch != null) {
                var color = swatch.rgb

                // Darken if needed
                if (ColorUtils.calculateLuminance(color) >= 0.5) {
                    color = ColorUtils.blendARGB(color, Color.BLACK, 0.2f)
                }

                currentAccentColor = color
            }
        } catch (e: Exception) {
            logcat(LogPriority.WARN) { e.asLog() }
            setDefaultColors()
        }
    }

    private fun setDefaultColors() {
        currentAccentColor = Color.BLACK
    }
}
