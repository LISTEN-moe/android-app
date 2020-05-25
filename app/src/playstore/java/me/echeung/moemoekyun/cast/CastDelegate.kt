package me.echeung.moemoekyun.cast

import android.content.Context
import android.net.Uri
import android.view.Menu
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.ext.cast.SessionAvailabilityListener
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.common.images.WebImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.client.RadioClient
import me.echeung.moemoekyun.client.api.socket.Socket
import me.echeung.moemoekyun.client.stream.Stream
import me.echeung.moemoekyun.viewmodel.RadioViewModel

class CastDelegate(
    private val context: Context,
    private val radioViewModel: RadioViewModel,
    private val stream: Stream,
    private val socket: Socket
) {

    private val scope = CoroutineScope(Job() + Dispatchers.Main)

    private val castPlayer: CastPlayer? = try {
        CastPlayer(CastContext.getSharedInstance(context))
    } catch (e: Exception) {
        null
    }
    private var castStreamPlayer: CastStreamPlayer? = null

    init {
        castPlayer?.let { player ->
            castStreamPlayer = CastStreamPlayer(player)

            player.setSessionAvailabilityListener(object : SessionAvailabilityListener {
                override fun onCastSessionAvailable() {
                    stream.setAltPlayer(castStreamPlayer)
                    updateSong()
                }

                override fun onCastSessionUnavailable() {
                    stream.setAltPlayer(null)
                }
            })

            socket.channel.asFlow()
                .onEach {
                    when (it) {
                        is Socket.SocketResponse -> updateSong()
                    }
                }
                .launchIn(scope)
        }
    }

    fun onDestroy() {
        castPlayer?.let {
            stream.setAltPlayer(null)
            it.release()
        }
    }

    fun initCastButton(menu: Menu?) {
        castPlayer?.let {
            CastButtonFactory.setUpMediaRouteButton(
                context,
                menu,
                R.id.media_route_menu_item
            )
        }
    }

    private fun updateSong() {
        val song = radioViewModel.currentSong

        val metadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK).apply {
            putString(MediaMetadata.KEY_TITLE, song?.titleString)
            putString(MediaMetadata.KEY_ARTIST, song?.artistsString)
        }

        song?.albumArtUrl?.let {
            metadata.addImage(WebImage(Uri.parse(it)))
        }

        // TODO: react to switching between jpop/kpop
        val mediaInfo = MediaInfo.Builder(RadioClient.library?.streamUrl)
            .setStreamType(MediaInfo.STREAM_TYPE_LIVE)
            .setContentType(MimeTypes.AUDIO_UNKNOWN)
            .setMetadata(metadata)
            .build()

        castPlayer?.loadItem(MediaQueueItem.Builder(mediaInfo).build(), 0)
    }
}
