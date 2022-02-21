package me.echeung.moemoekyun.client.api.socket.response

import kotlinx.serialization.Serializable
import me.echeung.moemoekyun.client.api.model.User

@Serializable
class ConnectResponse : BaseResponse() {
    val d: Details? = null

    @Serializable
    data class Details(
        val heartbeat: Int,
        val message: String? = null,
        val user: User? = null,
    )
}
