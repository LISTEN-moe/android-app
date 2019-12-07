package me.echeung.moemoekyun.cast

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.ext.cast.SessionAvailabilityListener
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.common.images.WebImage
import me.echeung.moemoekyun.App
import me.echeung.moemoekyun.client.api.library.Jpop

class CastDelegate(context: Context) : SessionAvailabilityListener {

    private val castPlayer: CastPlayer? = try {
        CastPlayer(CastContext.getSharedInstance(context))
    } catch (e: Exception) {
        null
    }

    init {
        castPlayer?.setSessionAvailabilityListener(this)
    }

    fun onDestroy() {
        castPlayer?.release()
    }

    override fun onCastSessionAvailable() {
        App.radioClient?.stream?.pause()

        // TODO: proper metadata and update it
        val metadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK)
        metadata.putString(MediaMetadata.KEY_TITLE, "Test Stream")
        metadata.putString(MediaMetadata.KEY_ARTIST, "Test Artist")
        metadata.addImage(WebImage(Uri.parse("https://github.com/mkaflowski/HybridMediaPlayer/blob/master/images/cover.jpg?raw=true")))

        val mediaInfo = MediaInfo.Builder(Jpop.INSTANCE.streamUrl)
                .setStreamType(MediaInfo.STREAM_TYPE_LIVE)
                .setContentType(MimeTypes.AUDIO_UNKNOWN)
                .setMetadata(metadata).build()

        val mediaItems = arrayOf(MediaQueueItem.Builder(mediaInfo).build())

        // TODO: hook up app UI controls to control cast player
        castPlayer?.loadItems(mediaItems, 0, 0, Player.REPEAT_MODE_OFF)
    }

    override fun onCastSessionUnavailable() {
        // TODO: double check if it was playing before

        App.radioClient?.stream?.play()
    }

}
