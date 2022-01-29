package me.echeung.moemoekyun.client.api.socket.response

import kotlinx.serialization.Serializable

@Serializable
open class BaseResponse {
    val op: Int = 0
}
