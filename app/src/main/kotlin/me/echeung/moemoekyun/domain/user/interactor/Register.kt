package me.echeung.moemoekyun.domain.user.interactor

import me.echeung.moemoekyun.domain.user.UserService
import javax.inject.Inject

class Register @Inject constructor(private val userService: UserService) {

    suspend fun register(username: String, email: String, password: String): State = try {
        userService.register(email, username, password)
        userService.login(username, password)

        State.Complete
    } catch (e: Exception) {
        State.Error(e.message ?: e.javaClass.simpleName)
    }

    sealed interface State {
        data object Complete : State
        data class Error(val message: String) : State
    }
}
