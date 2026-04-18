package me.echeung.moemoekyun.client.api.sse

import kotlinx.serialization.json.Json
import me.echeung.moemoekyun.client.api.Station
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RadioSse @Inject constructor(override val okHttpClient: OkHttpClient, private val json: Json) :
    SseClient<SseMetadata>() {

    override val eventType = EVENT_METADATA

    @Volatile
    private var currentStation: Station? = null

    fun connect(station: Station) {
        currentStation = station
        connect(SSE_URL)
    }

    override fun parse(data: String): SseMetadata? {
        val metadata = json.decodeFromString<SseMetadata>(data)
        return metadata.takeIf { it.mount in (currentStation?.sseMounts ?: emptyList()) }
    }
}

private const val SSE_URL = "https://listen.moe/metadata"
private const val EVENT_METADATA = "metadata"
