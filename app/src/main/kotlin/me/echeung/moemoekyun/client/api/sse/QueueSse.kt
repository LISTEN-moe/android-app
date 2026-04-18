package me.echeung.moemoekyun.client.api.sse

import kotlinx.serialization.json.Json
import me.echeung.moemoekyun.client.api.Station
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QueueSse @Inject constructor(override val okHttpClient: OkHttpClient, private val json: Json) :
    SseClient<SseQueue>() {

    override val eventType = EVENT_QUEUE

    fun connect(station: Station) = connect("$SSE_QUEUE_URL?service=${station.sseService}")

    override fun parse(data: String): SseQueue = json.decodeFromString(data)
}

private const val SSE_QUEUE_URL = "https://listen.moe/sse/queue"
private const val EVENT_QUEUE = "queue"
