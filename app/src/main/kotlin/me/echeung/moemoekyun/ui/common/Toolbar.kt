package me.echeung.moemoekyun.ui.common

import android.app.Activity
import androidx.annotation.StringRes
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import me.echeung.moemoekyun.R

@Composable
fun Toolbar(
    @StringRes titleResId: Int,
    showUpButton: Boolean = false,
) {
    val context = LocalContext.current

    TopAppBar(
        title = {
            Text(text = stringResource(titleResId))
        },
        navigationIcon = {
            if (showUpButton) {
                IconButton(onClick = { (context as? Activity)?.finish() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.action_back)
                    )
                }
            }
        },
    )
}