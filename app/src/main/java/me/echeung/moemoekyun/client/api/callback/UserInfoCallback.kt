package me.echeung.moemoekyun.client.api.callback

import me.echeung.moemoekyun.client.model.User

interface UserInfoCallback : BaseCallback {
    fun onSuccess(user: User)
}
