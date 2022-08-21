package me.echeung.moemoekyun.client.api.socket

import kotlinx.serialization.Serializable
import me.echeung.moemoekyun.client.model.Event
import me.echeung.moemoekyun.client.model.Song
import me.echeung.moemoekyun.client.model.User

@Serializable
sealed class ResponseModel {
    abstract val op: Int

    @Serializable
    data class Base(
        override val op: Int,
    ) : ResponseModel()

    @Serializable
    data class Connect(
        override val op: Int,
        val d: Details?,
    ) : ResponseModel() {

        @Serializable
        data class Details(
            val heartbeat: Int = 0,
            val message: String?,
            val user: User?,
        )
    }

    @Serializable
    data class Update(
        override val op: Int,
        val t: String?,
        val d: Details?,
    ) : ResponseModel() {

        @Serializable
        data class Details(
            val song: Song?,
            val startTime: String?,
            val lastPlayed: List<Song>?,
            val requester: User?,
            val event: Event?,
            val listeners: Int = 0,
        )
    }

    @Serializable
    data class Notification(
        override val op: Int,
        val t: String?,
        val d: Details?,
    ) : ResponseModel() {

        @Serializable
        data class Details(
            val type: String?,
        )
    }

    @Serializable
    data class EventNotificationResponse(
        override val op: Int,
        val t: String?,
        val d: Details?,
    ) : ResponseModel() {

        @Serializable
        data class Details(
            val type: String?,
            val event: Event?,
        )

        companion object {
            const val TYPE = "EVENT"
        }
    }
}
