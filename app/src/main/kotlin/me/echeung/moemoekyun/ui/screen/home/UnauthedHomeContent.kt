package me.echeung.moemoekyun.ui.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.ui.screen.auth.LoginScreen
import me.echeung.moemoekyun.ui.screen.auth.RegisterScreen
import me.echeung.moemoekyun.ui.theme.AppTheme

@Composable
fun UnauthedHomeContent() {
    val navigator = LocalNavigator.currentOrThrow

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .weight(1f),
            painter = painterResource(R.drawable.logo),
            contentScale = ContentScale.Fit,
            contentDescription = null,
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navigator.push(LoginScreen()) },
        ) {
            Text(stringResource(R.string.login))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navigator.push(RegisterScreen()) },
        ) {
            Text(stringResource(R.string.register))
        }
    }
}

@Preview
@Composable
private fun UnauthedHomeContentPreview() {
    AppTheme {
        UnauthedHomeContent()
    }
}
