package me.echeung.moemoekyun.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun PasswordTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
) {
    var hidePassword by remember { mutableStateOf(true) }

    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        label = label,
        value = value,
        onValueChange = onValueChange,
        trailingIcon = {
            IconButton(onClick = { hidePassword = !hidePassword }) {
                Icon(
                    imageVector = if (hidePassword) {
                        Icons.Filled.Visibility
                    } else {
                        Icons.Filled.VisibilityOff
                    },
                    contentDescription = null,
                )
            }
        },
        visualTransformation = if (hidePassword) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
        ),
        isError = isError,
        singleLine = true,
    )
}
