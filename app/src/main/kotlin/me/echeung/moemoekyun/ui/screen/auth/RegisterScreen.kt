package me.echeung.moemoekyun.ui.screen.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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

class RegisterScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = getScreenModel<RegisterScreenModel>()
        val state by screenModel.state.collectAsState()

        LaunchedEffect(state.registerState) {
            if (state.registerState == RegisterScreenModel.RegisterState.Complete) {
                navigator.pop()
            }
        }

        var username by remember { mutableStateOf(TextFieldValue("")) }
        var email by remember { mutableStateOf(TextFieldValue("")) }
        var password1 by remember { mutableStateOf(TextFieldValue("")) }
        var password2 by remember { mutableStateOf(TextFieldValue("")) }

        Scaffold(
            topBar = { Toolbar(titleResId = R.string.register, showUpButton = true) },
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
                        label = { Text(stringResource(R.string.username)) },
                        value = username,
                        onValueChange = { username = it },
                        singleLine = true,
                    )

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.email)) },
                        value = email,
                        onValueChange = { email = it },
                        singleLine = true,
                    )

                    PasswordTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.password)) },
                        value = password1,
                        onValueChange = { password1 = it },
                    )

                    PasswordTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.password_confirm)) },
                        value = password2,
                        onValueChange = { password2 = it },
                    )

                    if (state.registerState is RegisterScreenModel.RegisterState.Error) {
                        Text(
                            text = (state.registerState as RegisterScreenModel.RegisterState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            screenModel.register(
                                username.text,
                                email.text,
                                password1.text,
                                password2.text,
                            )
                        },
                    ) {
                        Text(stringResource(R.string.register))
                    }
                }
            }
        }
    }
}
