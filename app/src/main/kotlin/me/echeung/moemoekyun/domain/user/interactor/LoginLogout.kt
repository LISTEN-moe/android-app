package me.echeung.moemoekyun.domain.user.interactor

import me.echeung.moemoekyun.client.api.ApiClient
import me.echeung.moemoekyun.domain.user.UserService
import javax.inject.Inject

class LoginLogout @Inject constructor(
    private val userService: UserService,
) {

    suspend fun login(username: String, password: String): ApiClient.LoginState {
        return userService.login(username, password)
    }

    suspend fun loginMfa(token: String): ApiClient.LoginState {
        return userService.loginMfa(token)
    }

    fun logout() {
        return userService.logout()
    }
}
