package me.echeung.moemoekyun.ui.screen.home

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.common.listen
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util.handlePlayPauseButtonAction
import androidx.media3.common.util.Util.shouldEnablePlayPauseButton
import androidx.media3.common.util.Util.shouldShowPlayButton
import me.echeung.moemoekyun.R

@Composable
internal fun SongFavoriteIconButton(
    songId: Int?,
    favorited: Boolean,
    onToggleFavorite: ((Int) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = { songId?.let { onToggleFavorite?.invoke(it) } },
        enabled = songId != null && onToggleFavorite != null,
        modifier = modifier,
    ) {
        if (favorited) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = stringResource(R.string.action_unfavorite),
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = stringResource(R.string.action_favorite),
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
internal fun PlayerCircularPlayPauseButton(
    playPauseButtonState: PlayPauseButtonState,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
) {
    FloatingActionButton(
        onClick = playPauseButtonState::onClick,
        modifier = modifier.size(size),
        shape = CircleShape,
//        enabled = playPauseButtonState.isEnabled,
    ) {
        PlayStateIcon(playPauseButtonState)
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun PlayStateIcon(playPauseButtonState: PlayPauseButtonState) {
    when {
        playPauseButtonState.isBuffering -> {
            CircularProgressIndicator(
                modifier = Modifier.alpha(0.7f),
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }

        playPauseButtonState.showPlay -> {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = stringResource(R.string.action_play),
            )
        }

        !playPauseButtonState.showPlay -> {
            Icon(
                Icons.Outlined.Pause,
                contentDescription = stringResource(R.string.action_pause),
            )
        }
    }
}

@UnstableApi
@Composable
fun rememberPlayPauseButtonState(player: Player?): PlayPauseButtonState {
    val playPauseButtonState = remember(player) { PlayPauseButtonState(player) }
    LaunchedEffect(player) { playPauseButtonState.observe() }
    return playPauseButtonState
}

@UnstableApi
class PlayPauseButtonState(private val player: Player?) {
    var isEnabled by mutableStateOf(shouldEnablePlayPauseButton(player))
        private set

    var showPlay by mutableStateOf(shouldShowPlayButton(player))
        private set

    var isBuffering by mutableStateOf(player?.playbackState == Player.STATE_BUFFERING)

    fun onClick() {
        handlePlayPauseButtonAction(player)
    }

    suspend fun observe(): Nothing? = player?.listen { events ->
        if (
            events.containsAny(
                Player.EVENT_PLAYBACK_STATE_CHANGED,
                Player.EVENT_PLAY_WHEN_READY_CHANGED,
                Player.EVENT_AVAILABLE_COMMANDS_CHANGED,
            )
        ) {
            showPlay = shouldShowPlayButton(this)
            isEnabled = shouldEnablePlayPauseButton(this)
            isBuffering = playbackState == Player.STATE_BUFFERING
        }
    }
}
