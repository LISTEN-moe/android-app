package me.echeung.moemoekyun.domain.radio

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import me.echeung.moemoekyun.client.api.socket.Socket
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
 * Transforms the raw WebSocket flow into [SongUpdate] values, re-emitting whenever
 * the song, favorites, or romaji preference changes.
 */
@OptIn(ExperimentalTime::class)
@Singleton
class SongUpdateMapper @Inject constructor(
    private val songConverter: SongConverter,
    private val getFavoriteSongs: GetFavoriteSongs,
    private val preferenceUtil: PreferenceUtil,
) {

    fun flow(socketFlow: Flow<Socket.Result>): Flow<SongUpdate> = combine(
        socketFlow,
        getFavoriteSongs.asFlow(),
        preferenceUtil.shouldPreferRomaji().asFlow(),
    ) { socketResponse, _, _ -> socketResponse }
        .filterIsInstance<Socket.Result.Response>()
        .map { socketResponse ->
            val info = socketResponse.info
            SongUpdate(
                currentSong = info?.song?.let(songConverter::toDomainSong)?.copy(
                    favorited = getFavoriteSongs.isFavorite(info.song.id),
                ),
                listeners = info?.listeners ?: 0,
                requester = info?.requester?.displayName,
                event = info?.event,
            )
        }
}

/**
 * Maps the SSE metadata stream to song start times, deduplicated by title.
 *
 * SSE is the sole authority on [RadioState.startTime]. Deduplicating by title ensures
 * [startTime] only resets when the song actually changes.
 */
@OptIn(ExperimentalTime::class)
internal fun sseStartTimes(sseFlow: Flow<SseMetadata>): Flow<Instant?> = sseFlow
    .distinctUntilChangedBy { it.title }
    .map { metadata -> metadata.startedAt?.let(Instant::parse) }

@OptIn(ExperimentalTime::class)
data class SongUpdate(
    val currentSong: DomainSong? = null,
    val listeners: Int = 0,
    val requester: String? = null,
    val event: Event? = null,
)
