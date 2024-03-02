package me.echeung.moemoekyun.ui.screen.about

import android.content.pm.PackageManager
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import me.echeung.moemoekyun.BuildConfig
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.ui.common.Toolbar
import me.echeung.moemoekyun.ui.theme.AppTheme

class AboutScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        val uriHandler = LocalUriHandler.current

        val appVersion = remember(context) {
            try {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                var versionText = context.getString(R.string.version, packageInfo.versionName)
                if (BuildConfig.DEBUG) {
                    versionText += " (${packageInfo.packageName})"
                }
                versionText
            } catch (e: PackageManager.NameNotFoundException) {
                ""
            }
        }

        Scaffold(
            topBar = { Toolbar(titleResId = R.string.about, showUpButton = true) },
        ) { contentPadding ->
            LazyColumn(
                contentPadding = contentPadding,
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                item {
                    AboutCard {
                        Image(
                            modifier = Modifier
                                .widthIn(max = 250.dp)
                                .heightIn(max = 80.dp)
                                .padding(16.dp),
                            painter = painterResource(R.drawable.logo),
                            contentDescription = null,
                        )

                        Text(
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            text = appVersion,
                        )

                        if (BuildConfig.FLAVOR == "playstore") {
                            AboutCardItem(Icons.Default.Star, R.string.rate) {
                                uriHandler.openUri("https://play.google.com/store/apps/details?id=me.echeung.moemoekyun")
                            }
                        }
                        AboutCardItem(Icons.Default.Language, R.string.translate) {
                            uriHandler.openUri("https://crwd.in/listenmoe-android-app")
                        }
                        AboutCardItem(Icons.Default.Code, R.string.github) {
                            uriHandler.openUri("https://github.com/LISTEN-moe/android-app")
                        }
                        AboutCardItem(Icons.Default.Description, R.string.licenses) {
                            navigator.push(LicensesScreen())
                        }
                    }
                }

                item {
                    AboutCard(R.string.listenmoe) {
                        AboutCardItem(Icons.Default.Radio, R.string.open_in_browser) {
                            uriHandler.openUri("https://listen.moe")
                        }
                        AboutCardItem(Icons.Default.Person, R.string.discord) {
                            uriHandler.openUri("https://discordapp.com/invite/4S8JYr8")
                        }
                        AboutCardItem(Icons.Default.CardGiftcard, R.string.patreon) {
                            uriHandler.openUri("https://www.patreon.com/odysseyradio")
                        }
                    }
                }

                item {
                    AboutCard(R.string.translators) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            text = stringResource(R.string.translators_list),
                        )
                    }
                }

                item {
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { uriHandler.openUri("https://listen-moe.github.io/android-app/privacy.txt") },
                    ) {
                        Text(stringResource(R.string.privacy_policy))
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutCard(
    @StringRes headingResId: Int? = null,
    content: @Composable () -> Unit,
) {
    Box(modifier = Modifier.padding(vertical = 8.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                headingResId?.let {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        text = stringResource(headingResId),
                    )
                }

                content()
            }
        }
    }
}

@Composable
private fun AboutCardItem(
    imageVector: ImageVector,
    @StringRes textResId: Int,
    onClick: () -> Unit = {},
) {
    TextButton(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        onClick = onClick,
    ) {
        Icon(
            imageVector = imageVector,
            tint = MaterialTheme.colorScheme.secondary,
            contentDescription = null,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            color = MaterialTheme.colorScheme.secondary,
            text = stringResource(textResId),
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

@PreviewLightDark
@Composable
private fun AboutScreenPreview() {
    AppTheme {
        AboutScreen()
    }
}
