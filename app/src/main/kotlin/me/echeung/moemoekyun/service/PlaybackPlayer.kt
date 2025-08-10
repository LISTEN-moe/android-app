@file:kotlin.OptIn(ExperimentalTime::class)

package me.echeung.moemoekyun.service

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import logcat.logcat
import me.echeung.moemoekyun.domain.radio.interactor.CurrentSong
import me.echeung.moemoekyun.util.ext.launchIO
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.toDuration

@OptIn(UnstableApi::class)
class PlaybackPlayer @Inject constructor(
    val player: ExoPlayer,
    val currentSong: CurrentSong,
    val scope: CoroutineScope,
) : ForwardingPlayer(player) {

    private var currentStartTime: Instant? = null
    private var currentDuration = 0L

    init {
        scope.launchIO {
            currentSong.songProgressFlow()
                .collectLatest { (startTime, duration) ->
                    currentStartTime = startTime
                    currentDuration = duration ?: 0L
                }
        }
    }

    override fun play() {
        logcat { "will seek to default position and start playing" }
        player.seekToDefaultPosition()
        super.play()
    }

    // TODO: this doesn't get reflected in things like the system UI for some reason
    override fun getCurrentPosition(): Long = currentStartTime?.let {
        val currentPosition = Clock.System.now() - it
        logcat { "current position: $currentPosition" }
        currentPosition.inWholeMilliseconds
    } ?: super.currentPosition

    override fun getDuration(): Long {
        val duration = currentDuration.seconds.inWholeMilliseconds.takeIf { it > 0 } ?: C.TIME_UNSET
        logcat { "current duration: ${duration.toDuration(DurationUnit.MILLISECONDS)}" }
        return duration
    }
}
