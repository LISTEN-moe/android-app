package me.echeung.moemoekyun.client.api.socket

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.echeung.moemoekyun.client.model.Event
import me.echeung.moemoekyun.client.model.Song
import me.echeung.moemoekyun.client.model.User

object WebsocketResponseSerializer : JsonContentPolymorphicSerializer<WebsocketResponse>(WebsocketResponse::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<WebsocketResponse> =
        when (val opValue = element.jsonObject["op"]?.jsonPrimitive?.intOrNull) {
            0 -> WebsocketResponse.Connect.serializer()
            1 -> WebsocketResponse.Update.serializer()
            10 -> WebsocketResponse.HeartbeatAck.serializer()
            else -> error("Unknown op value: $opValue")
        }
}

@Serializable(with = WebsocketResponseSerializer::class)
sealed interface WebsocketResponse {

    @Serializable
    data class Connect(val d: Details) : WebsocketResponse {
        @Serializable
        data class Details(val heartbeat: Int, val message: String?, val user: User?)
    }

    @Serializable
    data class Update(val t: String?, val d: Details?) : WebsocketResponse {
        @Serializable
        data class Details(
            val song: Song?,
            val startTime: String?,
            val lastPlayed: List<Song>?,
            val requester: User?,
            val event: Event?,
            val listeners: Int,
        )

        fun isValidUpdate(): Boolean = (
            t == TRACK_UPDATE ||
                t == TRACK_UPDATE_REQUEST ||
                t == QUEUE_UPDATE ||
                isNotification()
            )

        private fun isNotification(): Boolean = t == NOTIFICATION

        companion object {
            private const val TRACK_UPDATE = "TRACK_UPDATE"
            private const val TRACK_UPDATE_REQUEST = "TRACK_UPDATE_REQUEST"
            private const val QUEUE_UPDATE = "QUEUE_UPDATE"
            private const val NOTIFICATION = "NOTIFICATION"
        }
    }

    @Serializable
    data object HeartbeatAck : WebsocketResponse

//    @Serializable
//    data class Notification(
//        override val op: Int,
//        val t: String?,
//        val d: Details?,
//    ) : ResponseModel() {
//
//        @Serializable
//        data class Details(
//            val type: String?,
//        )
//    }
//
//    @Serializable
//    data class EventNotificationResponse(
//        override val op: Int,
//        val t: String?,
//        val d: Details?,
//    ) : ResponseModel() {
//
//        @Serializable
//        data class Details(
//            val type: String?,
//            val event: Event?,
//        )
//
//        companion object {
//            const val TYPE = "EVENT"
//        }
//    }
}

@Serializable
sealed interface WebsocketRequest {

    @Serializable
    data class Update(val op: Int = 2) : WebsocketRequest

    @Serializable
    data class Heartbeat(val op: Int = 9) : WebsocketRequest
}
