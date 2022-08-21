package me.echeung.moemoekyun.cast

import android.content.Context
import android.net.Uri
import android.view.Menu
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaMetadata
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.ext.cast.SessionAvailabilityListener
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.gms.cast.framework.CastContext
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.client.RadioClient
import me.echeung.moemoekyun.client.api.socket.Socket
import me.echeung.moemoekyun.client.model.Song
import me.echeung.moemoekyun.client.stream.Stream
import me.echeung.moemoekyun.viewmodel.RadioViewModel

class CastDelegate(
    context: Context,
    private val radioViewModel: RadioViewModel,
    private val stream: Stream,
    private val socket: Socket,
) {

    private val scope = MainScope()

    private val castPlayer: CastPlayer? = try {
        CastPlayer(CastContext.getSharedInstance(context))
    } catch (e: Exception) {
        null
    }
    private var castStreamPlayer: CastStreamPlayer? = null

    private var song: Song? = null

    init {
        castPlayer?.let { player ->
            castStreamPlayer = CastStreamPlayer(player)

            player.setSessionAvailabilityListener(
                object : SessionAvailabilityListener {
                    override fun onCastSessionAvailable() {
                        stream.setAltPlayer(castStreamPlayer)
                        updateSong()
                    }

                    override fun onCastSessionUnavailable() {
                        stream.setAltPlayer(null)
                    }
                },
            )

            merge(socket.channel.asFlow(), stream.channel.asFlow())
                .distinctUntilChanged()
                .onEach { updateSong() }
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
        // TODO: enable this when the implementation isn't super janky
        menu?.findItem(R.id.media_route_menu_item)?.isVisible = false

//        castPlayer?.let {
//            CastButtonFactory.setUpMediaRouteButton(
//                context,
//                menu,
//                R.id.media_route_menu_item
//            )
//        }
    }

    private fun updateSong() {
        if (song == radioViewModel.currentSong) {
            return
        }

        val song = radioViewModel.currentSong!!

        val metadataBuilder = MediaMetadata.Builder()
            .setTitle(song.titleString)
            .setArtist(song.artistsString)

        song.albumArtUrl?.let {
            metadataBuilder.setArtworkUri(Uri.parse(it))
        }

        val item = MediaItem.Builder()
            .setUri(RadioClient.library.streamUrl)
            .setMimeType(MimeTypes.AUDIO_UNKNOWN)
            .setMediaMetadata(metadataBuilder.build())
            .build()

        castPlayer?.addMediaItem(0, item)
    }
}
