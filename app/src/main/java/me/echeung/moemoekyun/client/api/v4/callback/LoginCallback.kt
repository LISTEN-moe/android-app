package me.echeung.moemoekyun.client.api.v4.callback

interface LoginCallback : BaseCallback {
    fun onSuccess(token: String)

    fun onMfaRequired(token: String)
}
