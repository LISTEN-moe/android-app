package me.echeung.moemoekyun.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Rating
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaConstants
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.combine
import logcat.LogPriority
import logcat.asLog
import logcat.logcat
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.client.api.Station
import me.echeung.moemoekyun.domain.radio.RadioService
import me.echeung.moemoekyun.domain.songs.interactor.FavoriteSong
import me.echeung.moemoekyun.domain.user.interactor.GetAuthenticatedUser
import me.echeung.moemoekyun.ui.MainActivity
import me.echeung.moemoekyun.util.AlbumArtUtil
import me.echeung.moemoekyun.util.PreferenceUtil
import me.echeung.moemoekyun.util.ext.collectWithUiContext
import me.echeung.moemoekyun.util.ext.launchIO
import me.echeung.moemoekyun.util.system.NetworkUtil
import javax.inject.Inject

const val FAVORITE_ACTION_ID = "a_favorite"
const val UNFAVORITE_ACTION_ID = "a_unfavorite"

@AndroidEntryPoint
@OptIn(UnstableApi::class)
class PlaybackService : MediaLibraryService() {

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

    lateinit var session: MediaLibrarySession

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

        val audioPlayer = ExoPlayer.Builder(applicationContext)
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
        val player = PlaybackPlayer(audioPlayer)
        player.addListener(PlaybackServicePlayerListener())
        session = MediaLibrarySession.Builder(this, player, PlaybackServiceSessionCallback())
            .setSessionActivity(clickIntent)
            .build()
        setListener(PlaybackServiceSessionListener())
        scope.launchIO {
            preferenceUtil.station()
                .asFlow()
                .collectWithUiContext { station ->
                    with(audioPlayer) {
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
                                    .setTitle("<no name>")
                                    .setArtist("Listen MOE")
                                    .build()
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
                                // setDurationMs(currentSong.durationSeconds.seconds.inWholeMilliseconds)
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
        logcat { "onTaskRemoved" }
        pauseAllPlayersAndStopSelf()
    }

    override fun onDestroy() {
        session.player.release()
        session.release()
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? = session

    inner class PlaybackServicePlayerListener : Player.Listener {
        override fun onPlayerError(error: PlaybackException) {
            logcat(LogPriority.ERROR) { "An error ocurred in the player.\n\n" + error.asLog() }
            val wasPlaying = session.player.isPlaying

            val mediaItem = preferenceUtil.station().get().toMediaItem()
            session.player.setMediaItem(mediaItem)
            session.player.prepare()
            if (wasPlaying) {
                session.player.play()
            }
        }
    }

    inner class PlaybackServiceSessionListener : Listener {
        override fun onForegroundServiceStartNotAllowedException() {
            logcat { "Couldn't start foreground service." }
            super.onForegroundServiceStartNotAllowedException()
        }
    }

    inner class PlaybackServiceSessionCallback : MediaLibrarySession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
        ): MediaSession.ConnectionResult {
            logcat { "onConnect request from: ${controller.packageName}, uid: ${controller.uid}" }
            if (
                session.isMediaNotificationController(controller) ||
                session.isAutomotiveController(controller) ||
                session.isAutoCompanionController(controller)
            ) {
                val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_AND_LIBRARY_COMMANDS.buildUpon()
                    .add(SessionCommand(FAVORITE_ACTION_ID, Bundle.EMPTY))
                    .add(SessionCommand(UNFAVORITE_ACTION_ID, Bundle.EMPTY))
                    .build()
                return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                    .setAvailableSessionCommands(sessionCommands)
                    .build()
            }
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session).build()
        }

        override fun onPlaybackResumption(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
            logcat { "onPlackbackResumption request from: ${controller.packageName}, uid: ${controller.uid}" }
            val mediaItem = preferenceUtil.station().get().toMediaItem()
            return Futures.immediateFuture(MediaSession.MediaItemsWithStartPosition(listOf(mediaItem), C.INDEX_UNSET, C.TIME_UNSET))
        }

        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?,
        ): ListenableFuture<LibraryResult<MediaItem>> {
            logcat { "onGetLibraryRoot request from: ${browser.packageName}, uid: ${browser.uid}" }
            val rootExtras = Bundle().apply {
                putBoolean(CONTENT_STYLE_SUPPORTED, true)
                putInt(CONTENT_STYLE_BROWSABLE_HINT, CONTENT_STYLE_GRID)
                putInt(CONTENT_STYLE_PLAYABLE_HINT, CONTENT_STYLE_LIST)
            }
            val rootMediaItem = MediaItem.Builder()
                .setMediaId("media_root")
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setIsBrowsable(true)
                        .setIsPlayable(false)
                        .build(),
                )
                .build()
            return Futures.immediateFuture(
                LibraryResult.ofItem(
                    rootMediaItem,
                    LibraryParams.Builder()
                        .setExtras(rootExtras)
                        .build(),
                ),
            )
        }

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?,
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            logcat { "onGetChildren request from: ${browser.packageName}, uid: ${browser.uid}" }
            val mediaItems = ImmutableList.copyOf(
                Station.entries.map {
                    MediaItem.Builder()
                        .setMediaId(it.name)
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(resources.getString(it.labelRes))
                                .setIsBrowsable(false)
                                .setIsPlayable(true)
                                .build(),
                        )
                        .build()
                },
            )
            return Futures.immediateFuture(
                LibraryResult.ofItemList(
                    mediaItems,
                    LibraryParams.Builder().build(),
                ),
            )
        }

        override fun onGetItem(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String
        ): ListenableFuture<LibraryResult<MediaItem>> {
            logcat { "onGetItem request from: ${browser.packageName}, uid: ${browser.uid}" }
            return super.onGetItem(session, browser, mediaId)
        }

        override fun onSearch(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<Void>> {
            logcat { "onSearch request from: ${browser.packageName}, uid: ${browser.uid}" }
            return Futures.immediateFuture(LibraryResult.ofVoid())
        }

        override fun onGetSearchResult(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            logcat { "onGetSearchResult request from: ${browser.packageName}, uid: ${browser.uid}" }
            return Futures.immediateFuture(LibraryResult.ofItemList(ImmutableList.of(), LibraryParams.Builder().build()))
        }

        override fun onSetRating(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaId: String,
            rating: Rating
        ): ListenableFuture<SessionResult> {
            toggleFavoriteSong()
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle,
        ): ListenableFuture<SessionResult> {
            val mediaId = args.getString(MediaConstants.EXTRA_KEY_MEDIA_ID)
            if (mediaId != null) {
                println(mediaId)
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
            return when (customCommand.customAction) {
                FAVORITE_ACTION_ID, UNFAVORITE_ACTION_ID -> {
                    toggleFavoriteSong()
                    Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }

                else -> Futures.immediateFuture(SessionResult(SessionError.ERROR_NOT_SUPPORTED));
            }
        }

        fun toggleFavoriteSong() {
            scope.launchIO {
                radioService.state.value.currentSong?.id?.let {
                    favoriteSong.await(it)
                }
            }
        }
    }

}

private const val CONTENT_STYLE_BROWSABLE_HINT = "android.media.browse.CONTENT_STYLE_BROWSABLE_HINT"
private const val CONTENT_STYLE_PLAYABLE_HINT = "android.media.browse.CONTENT_STYLE_PLAYABLE_HINT"
private const val CONTENT_STYLE_SUPPORTED = "android.media.browse.CONTENT_STYLE_SUPPORTED"
private const val CONTENT_STYLE_LIST = 1
private const val CONTENT_STYLE_GRID = 2

fun Station.toMediaItem() = MediaItem.Builder()
    .setUri(streamUrl)
    .setMediaId(name)
    .build()

fun Player.editCurrentMediaItem(block: MediaItem.Builder.(MediaItem?) -> Unit) {
    val mediaItem = currentMediaItem
    val builder = mediaItem?.buildUpon() ?: MediaItem.Builder()
    block(builder, mediaItem)
    replaceMediaItem(0, builder.build())
}
