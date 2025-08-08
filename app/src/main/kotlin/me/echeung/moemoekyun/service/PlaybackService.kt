package me.echeung.moemoekyun.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.domain.radio.RadioService
import me.echeung.moemoekyun.domain.user.interactor.GetAuthenticatedUser
import me.echeung.moemoekyun.ui.MainActivity
import me.echeung.moemoekyun.util.AlbumArtUtil
import me.echeung.moemoekyun.util.PreferenceUtil
import me.echeung.moemoekyun.util.ext.collectWithUiContext
import me.echeung.moemoekyun.util.ext.editCurrentMediaItem
import me.echeung.moemoekyun.util.ext.launchIO
import me.echeung.moemoekyun.util.ext.toMediaItem
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

const val FAVORITE_ACTION_ID = "action_favorite"
const val UNFAVORITE_ACTION_ID = "action_unfavorite"

@AndroidEntryPoint
@OptIn(UnstableApi::class)
class PlaybackService : MediaLibraryService() {

    @Inject
    lateinit var scope: CoroutineScope

    @Inject
    lateinit var preferenceUtil: PreferenceUtil

    @Inject
    lateinit var radioService: RadioService

    @Inject
    lateinit var albumArtUtil: AlbumArtUtil

    @Inject
    lateinit var getAuthenticatedUser: GetAuthenticatedUser

    @Inject
    lateinit var player: PlaybackPlayer

    @Inject
    lateinit var playbackServicePlayerListenerFactory: PlaybackServicePlayerListener.Factory

    @Inject
    lateinit var playbackServiceSessionCallbackFactory: PlaybackServiceSessionCallback.Factory

    @Inject
    lateinit var playbackServiceSessionListener: PlaybackServiceSessionListener

    lateinit var session: MediaLibrarySession

    override fun onCreate() {
        super.onCreate()

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

        setListener(playbackServiceSessionListener)

        // TODO: Investigate if player position can be changed
        // TODO: Investigate if skip next/prev can be remove on Auto
        player.addListener(playbackServicePlayerListenerFactory.create(player))
        session =
            MediaLibrarySession.Builder(this, player, playbackServiceSessionCallbackFactory.create(applicationContext))
                .setSessionActivity(clickIntent)
                .build()

        scope.launchIO {
            preferenceUtil.station()
                .asFlow()
                .collectWithUiContext { station ->
                    with(player) {
                        replaceMediaItem(
                            0,
                            station.toMediaItem(),
                        )
                        prepare()
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
                            setMediaMetadata(
                                MediaMetadata.Builder()
                                    .setTitle("「no name」")
                                    .setArtist(resources.getString(R.string.app_name))
                                    .build(),
                            )
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
                                albumArtUtil.getCurrentAlbumArt(500)?.let {
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

    override fun onTaskRemoved(rootIntent: Intent?) {
        pauseAllPlayersAndStopSelf()
    }

    override fun onDestroy() {
        session.player.release()
        session.release()
        scope.cancel()
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? = session

}
