package me.echeung.moemoekyun.ui.screen.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.hilt.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.ui.common.BackgroundBox
import me.echeung.moemoekyun.ui.common.Toolbar
import me.echeung.moemoekyun.ui.util.autofill

object LoginScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current

        val screenModel = getScreenModel<LoginScreenModel>()
        val state by screenModel.state.collectAsState()

        val username = rememberTextFieldState("")
        val password = rememberTextFieldState("")
        val otpToken = rememberTextFieldState("")

        LaunchedEffect(state.result) {
            if (state.result is LoginScreenModel.Result.Complete) {
                navigator.pop()
            }
        }

        Scaffold(
            topBar = { Toolbar(titleResId = R.string.login, showUpButton = true) },
        ) { contentPadding ->
            BackgroundBox(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .autofill(
                                autofillTypes = listOf(AutofillType.Username),
                                onFill = { username.setTextAndPlaceCursorAtEnd(it) },
                            ),
                        label = { Text(stringResource(R.string.username_or_email)) },
                        state = username,
                        lineLimits = TextFieldLineLimits.SingleLine,
                        enabled = !state.loading,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                        ),
                    )

                    OutlinedSecureTextField(
                        modifier = Modifier.fillMaxWidth()
                            .autofill(
                                autofillTypes = listOf(AutofillType.Password),
                                onFill = { password.setTextAndPlaceCursorAtEnd(it) },
                            ),
                        label = { Text(stringResource(R.string.password)) },
                        state = password,
                        enabled = !state.loading,
                    )

                    if (state.requiresMfa) {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.mfa_hint)) },
                            state = otpToken,
                            lineLimits = TextFieldLineLimits.SingleLine,
                            enabled = !state.loading,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                        )

                        TextButton(
                            enabled = !state.loading,
                            onClick = {
                                screenModel.getOtpTokenFromClipboardOrNull(context)?.let {
                                    otpToken.setTextAndPlaceCursorAtEnd(it)
                                }
                            },
                        ) {
                            Text(stringResource(R.string.paste_from_clipboard))
                        }
                    }

                    when (state.result) {
                        is LoginScreenModel.Result.InvalidOtp -> stringResource(R.string.invalid_mfa_token)
                        is LoginScreenModel.Result.ApiError ->
                            (state.result as LoginScreenModel.Result.ApiError).message
                        else -> null
                    }?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.loading,
                        onClick = {
                            if (state.requiresMfa) {
                                screenModel.loginMfa(otpToken.text.toString())
                            } else {
                                screenModel.login(username.text.toString(), password.text.toString())
                            }
                        },
                    ) {
                        Text(stringResource(R.string.login))
                    }
                }
            }
        }
    }
}
