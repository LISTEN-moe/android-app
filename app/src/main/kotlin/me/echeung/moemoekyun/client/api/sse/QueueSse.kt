package me.echeung.moemoekyun.client.api.sse

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.json.Json
import logcat.LogPriority
import logcat.asLog
import logcat.logcat
import me.echeung.moemoekyun.client.api.Station
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QueueSse @Inject constructor(private val okHttpClient: OkHttpClient, private val json: Json) :
    EventSourceListener() {

    private val _flow = MutableSharedFlow<SseQueue>(replay = 1)
    val flow = _flow.asSharedFlow()

    @Volatile
    private var eventSource: EventSource? = null
    private val lock = Any()

    fun connect(station: Station) {
        synchronized(lock) {
            eventSource?.cancel()
            val url = "$SSE_QUEUE_URL?service=${station.sseService}"
            val request = Request.Builder().url(url).build()
            eventSource = EventSources.createFactory(okHttpClient)
                .newEventSource(request, this)
        }
    }

    fun disconnect() {
        synchronized(lock) {
            eventSource?.cancel()
            eventSource = null
        }
    }

    override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
        if (type != EVENT_QUEUE) return
        logcat { "Queue SSE event: $data" }

        try {
            _flow.tryEmit(json.decodeFromString<SseQueue>(data))
        } catch (e: Exception) {
            logcat(LogPriority.ERROR) { "Failed to parse queue SSE data: $data ${e.asLog()}" }
        }
    }

    override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
        logcat(LogPriority.ERROR) { "Queue SSE failure: ${t?.asLog()}" }
        // OkHttp EventSource does not auto-reconnect; reconnect is driven by RadioService
    }
}

private const val SSE_QUEUE_URL = "https://listen.moe/sse/queue"
private const val EVENT_QUEUE = "queue"
