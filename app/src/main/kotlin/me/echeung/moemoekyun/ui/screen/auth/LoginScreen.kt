package me.echeung.moemoekyun.ui.screen.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.hilt.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.client.api.ApiClient
import me.echeung.moemoekyun.ui.common.BackgroundBox
import me.echeung.moemoekyun.ui.common.PasswordTextField
import me.echeung.moemoekyun.ui.common.Toolbar

class LoginScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current

        val screenModel = getScreenModel<LoginScreenModel>()
        val state by screenModel.state.collectAsState()

        var username by remember { mutableStateOf(TextFieldValue("")) }
        var password by remember { mutableStateOf(TextFieldValue("")) }
        var otpToken by remember { mutableStateOf(TextFieldValue("")) }

        LaunchedEffect(state.loginState) {
            if (state.loginState == ApiClient.LoginState.COMPLETE) {
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
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.username_or_email)) },
                        value = username,
                        onValueChange = { username = it },
                        singleLine = true,
                    )

                    PasswordTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.password)) },
                        value = password,
                        onValueChange = { password = it },
                    )

                    if (state.requiresMfa) {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.mfa_hint)) },
                            value = otpToken,
                            onValueChange = { otpToken = it },
                            singleLine = true,
                        )

                        TextButton(onClick = {
                            screenModel.getOtpTokenFromClipboardOrNull(context)?.let {
                                otpToken = otpToken.copy(text = it)
                            }
                        },) {
                            Text(stringResource(R.string.paste_from_clipboard))
                        }
                    }

                    Button(
                        modifier = Modifier.fillMaxWidth(),
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