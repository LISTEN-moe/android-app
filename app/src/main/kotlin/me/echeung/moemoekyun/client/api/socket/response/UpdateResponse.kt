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
}
