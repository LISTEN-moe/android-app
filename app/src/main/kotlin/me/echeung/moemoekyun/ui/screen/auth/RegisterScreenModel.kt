package me.echeung.moemoekyun.ui.screen.auth

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import kotlinx.coroutines.flow.update
import me.echeung.moemoekyun.domain.user.UserService
import me.echeung.moemoekyun.util.ext.launchIO
import javax.inject.Inject

class RegisterScreenModel @Inject constructor(
    private val userService: UserService,
) : StateScreenModel<RegisterScreenModel.State>(State()) {

    fun register(username: String, email: String, password1: String, password2: String) {
        // TODO: localized errors?
        if (username.isEmpty() || email.isEmpty() || password1.isEmpty() || password2.isEmpty()) {
            mutableState.update {
                it.copy(registerState = RegisterState.Error("Required"))
            }
            return
        }

        if (password1 != password2) {
            mutableState.update {
                it.copy(registerState = RegisterState.Error("Mismatched passwords"))
            }
            return
        }

        coroutineScope.launchIO {
            try {
                userService.register(email, username, password1)
                userService.login(username, password1)

                mutableState.update {
                    it.copy(registerState = RegisterState.Complete)
                }
            } catch (e: Exception) {
                mutableState.update {
                    it.copy(registerState = RegisterState.Error(e.message.orEmpty()))
                }
            }
        }
    }

    data class State(
        val registerState: RegisterState? = null,
    )

    sealed interface RegisterState {
        object Complete : RegisterState
        data class Error(val message: String) : RegisterState
    }
}
