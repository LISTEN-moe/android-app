package me.echeung.moemoekyun.ui.screen.about

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.ui.common.Toolbar

@Composable
fun AboutScreen() {
    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = { Toolbar(titleResId = R.string.about, showUpButton = true) }
    ) {
        LazyColumn(modifier = Modifier.padding(8.dp)) {
            item {
                AboutCard {
                    Text("Hello world")
                }
            }
            
            item {
                Button(
                    onClick = { uriHandler.openUri("https://listen-moe.github.io/android-app/privacy.txt") },
                ) {
                    Text(stringResource(R.string.privacy_policy))
                }
            }
        }
    }
}

@Composable
private fun AboutCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
    ) {
        content()
    }
}