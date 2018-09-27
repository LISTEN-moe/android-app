package me.echeung.moemoekyun.client.api.callback

interface LoginCallback : BaseCallback {
    fun onSuccess(token: String)

    fun onMfaRequired(token: String)
}
