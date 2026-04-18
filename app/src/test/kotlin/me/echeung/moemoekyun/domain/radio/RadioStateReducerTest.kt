package me.echeung.moemoekyun.domain.radio

import app.cash.turbine.test
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import me.echeung.moemoekyun.client.api.socket.Socket
import me.echeung.moemoekyun.client.api.socket.WebsocketResponse
import me.echeung.moemoekyun.client.api.sse.SseMetadata
import me.echeung.moemoekyun.client.model.Song
import me.echeung.moemoekyun.domain.songs.model.DomainSong
import org.junit.jupiter.api.Test
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class RadioStateReducerTest {

    private val socketFlow = MutableSharedFlow<Socket.Result.Response>(replay = 1)
    private val sseFlow = MutableSharedFlow<SseMetadata>(replay = 1)

    private val flow = mergeSocketAndSse(socketFlow, sseFlow) { info ->
        info?.song?.let { song ->
            DomainSong(
                id = song.id,
                title = song.title.orEmpty(),
                artists = null,
                albums = null,
                sources = null,
                duration = "${song.duration}",
                durationSeconds = song.duration.toLong(),
                albumArtUrl = null,
                favorited = false,
                favoritedAtEpoch = null,
            )
        }
    }

    private fun socketResponse(songId: Int, listeners: Int = 100) = Socket.Result.Response(
        info = WebsocketResponse.Update.Details(
            song = Song(id = songId, title = "Song $songId", duration = 240),
            requester = null,
            event = null,
            listeners = listeners,
        ),
    )

    private fun sseMetadata(startedAt: String) = SseMetadata(
        mount = "/stream",
        title = "",
        artist = "",
        startedAt = startedAt,
    )

    @Test
    fun `initial state has null startTime before SSE emits`() = runTest {
        flow.test {
            socketFlow.emit(socketResponse(songId = 1))

            val update = awaitItem()
            update.currentSong?.id shouldBe 1
            update.startTime.shouldBeNull()
        }
    }

    @Test
    fun `SSE updates startTime for current song`() = runTest {
        val time1 = "2026-04-18T10:00:00Z"

        flow.test {
            socketFlow.emit(socketResponse(songId = 1))
            awaitItem()

            sseFlow.emit(sseMetadata(time1))
            val update = awaitItem()
            update.currentSong?.id shouldBe 1
            update.startTime shouldBe Instant.parse(time1)
        }
    }

    @Test
    fun `song change - SSE fires before WebSocket - startTime preserved`() = runTest {
        val time1 = "2026-04-18T10:00:00Z"
        val time2 = "2026-04-18T10:04:00Z"

        flow.test {
            socketFlow.emit(socketResponse(songId = 1))
            awaitItem()
            sseFlow.emit(sseMetadata(time1))
            awaitItem()

            // SSE fires first with new startedAt
            sseFlow.emit(sseMetadata(time2))
            awaitItem().startTime shouldBe Instant.parse(time2)

            // Then WebSocket fires with new song
            socketFlow.emit(socketResponse(songId = 2))
            val update = awaitItem()
            update.currentSong?.id shouldBe 2
            update.startTime.shouldNotBeNull()
            update.startTime shouldBe Instant.parse(time2)
        }
    }

    @Test
    fun `song change - WebSocket fires before SSE - startTime updates when SSE arrives`() = runTest {
        val time1 = "2026-04-18T10:00:00Z"
        val time2 = "2026-04-18T10:04:00Z"

        flow.test {
            socketFlow.emit(socketResponse(songId = 1))
            awaitItem()
            sseFlow.emit(sseMetadata(time1))
            awaitItem()

            // WebSocket fires first with new song
            socketFlow.emit(socketResponse(songId = 2))
            val afterWs = awaitItem()
            afterWs.currentSong?.id shouldBe 2
            // startTime still holds old value — stale but will be overwritten
            afterWs.startTime shouldBe Instant.parse(time1)

            // Then SSE delivers new start time
            sseFlow.emit(sseMetadata(time2))
            val afterSse = awaitItem()
            afterSse.currentSong?.id shouldBe 2
            afterSse.startTime shouldBe Instant.parse(time2)
        }
    }

    @Test
    fun `repeated WebSocket emissions for same song preserve startTime`() = runTest {
        val time1 = "2026-04-18T10:00:00Z"

        flow.test {
            socketFlow.emit(socketResponse(songId = 1))
            awaitItem()
            sseFlow.emit(sseMetadata(time1))
            awaitItem()

            // WebSocket re-emits (e.g. listener count update)
            socketFlow.emit(socketResponse(songId = 1, listeners = 101))
            val update = awaitItem()
            update.listeners shouldBe 101
            update.startTime shouldBe Instant.parse(time1)
        }
    }

    @Test
    fun `null song in WebSocket response produces null currentSong`() = runTest {
        flow.test {
            socketFlow.emit(Socket.Result.Response(info = null))

            val update = awaitItem()
            update.currentSong.shouldBeNull()
            update.listeners shouldBe 0
        }
    }

    @Test
    fun `SSE metadata without startedAt keeps startTime null`() = runTest {
        flow.test {
            socketFlow.emit(socketResponse(songId = 1))
            awaitItem()

            sseFlow.emit(SseMetadata(mount = "/stream", title = "", artist = "", startedAt = null))
            val update = awaitItem()
            update.currentSong?.id shouldBe 1
            update.startTime.shouldBeNull()
        }
    }

    @Test
    fun `listeners and requester are passed through`() = runTest {
        flow.test {
            socketFlow.emit(
                Socket.Result.Response(
                    info = WebsocketResponse.Update.Details(
                        song = Song(id = 1, title = "Test", duration = 200),
                        requester = me.echeung.moemoekyun.client.model.User(
                            uuid = "abc",
                            displayName = "DJ Test",
                            avatarImage = null,
                            bannerImage = null,
                        ),
                        event = null,
                        listeners = 42,
                    ),
                ),
            )

            val update = awaitItem()
            update.listeners shouldBe 42
            update.requester shouldBe "DJ Test"
        }
    }
}
