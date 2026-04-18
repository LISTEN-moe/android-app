package me.echeung.moemoekyun.client.api.sse

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SseMetadata(
    val mount: String,
    val title: String,
    val artist: String,
    @SerialName("started_at")
    val startedAt: String? = null,
)
