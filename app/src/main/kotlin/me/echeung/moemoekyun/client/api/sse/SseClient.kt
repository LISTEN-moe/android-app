package me.echeung.moemoekyun.client.api.sse

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import logcat.LogPriority
import logcat.asLog
import logcat.logcat
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources

abstract class SseClient<T> : EventSourceListener() {

    protected abstract val okHttpClient: OkHttpClient
    protected abstract val eventType: String

    private val _flow = MutableSharedFlow<T>(replay = 1)
    val flow = _flow.asSharedFlow()

    @Volatile
    private var eventSource: EventSource? = null
    private val lock = Any()

    protected fun connect(url: String) {
        synchronized(lock) {
            eventSource?.cancel()
            eventSource = EventSources.createFactory(okHttpClient)
                .newEventSource(Request.Builder().url(url).build(), this)
        }
    }

    fun disconnect() {
        synchronized(lock) {
            eventSource?.cancel()
            eventSource = null
        }
    }

    protected abstract fun parse(data: String): T?

    override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
        if (type != eventType) return
        logcat { "SSE event [$eventType]: $data" }

        try {
            parse(data)?.let { _flow.tryEmit(it) }
        } catch (e: Exception) {
            logcat(LogPriority.ERROR) { "Failed to parse SSE data [$eventType]: $data ${e.asLog()}" }
        }
    }

    override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
        logcat(LogPriority.ERROR) { "SSE failure [$eventType]: ${t?.asLog()}" }
        // OkHttp EventSource does not auto-reconnect; reconnect is driven by RadioService
    }
}
