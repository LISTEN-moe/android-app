package me.echeung.moemoekyun.ui.screen.auth

import android.content.Context
import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.update
import me.echeung.moemoekyun.domain.user.interactor.LoginLogout
import me.echeung.moemoekyun.util.ext.clipboardManager
import me.echeung.moemoekyun.util.ext.launchIO
import javax.inject.Inject

class LoginScreenModel @Inject constructor(
    private val loginLogout: LoginLogout,
) : StateScreenModel<LoginScreenModel.State>(State()) {

    fun login(username: String, password: String) {
        mutableState.update {
            it.copy(loading = true)
        }

        screenModelScope.launchIO {
            val state = loginLogout.login(username, password)

            mutableState.update {
                it.copy(
                    loading = false,
                    result = state.toResult(),
                )
            }
        }
    }

    fun loginMfa(otpToken: String) {
        mutableState.update {
            it.copy(loading = true)
        }

        val token = otpToken.trim { it <= ' ' }
        if (token.length != OTP_LENGTH) {
            mutableState.update {
                it.copy(
                    result = Result.InvalidOtp,
                    loading = false,
                )
            }
            return
        }

        screenModelScope.launchIO {
            val state = loginLogout.loginMfa(token)

            mutableState.update {
                it.copy(
                    loading = false,
                    result = state.toResult(),
                )
            }
        }
    }

    fun getOtpTokenFromClipboardOrNull(context: Context): String? {
        val clipData = context.clipboardManager.primaryClip
        if (clipData == null || clipData.itemCount == 0) {
            return null
        }

        val clipDataItem = clipData.getItemAt(0)
        val clipboardText = clipDataItem.text.toString()

        if (clipboardText.length == OTP_LENGTH && clipboardText.matches(OTP_REGEX)) {
            return clipboardText
        }

        return null
    }

    private fun LoginLogout.State.toResult() = when (this) {
        is LoginLogout.State.Complete -> Result.Complete
        is LoginLogout.State.RequireOtp -> Result.RequireOtp
        is LoginLogout.State.Error -> Result.ApiError(this.message)
    }

    @Immutable
    data class State(
        val loading: Boolean = false,
        val result: Result? = null,
    ) {
        val requiresMfa: Boolean
            get() = result is Result.RequireOtp || result is Result.InvalidOtp
    }

    sealed interface Result {
        data object Complete : Result
        data object InvalidOtp : Result
        data object RequireOtp : Result
        data class ApiError(val message: String) : Result
    }
}

private const val OTP_LENGTH = 6
private val OTP_REGEX = "^\\d*$".toRegex()
