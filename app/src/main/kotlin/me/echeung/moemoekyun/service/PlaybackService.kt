package me.echeung.moemoekyun.service

import androidx.annotation.OptIn
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import coil3.Bitmap
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.combine
import logcat.logcat
import me.echeung.moemoekyun.domain.radio.RadioService
import me.echeung.moemoekyun.domain.user.interactor.GetAuthenticatedUser
import me.echeung.moemoekyun.util.AlbumArtUtil
import me.echeung.moemoekyun.util.PreferenceUtil
import me.echeung.moemoekyun.util.ext.collectWithUiContext
import me.echeung.moemoekyun.util.ext.launchIO
import me.echeung.moemoekyun.util.system.NetworkUtil
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    val scope = MainScope()


    @Inject
    lateinit var audioAttributes: AudioAttributes

    @Inject
    lateinit var preferenceUtil: PreferenceUtil

    @Inject
    lateinit var radioService: RadioService

    @Inject
    lateinit var albumArtUtil: AlbumArtUtil

    @Inject
    lateinit var getAuthenticatedUser: GetAuthenticatedUser

    lateinit var session: MediaSession



    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        logcat { "Starting engine :fire:" }

        val dataSourceFactory = DefaultDataSource.Factory(
            applicationContext,
            DefaultHttpDataSource.Factory()
                .setUserAgent(NetworkUtil.userAgent),
        )
        val progressiveMediaSourceFactory =
            ProgressiveMediaSource.Factory(dataSourceFactory, DefaultExtractorsFactory())

        val player = ExoPlayer.Builder(applicationContext)
            .setMediaSourceFactory(progressiveMediaSourceFactory)
            .setAudioAttributes(audioAttributes, true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true,
            )
            .build()

        session = MediaSession.Builder(applicationContext, player)
            .build()

        scope.launchIO {
            preferenceUtil.station()
                .asFlow()
                .collectWithUiContext { station ->
                    with(player) {
                        replaceMediaItem(
                            0,
                            MediaItem.Builder()
                                .setUri(station.streamUrl.toUri())
                                .build(),
                        )
                        prepare()
                        play()
                    }
                }
        }

        scope.launchIO {
            combine(
                radioService.state,
                albumArtUtil.flow,
                getAuthenticatedUser.asFlow(),
                preferenceUtil.shouldPreferRomaji().asFlow()
            ) { radioState, _, _, _ -> radioState }
                .collectWithUiContext { radioState ->
                    val currentSong = radioState.currentSong

                    if (currentSong == null) {
                        session.player.editCurrentMediaItem { currentMediaItem ->
                            setMediaMetadata(MediaMetadata.EMPTY)
                        }
                        return@collectWithUiContext
                    }

                    session.player.editCurrentMediaItem { currentMediaItem ->
                        val builder = currentMediaItem?.mediaMetadata?.buildUpon() ?: MediaMetadata.Builder()
                        setMediaMetadata(
                            builder.run {
                                setTitle(currentSong.title)
                                setArtist(currentSong.artists)
                                setAlbumTitle(currentSong.albums)
                                albumArtUtil.getCurrentAlbumArt(500)?.toByteArray()?.let {
                                    setArtworkData(
                                        it,
                                        MediaMetadata.PICTURE_TYPE_FRONT_COVER
                                    )
                                }
                                build()
                            }
                        )
                    }

                }
        }
    }


    override fun onDestroy() {
        session.player.release()
        session.release()
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

}

fun Player.editCurrentMediaItem(block: MediaItem.Builder.(MediaItem?) -> Unit) {
    val mediaItem = currentMediaItem
    val builder = mediaItem?.buildUpon() ?: MediaItem.Builder()
    block(builder, mediaItem)
    replaceMediaItem(0, builder.build())
}

fun android.graphics.Bitmap.toByteArray(): ByteArray {
    val stream = ByteArrayOutputStream()
    compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}
