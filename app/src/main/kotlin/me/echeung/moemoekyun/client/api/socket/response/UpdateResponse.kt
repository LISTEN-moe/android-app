package me.echeung.moemoekyun.client.api.socket.response

import kotlinx.serialization.Serializable
import me.echeung.moemoekyun.client.api.model.Event
import me.echeung.moemoekyun.client.api.model.Song
import me.echeung.moemoekyun.client.api.model.User

@Serializable
class UpdateResponse : BaseResponse() {
    val t: String? = null
    val d: Details? = null

    @Serializable
    data class Details(
        val song: Song?,
        val startTime: String?,
        val lastPlayed: List<Song>?,
        val listeners: Int = 0,
        val requester: User? = null,
        val event: Event? = null,
    )

    fun isValidUpdate(): Boolean =
        t == TRACK_UPDATE || t == TRACK_UPDATE_REQUEST || t == QUEUE_UPDATE || isNotification()

    fun isNotification(): Boolean = t == NOTIFICATION
}

private const val TRACK_UPDATE = "TRACK_UPDATE"
private const val TRACK_UPDATE_REQUEST = "TRACK_UPDATE_REQUEST"
private const val QUEUE_UPDATE = "QUEUE_UPDATE"
private const val NOTIFICATION = "NOTIFICATION"