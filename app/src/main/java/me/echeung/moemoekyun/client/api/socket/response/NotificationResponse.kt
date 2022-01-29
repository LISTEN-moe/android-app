package me.echeung.moemoekyun.client.api.socket.response

import kotlinx.serialization.Serializable

@Serializable
open class NotificationResponse : BaseResponse() {
    val t: String? = null
    val d: Details? = null

    @Serializable
    data class Details(
        val type: String
    )
}
