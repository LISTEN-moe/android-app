package me.echeung.moemoekyun.util

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.client.model.Song
import me.echeung.moemoekyun.viewmodel.RadioViewModel
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.ArrayList
import kotlin.math.max

class AlbumArtUtil(
        private val context: Context
) : KoinComponent {

    private val preferenceUtil: PreferenceUtil by inject()
    private val radioViewModel: RadioViewModel by inject()

    private val MAX_SCREEN_SIZE = maxScreenLength

    private val listeners = ArrayList<Callback>()

    private var defaultAlbumArt: Bitmap? = null

    var isDefaultAlbumArt = true
        private set
    var currentAlbumArt: Bitmap? = null
        private set
    var currentAccentColor: Int = 0
        private set

    private val maxScreenLength: Int
        get() {
            val displayMetrics = Resources.getSystem().displayMetrics
            return max(displayMetrics.widthPixels, displayMetrics.heightPixels)
        }

    fun registerListener(callback: Callback) {
        listeners.add(callback)
    }

    fun unregisterListener(callback: Callback) {
        if (listeners.contains(callback)) {
            listeners.remove(callback)
        }
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
        if (preferenceUtil.shouldDownloadImage(context) && song != null) {
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

        updateListeners(getDefaultAlbumArt())
    }

    private fun updateListeners(bitmap: Bitmap) {
        currentAlbumArt = bitmap
        listeners.forEach { it.onAlbumArtReady(bitmap) }
    }

    private fun downloadAlbumArtBitmap(url: String) {
        Handler(Looper.getMainLooper()).post(fun() {
            Glide.with(context)
                    .asBitmap()
                    .load(url)
                    .apply(RequestOptions()
                            .override(MAX_SCREEN_SIZE, MAX_SCREEN_SIZE)
                            .centerCrop()
                            .dontAnimate())
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
        })
    }

    private fun getDefaultAlbumArt(): Bitmap {
        if (defaultAlbumArt == null) {
            defaultAlbumArt = BitmapFactory.decodeResource(context.resources, R.drawable.default_album_art)
        }

        isDefaultAlbumArt = true
        setDefaultColors()

        return defaultAlbumArt!!
    }

    private fun extractAccentColor(resource: Bitmap) {
        try {
            var swatch: Palette.Swatch? = Palette.from(resource).generate().vibrantSwatch
            if (swatch == null) {
                swatch = Palette.from(resource).generate().mutedSwatch
            }
            if (swatch != null) {
                var color = swatch.rgb

                // Darken if needed
                if (ColorUtils.calculateLuminance(color) >= 0.5) {
                    color = ColorUtils.blendARGB(color, Color.BLACK, 0.2f)
                }

                currentAccentColor = color
            }
        } catch (e: Exception) {
            // Ignore things like OutOfMemoryExceptions
            e.printStackTrace()

            setDefaultColors()
        }
    }

    private fun setDefaultColors() {
        currentAccentColor = Color.BLACK
    }

    interface Callback {
        fun onAlbumArtReady(bitmap: Bitmap)
    }
}
