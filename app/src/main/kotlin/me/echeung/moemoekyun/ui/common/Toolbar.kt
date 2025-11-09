package me.echeung.moemoekyun.ui.common

import androidx.annotation.StringRes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TextFieldDefaults.textFieldColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
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
    modifier: Modifier = Modifier,
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
        modifier = modifier,
    )
}

@Composable
fun toolbarColors() = TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.primary,
    navigationIconContentColor = Color.White,
    titleContentColor = Color.White,
    actionIconContentColor = Color.White,
)

@Composable
fun Toolbar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    showUpButton: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = title,
        navigationIcon = {
            if (showUpButton) {
                UpButton()
            }
        },
        actions = actions,
        modifier = modifier,
        colors = toolbarColors(),
    )
}

@Composable
fun UpButton(modifier: Modifier = Modifier) {
    val navigator = LocalNavigator.currentOrThrow

    IconButton(
        modifier = modifier,
        onClick = { navigator.pop() },
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = stringResource(R.string.action_back),
        )
    }
}

@Composable
fun SearchTextInput(query: String?, modifier: Modifier = Modifier, onQueryChange: (String) -> Unit) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = query ?: "",
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            textStyle = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
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
                    colors = textFieldColors(backgroundColor = Color.Transparent),
                )
            },
        )

        if (!query.isNullOrEmpty()) {
            IconButton(onClick = { onQueryChange("") }) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
