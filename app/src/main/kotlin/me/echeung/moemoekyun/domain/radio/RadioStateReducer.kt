package me.echeung.moemoekyun.domain.radio

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import me.echeung.moemoekyun.client.api.socket.Socket
import me.echeung.moemoekyun.client.api.socket.WebsocketResponse
import me.echeung.moemoekyun.client.api.sse.SseMetadata
import me.echeung.moemoekyun.client.model.Event
import me.echeung.moemoekyun.domain.songs.interactor.GetFavoriteSongs
import me.echeung.moemoekyun.domain.songs.model.DomainSong
import me.echeung.moemoekyun.domain.songs.model.SongConverter
import me.echeung.moemoekyun.util.PreferenceUtil
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Merges WebSocket song updates and SSE start-time events into a single [RadioStateUpdate] flow.
 */
@OptIn(ExperimentalTime::class)
@Singleton
class RadioStateReducer @Inject constructor(
    private val songConverter: SongConverter,
    private val getFavoriteSongs: GetFavoriteSongs,
    private val preferenceUtil: PreferenceUtil,
) {

    fun radioStateFlow(socketFlow: Flow<Socket.Result>, sseFlow: Flow<SseMetadata>): Flow<RadioStateUpdate> {
        val socketUpdates = combine(
            socketFlow,
            getFavoriteSongs.asFlow(),
            preferenceUtil.shouldPreferRomaji().asFlow(),
        ) { socketResponse, _, _ -> socketResponse }
            .filterIsInstance<Socket.Result.Response>()

        return mergeSocketAndSse(socketUpdates, sseFlow) { info ->
            info?.song?.let(songConverter::toDomainSong)?.copy(
                favorited = getFavoriteSongs.isFavorite(info.song.id),
            )
        }
    }
}

/**
 * Core combine logic: merges a WebSocket update flow with an SSE start-time flow,
 * converting song data via [convertSong].
 */
@OptIn(ExperimentalTime::class)
@VisibleForTesting
internal fun mergeSocketAndSse(
    socketUpdates: Flow<Socket.Result.Response>,
    sseFlow: Flow<SseMetadata>,
    convertSong: (WebsocketResponse.Update.Details?) -> DomainSong?,
): Flow<RadioStateUpdate> {
    val sseStartTimes = sseFlow
        .map { metadata -> metadata.startedAt?.let(Instant::parse) }
        .onStart { emit(null) }

    return combine(socketUpdates, sseStartTimes) { socketResponse, startTime ->
        val info = socketResponse.info
        RadioStateUpdate(
            currentSong = convertSong(info),
            startTime = startTime,
            listeners = info?.listeners ?: 0,
            requester = info?.requester?.displayName,
            event = info?.event,
        )
    }
}

@OptIn(ExperimentalTime::class)
data class RadioStateUpdate(
    val currentSong: DomainSong? = null,
    val startTime: Instant? = null,
    val listeners: Int = 0,
    val requester: String? = null,
    val event: Event? = null,
)
