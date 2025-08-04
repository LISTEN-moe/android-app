package me.echeung.moemoekyun.service

import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import logcat.logcat
import me.echeung.moemoekyun.util.PreferenceUtil
import me.echeung.moemoekyun.util.ext.collectWithUiContext
import me.echeung.moemoekyun.util.ext.launchIO
import me.echeung.moemoekyun.util.system.NetworkUtil
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    val scope = MainScope()

    @Inject
    lateinit var audioAttributes: AudioAttributes

    @Inject
    lateinit var preferenceUtil: PreferenceUtil

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
    }


    override fun onDestroy() {
        session.player.release()
        session.release()
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

}
