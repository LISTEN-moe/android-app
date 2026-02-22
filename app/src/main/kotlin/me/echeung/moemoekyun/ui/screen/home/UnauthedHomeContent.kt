package me.echeung.moemoekyun.ui.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.ui.theme.AppTheme
import me.echeung.moemoekyun.ui.util.plus

@Composable
fun UnauthedHomeContent(
    onNavigateLogin: () -> Unit,
    onNavigateRegister: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding + 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            modifier = Modifier
                .fillMaxWidth(0.75f),
            painter = painterResource(R.drawable.logo),
            contentScale = ContentScale.Fit,
            contentDescription = null,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = { onNavigateRegister() },
            ) {
                Text(stringResource(R.string.register))
            }

            Button(
                modifier = Modifier.weight(1f),
                onClick = { onNavigateLogin() },
            ) {
                Text(stringResource(R.string.login))
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun UnauthedHomeContentPreview() {
    AppTheme {
        UnauthedHomeContent(
            onNavigateLogin = {},
            onNavigateRegister = {},
        )
    }
}
