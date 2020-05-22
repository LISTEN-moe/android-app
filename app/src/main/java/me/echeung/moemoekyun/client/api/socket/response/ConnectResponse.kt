package me.echeung.moemoekyun.client.api.socket.response

import me.echeung.moemoekyun.client.model.User

class ConnectResponse : BaseResponse() {
    val d: Details? = null

    class Details {
        val heartbeat: Int = 0
        val message: String? = null
        val user: User? = null
    }
}
