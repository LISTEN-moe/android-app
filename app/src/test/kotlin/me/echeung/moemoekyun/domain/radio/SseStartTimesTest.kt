package me.echeung.moemoekyun.domain.radio

import app.cash.turbine.test
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import me.echeung.moemoekyun.client.api.sse.SseMetadata
import org.junit.jupiter.api.Test
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class SseStartTimesTest {

    private val sseFlow = MutableSharedFlow<SseMetadata>()

    private fun sseMetadata(title: String, startedAt: String? = null) =
        SseMetadata(mount = "/stream", title = title, artist = "", startedAt = startedAt)

    @Test
    fun `emits parsed Instant when startedAt is present`() = runTest {
        sseStartTimes(sseFlow).test {
            sseFlow.emit(sseMetadata("Song A", startedAt = "2026-04-18T10:00:00Z"))
            awaitItem() shouldBe Instant.parse("2026-04-18T10:00:00Z")
        }
    }

    @Test
    fun `emits null when startedAt is absent`() = runTest {
        sseStartTimes(sseFlow).test {
            sseFlow.emit(sseMetadata("Song A", startedAt = null))
            awaitItem().shouldBeNull()
        }
    }

    @Test
    fun `repeated SSE events for the same title do not re-emit`() = runTest {
        sseStartTimes(sseFlow).test {
            sseFlow.emit(sseMetadata("Song A", startedAt = "2026-04-18T10:00:00Z"))
            awaitItem()

            sseFlow.emit(sseMetadata("Song A", startedAt = "2026-04-18T10:00:01Z"))
            expectNoEvents()
        }
    }

    @Test
    fun `title change emits new startTime`() = runTest {
        sseStartTimes(sseFlow).test {
            sseFlow.emit(sseMetadata("Song A", startedAt = "2026-04-18T10:00:00Z"))
            awaitItem()

            sseFlow.emit(sseMetadata("Song B", startedAt = "2026-04-18T10:04:00Z"))
            awaitItem() shouldBe Instant.parse("2026-04-18T10:04:00Z")
        }
    }

    @Test
    fun `SSE arrives before WebSocket song change - startTime ready immediately`() = runTest {
        // SSE fires for the new song before the WebSocket announces it. Since the two
        // flows are independent in RadioService, startTime is already correct by the
        // time the UI reflects the new song.
        sseStartTimes(sseFlow).test {
            sseFlow.emit(sseMetadata("Song A", startedAt = "2026-04-18T10:00:00Z"))
            awaitItem()

            sseFlow.emit(sseMetadata("Song B", startedAt = "2026-04-18T10:04:00Z"))
            awaitItem() shouldBe Instant.parse("2026-04-18T10:04:00Z")
        }
    }

    @Test
    fun `WebSocket arrives before SSE - startTime updates on next SSE emission`() = runTest {
        // WebSocket announces the new song first. The SSE collector in RadioService
        // hasn't emitted yet, so startTime holds the previous value until SSE fires.
        sseStartTimes(sseFlow).test {
            sseFlow.emit(sseMetadata("Song A", startedAt = "2026-04-18T10:00:00Z"))
            awaitItem()

            // WebSocket announces Song B here (not modelled — independent flow)
            expectNoEvents()

            sseFlow.emit(sseMetadata("Song B", startedAt = "2026-04-18T10:04:00Z"))
            awaitItem() shouldBe Instant.parse("2026-04-18T10:04:00Z")
        }
    }
}
