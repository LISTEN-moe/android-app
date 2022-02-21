package me.echeung.moemoekyun.client.api.socket.response

import kotlinx.serialization.Serializable
import me.echeung.moemoekyun.client.api.model.Event

@Serializable
class EventNotificationResponse : BaseResponse() {
    val t: String? = null
    val d: Details? = null

    @Serializable
    data class Details(
        val type: String,
        val event: Event
    )

    companion object {
        const val TYPE = "EVENT"
    }
}
