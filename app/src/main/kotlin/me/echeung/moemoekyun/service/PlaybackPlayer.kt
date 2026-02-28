@file:kotlin.OptIn(ExperimentalTime::class)

package me.echeung.moemoekyun.service

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.ForwardingSimpleBasePlayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import logcat.logcat
import me.echeung.moemoekyun.domain.radio.interactor.CurrentSong
import me.echeung.moemoekyun.util.ext.launchIO
import me.echeung.moemoekyun.util.ext.withUIContext
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(UnstableApi::class)
class PlaybackPlayer @Inject constructor(
    player: Player,
    scope: CoroutineScope,
    currentSong: CurrentSong,
) : ForwardingSimpleBasePlayer(player) {

    @Volatile private var currentStartTime: Instant? = null
    @Volatile private var currentDuration = 0L

    init {
        scope.launchIO {
            currentSong.songProgressFlow()
                .collectLatest { (startTime, duration) ->
                    currentStartTime = startTime
                    currentDuration = duration ?: 0L
                    withUIContext {
                        invalidateState()
                    }
                }
        }
    }

    override fun getState(): State {
        val state = super.getState()

        val positionSupplier = currentStartTime?.let {
            val elapsedMs = (Clock.System.now() - it).inWholeMilliseconds
            PositionSupplier.getExtrapolating(elapsedMs, 1f)
        } ?: PositionSupplier.getConstant(C.TIME_UNSET)

        val playlist = state.getPlaylist()
        val updatedPlaylist = playlist.mapIndexed { index, mediaItemData ->
            if (index == state.currentMediaItemIndex) {
                val durationUs = if (currentDuration > 0) {
                    currentDuration.seconds.inWholeMicroseconds
                } else {
                    C.TIME_UNSET
                }
                mediaItemData.buildUpon()
                    .setDurationUs(durationUs)
                    // Pretend this is a non-live, seekable item so the system UI renders a progress
                    // scrubber. Without this, Android suppresses position entirely for live streams.
                    // Actual seeking is blocked via stripped player commands in the session callback.
                    .setLiveConfiguration(null)
                    .setIsSeekable(true)
                    .build()
            } else {
                mediaItemData
            }
        }

        return state.buildUpon()
            .setPlaylist(updatedPlaylist)
            .setContentPositionMs(positionSupplier)
            .build()
    }

    override fun handleSetPlayWhenReady(playWhenReady: Boolean): ListenableFuture<*> {
        // Seek to the live edge when starting fresh, but not when resuming from pause —
        // otherwise resuming will restart the stream from the beginning.
        if (playWhenReady && (playbackState == STATE_IDLE || playbackState == STATE_ENDED)) {
            logcat { "will seek to default position and start playing" }
            player.seekToDefaultPosition()
        }
        return super.handleSetPlayWhenReady(playWhenReady)
    }
}
