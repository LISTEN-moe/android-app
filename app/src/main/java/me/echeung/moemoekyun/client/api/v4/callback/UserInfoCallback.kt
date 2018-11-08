package me.echeung.moemoekyun.client.api.v4.callback

import me.echeung.moemoekyun.client.model.User

interface UserInfoCallback : BaseCallback {
    fun onSuccess(user: User)
}
