package me.echeung.moemoekyun.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
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
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.combine
import logcat.logcat
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.domain.radio.RadioService
import me.echeung.moemoekyun.domain.songs.interactor.FavoriteSong
import me.echeung.moemoekyun.domain.user.interactor.GetAuthenticatedUser
import me.echeung.moemoekyun.ui.MainActivity
import me.echeung.moemoekyun.util.AlbumArtUtil
import me.echeung.moemoekyun.util.PreferenceUtil
import me.echeung.moemoekyun.util.ext.collectWithUiContext
import me.echeung.moemoekyun.util.ext.launchIO
import me.echeung.moemoekyun.util.system.NetworkUtil
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

const val FAVORITE_ACTION_ID = "a_favorite"
const val UNFAVORITE_ACTION_ID = "a_unfavorite"
const val FAVORITE_COMMAND = "a_favorite_command"
const val UNFAVORITE_COMMAND = "a_unfavorite_command"

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

    @Inject
    lateinit var favoriteSong: FavoriteSong

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

        setMediaNotificationProvider(
            DefaultMediaNotificationProvider.Builder(applicationContext)
                .setChannelName(androidx.media3.session.R.string.default_notification_channel_name)
                .setChannelId("default")
                .setNotificationId(1)
                .build()
                .apply {
                    setSmallIcon(R.drawable.ic_icon)
                },
        )

        val favoriteButton =
            CommandButton.Builder(CommandButton.ICON_STAR_UNFILLED)
                .setEnabled(true)
                .setDisplayName("Favorite")
                .setSessionCommand(SessionCommand(FAVORITE_ACTION_ID, Bundle.EMPTY))
                .setSlots(CommandButton.SLOT_BACK)
                .build()
        val unfavoriteButton =
            CommandButton.Builder(CommandButton.ICON_STAR_FILLED)
                .setEnabled(true)
                .setDisplayName("Unfavorite")
                .setSessionCommand(SessionCommand(UNFAVORITE_ACTION_ID, Bundle.EMPTY))
                .setSlots(CommandButton.SLOT_BACK)
                .build()

        val action = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val clickIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            action,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // FIXME: dismiss doesn't work
        // TODO: Investigate if player position can be changed
        // TODO: Investigate if skip next/prev can be remove on Auto
        // FIXME: Handle changing station on Auto
        // TODO: Investigate how to change station on Auto
        session = MediaSession.Builder(applicationContext, player)
            .setSessionActivity(clickIntent)
            .setCallback(
                object : MediaSession.Callback {
                    override fun onConnect(
                        session: MediaSession,
                        controller: MediaSession.ControllerInfo,
                    ): MediaSession.ConnectionResult {
                        val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                            .add(SessionCommand(FAVORITE_ACTION_ID, Bundle.EMPTY))
                            .add(SessionCommand(UNFAVORITE_ACTION_ID, Bundle.EMPTY))
                            .build()
                        return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                            .setAvailableSessionCommands(sessionCommands)
                            .build()
                    }

                    override fun onCustomCommand(
                        session: MediaSession,
                        controller: MediaSession.ControllerInfo,
                        customCommand: SessionCommand,
                        args: Bundle,
                    ): ListenableFuture<SessionResult> {
                        return when (customCommand.customAction) {
                            FAVORITE_ACTION_ID, UNFAVORITE_ACTION_ID -> {
                                scope.launchIO {
                                    radioService.state.value.currentSong?.id?.let {
                                        favoriteSong.await(it)
                                    }
                                }
                                Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                            }
                            else -> super.onCustomCommand(session, controller, customCommand, args)
                        }
                    }
                },
            )
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
                        // play()
                    }
                }
        }

        scope.launchIO {
            combine(
                radioService.state,
                albumArtUtil.flow,
                getAuthenticatedUser.asFlow(),
                preferenceUtil.shouldPreferRomaji().asFlow(),
            ) { radioState, _, _, _ -> radioState }
                .collectWithUiContext { radioState ->
                    val currentSong = radioState.currentSong

                    if (currentSong == null) {
                        session.player.editCurrentMediaItem { currentMediaItem ->
                            setMediaMetadata(MediaMetadata.EMPTY)
                        }
                        return@collectWithUiContext
                    }

                    if (getAuthenticatedUser.isAuthenticated()) {
                        val commandButtons = if (currentSong.favorited) {
                            listOf(unfavoriteButton)
                        } else {
                            listOf(favoriteButton)
                        }
                        session.setMediaButtonPreferences(commandButtons)
                    } else {
                        session.setMediaButtonPreferences(listOf())
                    }

                    session.player.editCurrentMediaItem { currentMediaItem ->
                        val builder = currentMediaItem?.mediaMetadata?.buildUpon() ?: MediaMetadata.Builder()
                        setMediaMetadata(
                            builder.run {
                                setTitle(currentSong.title)
                                setArtist(currentSong.artists)
                                setAlbumTitle(currentSong.albums)
                                setDurationMs(currentSong.durationSeconds.seconds.inWholeMilliseconds)
                                albumArtUtil.getCurrentAlbumArt(500)?.toByteArray()?.let {
                                    setArtworkData(
                                        it,
                                        MediaMetadata.PICTURE_TYPE_FRONT_COVER,
                                    )
                                }
                                build()
                            },
                        )
                    }

                }
        }
    }

    @OptIn(UnstableApi::class)
    override fun onTaskRemoved(rootIntent: Intent?) {
        pauseAllPlayersAndStopSelf()
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
