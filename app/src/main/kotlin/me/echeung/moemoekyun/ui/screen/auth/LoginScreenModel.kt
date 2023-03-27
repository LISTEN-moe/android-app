package me.echeung.moemoekyun.ui.screen.auth

import android.content.Context
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import kotlinx.coroutines.flow.update
import me.echeung.moemoekyun.client.api.ApiClient
import me.echeung.moemoekyun.domain.user.UserService
import me.echeung.moemoekyun.util.ext.clipboardManager
import me.echeung.moemoekyun.util.ext.launchIO
import javax.inject.Inject

class LoginScreenModel @Inject constructor(
    private val userService: UserService,
) : StateScreenModel<LoginScreenModel.State>(State()) {

    fun login(username: String, password: String) {
        coroutineScope.launchIO {
            val state = userService.login(username, password)

            mutableState.update {
                it.copy(
                    loginState = state,
                )
            }
        }
    }

    fun loginMfa(otpToken: String) {
        val token = otpToken.trim { it <= ' ' }
        if (token.length == OTP_LENGTH) {
            coroutineScope.launchIO {
                val state = userService.loginMfa(token)

                mutableState.update {
                    it.copy(
                        loginState = state,
                    )
                }
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

    data class State(
        val loginState: ApiClient.LoginState? = null,
    ) {
        val requiresMfa: Boolean
            get() = loginState == ApiClient.LoginState.REQUIRE_OTP
    }
}

private const val OTP_LENGTH = 6
private val OTP_REGEX = "^[0-9]*$".toRegex()
