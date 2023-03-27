package me.echeung.moemoekyun.ui.common

import androidx.annotation.StringRes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import me.echeung.moemoekyun.R

@Composable
fun Toolbar(
    @StringRes titleResId: Int? = null,
    showUpButton: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Toolbar(
        title = {
            titleResId?.let { Text(text = stringResource(it)) }
        },
        showUpButton = showUpButton,
        actions = actions,
    )
}

@Composable
fun Toolbar(
    title: @Composable () -> Unit,
    showUpButton: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val navigator = LocalNavigator.currentOrThrow

    TopAppBar(
        title = { title() },
        navigationIcon = {
            if (showUpButton) {
                IconButton(onClick = { navigator.pop() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                    )
                }
            }
        },
        actions = actions,
    )
}

@Composable
fun SearchTextInput(
    modifier: Modifier = Modifier,
    query: String?,
    onQueryChange: (String) -> Unit,
) {
    BasicTextField(
        value = query ?: "",
        onValueChange = onQueryChange,
        modifier = modifier,
        textStyle = MaterialTheme.typography.titleMedium.copy(
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp,
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        singleLine = true,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
        decorationBox = { innerTextField ->
            TextFieldDefaults.TextFieldDecorationBox(
                value = query ?: "",
                innerTextField = innerTextField,
                enabled = true,
                singleLine = true,
                placeholder = {
                    Text(
                        text = stringResource(R.string.search),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                        ),
                    )
                },
                visualTransformation = VisualTransformation.None,
                interactionSource = remember { MutableInteractionSource() },
            )
        },
    )
}
