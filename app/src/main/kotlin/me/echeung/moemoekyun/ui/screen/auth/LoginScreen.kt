package me.echeung.moemoekyun.ui.screen.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.hilt.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.ui.common.BackgroundBox
import me.echeung.moemoekyun.ui.common.PasswordTextField
import me.echeung.moemoekyun.ui.common.Toolbar
import me.echeung.moemoekyun.ui.util.autofill

object LoginScreen : Screen {

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current

        val screenModel = getScreenModel<LoginScreenModel>()
        val state by screenModel.state.collectAsState()

        var username by remember { mutableStateOf(TextFieldValue("")) }
        var password by remember { mutableStateOf(TextFieldValue("")) }
        var otpToken by remember { mutableStateOf(TextFieldValue("")) }

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
                                onFill = { username = TextFieldValue(it) },
                            ),
                        label = { Text(stringResource(R.string.username_or_email)) },
                        value = username,
                        onValueChange = { username = it },
                        singleLine = true,
                        enabled = !state.loading,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                        ),
                    )

                    PasswordTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.password)) },
                        value = password,
                        onValueChange = { password = it },
                        enabled = !state.loading,
                    )

                    if (state.requiresMfa) {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.mfa_hint)) },
                            value = otpToken,
                            onValueChange = { otpToken = it },
                            singleLine = true,
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
                                    otpToken = otpToken.copy(text = it)
                                }
                            },
                        ) {
                            Text(stringResource(R.string.paste_from_clipboard))
                        }
                    }

                    when (state.result) {
                        is LoginScreenModel.Result.InvalidOtp -> stringResource(R.string.invalid_mfa_token)
                        is LoginScreenModel.Result.ApiError -> (state.result as LoginScreenModel.Result.ApiError).message
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
                                screenModel.loginMfa(otpToken.text)
                            } else {
                                screenModel.login(username.text, password.text)
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
