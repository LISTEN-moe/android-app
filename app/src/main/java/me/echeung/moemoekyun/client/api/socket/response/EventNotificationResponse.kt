package me.echeung.moemoekyun.client.api.socket.response

import me.echeung.moemoekyun.client.model.Event

class EventNotificationResponse : NotificationResponse() {
    override val d: Details? = null

    class Details : NotificationResponse.Details() {
        val event: Event? = null
    }

    companion object {
        const val TYPE = "EVENT"
    }
}
