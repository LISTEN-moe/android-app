package me.echeung.moemoekyun.service

import android.content.Context
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Rating
import androidx.media3.common.util.UnstableApi
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
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import logcat.logcat
import me.echeung.moemoekyun.client.api.Station
import me.echeung.moemoekyun.domain.radio.RadioService
import me.echeung.moemoekyun.domain.songs.interactor.FavoriteSong
import me.echeung.moemoekyun.util.PreferenceUtil
import me.echeung.moemoekyun.util.ext.launchIO
import me.echeung.moemoekyun.util.ext.toMediaItem


@OptIn(UnstableApi::class)
class PlaybackServiceSessionCallback @AssistedInject constructor(
    @Assisted private val context: Context,
    private val scope: CoroutineScope,
    private val preferenceUtil: PreferenceUtil,
    private val favoriteSong: FavoriteSong,
    private val radioService: RadioService,
) : MediaLibraryService.MediaLibrarySession.Callback {

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
            val playerCommands = MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS.buildUpon()
                .remove(Player.COMMAND_SEEK_TO_NEXT)
                .remove(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                .remove(Player.COMMAND_SEEK_TO_PREVIOUS)
                .remove(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                .remove(Player.COMMAND_SEEK_BACK)
                .remove(Player.COMMAND_SEEK_FORWARD)
                .build()
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(sessionCommands)
                .setAvailablePlayerCommands(playerCommands)
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
        return Futures.immediateFuture(
            MediaSession.MediaItemsWithStartPosition(
                listOf(mediaItem),
                C.INDEX_UNSET,
                C.TIME_UNSET,
            ),
        )
    }

    override fun onGetLibraryRoot(
        session: MediaLibraryService.MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        params: MediaLibraryService.LibraryParams?,
    ): ListenableFuture<LibraryResult<MediaItem>> {
        logcat { "onGetLibraryRoot request from: ${browser.packageName}, uid: ${browser.uid}" }
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
                MediaLibraryService.LibraryParams.Builder()
                    .build(),
            ),
        )
    }

    override fun onGetChildren(
        session: MediaLibraryService.MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        parentId: String,
        page: Int,
        pageSize: Int,
        params: MediaLibraryService.LibraryParams?,
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        logcat { "onGetChildren request from: ${browser.packageName}, uid: ${browser.uid}" }
        val mediaItems = ImmutableList.copyOf(
            Station.entries.map {
                MediaItem.Builder()
                    .setMediaId(it.name)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(context.resources.getString(it.labelRes))
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
                MediaLibraryService.LibraryParams.Builder().build(),
            ),
        )
    }

    override fun onSetMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: List<MediaItem>,
        startIndex: Int,
        startPositionMs: Long,
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
        logcat { "onSetMediaItems request from: ${controller.packageName}, uid: ${controller.uid}" }
        if (mediaItems.size != 1) {
            return Futures.immediateFailedFuture(UnsupportedOperationException("Only one media item supported"))
        }

        try {
            val requested = mediaItems.first()
            val station = Station.valueOf(requested.mediaId)
            preferenceUtil.station().set(station)
            // Instead of returning a media item here we just set the preference,
            // and the flow will actually set the station as a media item
            return super.onSetMediaItems(mediaSession, controller, mediaItems, startIndex, startPositionMs)
        } catch (e: IllegalArgumentException) {
            return Futures.immediateFailedFuture(e)
        }
    }

    override fun onGetSearchResult(
        session: MediaLibraryService.MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        query: String,
        page: Int,
        pageSize: Int,
        params: MediaLibraryService.LibraryParams?,
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        logcat { "onGetSearchResult request from: ${browser.packageName}, uid: ${browser.uid}" }
        return Futures.immediateFuture(
            LibraryResult.ofItemList(
                ImmutableList.of(),
                MediaLibraryService.LibraryParams.Builder().build(),
            ),
        )
    }

    override fun onSetRating(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaId: String,
        rating: Rating,
    ): ListenableFuture<SessionResult> {
        logcat { "onSetRating request from: ${controller.packageName}, uid: ${controller.uid}" }
        toggleFavoriteSong()
        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
    }

    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle,
    ): ListenableFuture<SessionResult> {
        logcat { "onCustomCommand request from: ${controller.packageName}, uid: ${controller.uid}" }
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

    @AssistedFactory
    interface Factory {
        fun create(context: Context): PlaybackServiceSessionCallback
    }

}
