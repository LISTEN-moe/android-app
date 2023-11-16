package me.echeung.moemoekyun.domain.user.interactor

import me.echeung.moemoekyun.client.api.ApiClient
import me.echeung.moemoekyun.domain.user.UserService
import javax.inject.Inject

class LoginLogout @Inject constructor(
    private val userService: UserService,
) {

    suspend fun login(username: String, password: String): State {
        return try {
            when (userService.login(username, password)) {
                ApiClient.LoginResult.COMPLETE -> State.Complete
                ApiClient.LoginResult.REQUIRE_OTP -> State.RequireOtp
                else -> throw IllegalStateException()
            }
        } catch (e: Exception) {
            State.Error(e.message ?: e.javaClass.simpleName)
        }
    }

    suspend fun loginMfa(token: String): State {
        return try {
            when (userService.loginMfa(token)) {
                ApiClient.LoginResult.COMPLETE -> State.Complete
                else -> State.RequireOtp
            }
        } catch (e: Exception) {
            State.Error(e.message ?: e.javaClass.simpleName)
        }
    }

    fun logout() {
        return userService.logout()
    }

    sealed interface State {
        data object Complete : State
        data object RequireOtp : State
        data class Error(val message: String) : State
    }
}
