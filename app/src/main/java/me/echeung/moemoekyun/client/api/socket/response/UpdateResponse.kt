package me.echeung.moemoekyun.client.api.socket.response

import me.echeung.moemoekyun.client.model.Event
import me.echeung.moemoekyun.client.model.Song
import me.echeung.moemoekyun.client.model.User

class UpdateResponse : BaseResponse() {
    val t: String? = null
    val d: Details? = null

    class Details {
        val song: Song? = null
        val startTime: String? = null
        val lastPlayed: List<Song>? = null
        val listeners: Int = 0
        val requester: User? = null
        val event: Event? = null
    }
}
