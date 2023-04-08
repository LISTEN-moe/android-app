package me.echeung.moemoekyun.ui.screen.auth

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import kotlinx.coroutines.flow.update
import me.echeung.moemoekyun.domain.user.interactor.Register
import me.echeung.moemoekyun.util.ext.launchIO
import javax.inject.Inject

class RegisterScreenModel @Inject constructor(
    private val register: Register,
) : StateScreenModel<RegisterScreenModel.State>(State()) {

    fun register(username: String, email: String, password1: String, password2: String) {
        mutableState.update {
            it.copy(loading = true)
        }

        if (username.isEmpty() || email.isEmpty() || password1.isEmpty() || password2.isEmpty()) {
            mutableState.update {
                it.copy(
                    result = Result.AllFieldsRequired,
                    loading = false,
                )
            }
            return
        }

        if (password1 != password2) {
            mutableState.update {
                it.copy(
                    result = Result.MismatchedPasswords,
                    loading = false,
                )
            }
            return
        }

        coroutineScope.launchIO {
            val state = register.register(email, username, password1)

            mutableState.update {
                it.copy(
                    result = when (state) {
                        is Register.State.Complete -> Result.Complete
                        is Register.State.Error -> Result.ApiError(state.message)
                    },
                    loading = false,
                )
            }
        }
    }

    data class State(
        val loading: Boolean = false,
        val result: Result? = null,
    )

    sealed interface Result {
        object Complete : Result
        object AllFieldsRequired : Result
        object MismatchedPasswords : Result
        data class ApiError(val message: String) : Result
    }
}
