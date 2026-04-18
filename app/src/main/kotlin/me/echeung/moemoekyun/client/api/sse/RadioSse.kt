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
class RadioSse @Inject constructor(private val okHttpClient: OkHttpClient, private val json: Json) :
    EventSourceListener() {

    private val _flow = MutableSharedFlow<SseMetadata>(replay = 1)
    val flow = _flow.asSharedFlow()

    @Volatile
    private var eventSource: EventSource? = null
    private val lock = Any()

    @Volatile
    private var currentStation: Station? = null

    fun connect(station: Station) {
        synchronized(lock) {
            eventSource?.cancel()
            currentStation = station
            val request = Request.Builder().url(SSE_URL).build()
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
        if (type != EVENT_METADATA) return
        logcat { "SSE event: $data" }

        try {
            val metadata = json.decodeFromString<SseMetadata>(data)
            if (metadata.mount !in (currentStation?.sseMounts ?: return)) return
            _flow.tryEmit(metadata)
        } catch (e: Exception) {
            logcat(LogPriority.ERROR) { "Failed to parse SSE data: $data ${e.asLog()}" }
        }
    }

    override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
        logcat(LogPriority.ERROR) { "SSE failure: ${t?.asLog()}" }
        // OkHttp EventSource does not auto-reconnect; reconnect is driven by RadioService
    }
}

private const val SSE_URL = "https://listen.moe/metadata"
private const val EVENT_METADATA = "metadata"
