package me.echeung.moemoekyun.cast

import android.content.Context
import android.net.Uri
import android.view.Menu
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.ext.cast.SessionAvailabilityListener
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.common.images.WebImage
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.client.RadioClient
import me.echeung.moemoekyun.client.api.socket.Socket
import me.echeung.moemoekyun.client.api.socket.response.UpdateResponse
import me.echeung.moemoekyun.client.stream.Stream
import me.echeung.moemoekyun.viewmodel.RadioViewModel
import org.koin.core.KoinComponent
import org.koin.core.inject

class CastDelegate(
    private val context: Context
) : SessionAvailabilityListener, Socket.Listener, KoinComponent {

    private val radioViewModel: RadioViewModel by inject()

    private val stream: Stream by inject()
    private val socket: Socket by inject()

    private val castPlayer: CastPlayer? = try {
        CastPlayer(CastContext.getSharedInstance(context))
    } catch (e: Exception) {
        null
    }

    init {
        castPlayer?.setSessionAvailabilityListener(this)

        socket.addListener(this)
    }

    fun onDestroy() {
        castPlayer?.release()
    }

    fun initCastButton(menu: Menu?) {
        CastButtonFactory.setUpMediaRouteButton(
            context,
            menu,
            R.id.media_route_menu_item
        )
    }

    override fun onCastSessionAvailable() {
        stream.pause()

        playMedia()
    }

    private fun playMedia() {
        val song = radioViewModel.currentSong

        val metadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK)
        metadata.putString(MediaMetadata.KEY_TITLE, song?.titleString)
        metadata.putString(MediaMetadata.KEY_ARTIST, song?.artistsString)
        song?.albumArtUrl?.let {
            metadata.addImage(WebImage(Uri.parse(it)))
        }

        // TODO: react to switching between jpop/kpop
        val mediaInfo = MediaInfo.Builder(RadioClient.library?.streamUrl)
            .setStreamType(MediaInfo.STREAM_TYPE_LIVE)
            .setContentType(MimeTypes.AUDIO_UNKNOWN)
            .setMetadata(metadata).build()

        val mediaItems = arrayOf(MediaQueueItem.Builder(mediaInfo).build())

        // TODO: hook up app UI controls to control cast player
        castPlayer?.loadItems(mediaItems, 0, 0, Player.REPEAT_MODE_OFF)
    }

    override fun onCastSessionUnavailable() {
        // TODO: double check if it was playing before

        stream.play()
    }

    // TODO: I don't think this works
    override fun onSocketReceive(info: UpdateResponse.Details?) {
        playMedia()
    }

    override fun onSocketFailure() {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }
}
