package me.echeung.moemoekyun.ui.screen.auth

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import me.echeung.moemoekyun.domain.user.interactor.Register
import me.echeung.moemoekyun.util.ext.launchIO
import javax.inject.Inject

@HiltViewModel
class RegisterScreenModel @Inject constructor(private val register: Register) : ViewModel() {

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    fun register(username: String, email: String, password1: String, password2: String) {
        _state.update {
            it.copy(loading = true)
        }

        if (username.isEmpty() || email.isEmpty() || password1.isEmpty() || password2.isEmpty()) {
            _state.update {
                it.copy(
                    result = Result.AllFieldsRequired,
                    loading = false,
                )
            }
            return
        }

        if (password1 != password2) {
            _state.update {
                it.copy(
                    result = Result.MismatchedPasswords,
                    loading = false,
                )
            }
            return
        }

        viewModelScope.launchIO {
            val state = register.register(email, username, password1)

            _state.update {
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

    @Immutable
    data class State(val loading: Boolean = false, val result: Result? = null)

    sealed interface Result {
        data object Complete : Result
        data object AllFieldsRequired : Result
        data object MismatchedPasswords : Result
        data class ApiError(val message: String) : Result
    }
}
