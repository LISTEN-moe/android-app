package me.echeung.moemoekyun.util

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import androidx.palette.graphics.Target.MUTED
import androidx.palette.graphics.Target.VIBRANT
import androidx.palette.graphics.get
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.client.model.Song
import me.echeung.moemoekyun.util.ext.launchIO
import me.echeung.moemoekyun.util.ext.launchNow
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

    val channel = ConflatedBroadcastChannel<Bitmap>()

    var isDefaultAlbumArt = true
        private set
    var currentAlbumArt: Bitmap? = null
        private set
    var currentAccentColor: Int = 0
        private set

    private val requestOptions: RequestOptions by lazy {
        val displayMetrics = Resources.getSystem().displayMetrics
        val maxScreenLength = max(displayMetrics.widthPixels, displayMetrics.heightPixels)

        RequestOptions()
            .override(maxScreenLength, maxScreenLength)
            .centerCrop()
            .dontAnimate()
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
        launchNow {
            channel.send(bitmap)
        }
    }

    private fun downloadAlbumArtBitmap(url: String) {
        launchIO {
            Glide.with(context)
                .asBitmap()
                .load(url)
                .apply(requestOptions)
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Bitmap, model: Any, target: Target<Bitmap>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        isDefaultAlbumArt = false
                        extractAccentColor(resource)
                        updateListeners(resource)
                        return true
                    }
                })
                .submit()
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
            setDefaultColors()
        }
    }

    private fun setDefaultColors() {
        currentAccentColor = Color.BLACK
    }
}
