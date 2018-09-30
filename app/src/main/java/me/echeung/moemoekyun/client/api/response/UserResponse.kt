package me.echeung.moemoekyun.client.api.response

import me.echeung.moemoekyun.client.model.User

class UserResponse : BaseResponse() {
    lateinit var user: User
}
