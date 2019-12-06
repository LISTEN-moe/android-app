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
import me.echeung.moemoekyun.client.api.library.Jpop

class CastDelegate {

    private var castPlayer: CastPlayer? = null

    fun init(context: Context) {
        val castContext = CastContext.getSharedInstance(context)

        // TODO: proper metadata and update it
        val movieMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK)
        movieMetadata.putString(MediaMetadata.KEY_TITLE, "Test Stream")
        movieMetadata.putString(MediaMetadata.KEY_ALBUM_ARTIST, "Test Artist")
        movieMetadata.addImage(WebImage(Uri.parse("https://github.com/mkaflowski/HybridMediaPlayer/blob/master/images/cover.jpg?raw=true")))
        val mediaInfo = MediaInfo.Builder(Jpop.INSTANCE.streamUrl)
                .setStreamType(MediaInfo.STREAM_TYPE_LIVE)
                .setContentType(MimeTypes.AUDIO_UNKNOWN)
                .setMetadata(movieMetadata).build()

        val mediaItems = arrayOf(MediaQueueItem.Builder(mediaInfo).build())

        castPlayer = CastPlayer(castContext)
        castPlayer?.setSessionAvailabilityListener(object : SessionAvailabilityListener {
            override fun onCastSessionAvailable() {
                // TODO: pause on device, hook up controls
                castPlayer?.loadItems(mediaItems, 0, 0, Player.REPEAT_MODE_OFF)
            }

            override fun onCastSessionUnavailable() {}
        })
    }

}
