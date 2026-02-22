package me.echeung.moemoekyun.ui.screen.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.ui.common.BackgroundBox
import me.echeung.moemoekyun.ui.common.Toolbar

@Composable
fun RegisterScreen(onBack: () -> Unit) {
    val screenModel = hiltViewModel<RegisterScreenModel>()
    val state by screenModel.state.collectAsState()

    LaunchedEffect(state.result) {
        if (state.result is RegisterScreenModel.Result.Complete) {
            onBack()
        }
    }

    if (state.result is RegisterScreenModel.Result.Complete) {
        return
    }

    val username = rememberTextFieldState("")
    val email = rememberTextFieldState("")
    val password1 = rememberTextFieldState("")
    val password2 = rememberTextFieldState("")

    Scaffold(
        topBar = { Toolbar(titleResId = R.string.register, onBack = onBack) },
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
                    state = username,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    enabled = !state.loading,
                )

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.email)) },
                    state = email,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    enabled = !state.loading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                    ),
                )

                OutlinedSecureTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.password)) },
                    state = password1,
                    enabled = !state.loading,
                )

                OutlinedSecureTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.password_confirm)) },
                    state = password2,
                    enabled = !state.loading,
                )

                when (state.result) {
                    is RegisterScreenModel.Result.AllFieldsRequired -> stringResource(R.string.required)
                    is RegisterScreenModel.Result.MismatchedPasswords -> stringResource(R.string.password_mismatch)
                    is RegisterScreenModel.Result.ApiError ->
                        (state.result as RegisterScreenModel.Result.ApiError).message
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
                        screenModel.register(
                            username.text.toString(),
                            email.text.toString(),
                            password1.text.toString(),
                            password2.text.toString(),
                        )
                    },
                ) {
                    Text(stringResource(R.string.register))
                }
            }
        }
    }
}
