package me.echeung.moemoekyun.ui.screen.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.hilt.getScreenModel
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.ui.common.Toolbar
import me.echeung.moemoekyun.ui.common.preferences.ListPreference
import me.echeung.moemoekyun.ui.common.preferences.PreferenceGroupHeader
import me.echeung.moemoekyun.ui.common.preferences.SwitchPreference
import me.echeung.moemoekyun.util.system.LocaleUtil
import org.xmlpull.v1.XmlPullParser

class SettingsScreen : Screen {

    @Composable
    override fun Content() {
        val context = LocalContext.current

        val screenModel = getScreenModel<SettingsScreenModel>()

        val langs = remember { getLangs(context) }
        var currentLanguage by remember { mutableStateOf(AppCompatDelegate.getApplicationLocales().get(0)?.toLanguageTag() ?: "") }

        LaunchedEffect(currentLanguage) {
            val locale = if (currentLanguage.isEmpty()) {
                LocaleListCompat.getEmptyLocaleList()
            } else {
                LocaleListCompat.forLanguageTags(currentLanguage)
            }
            AppCompatDelegate.setApplicationLocales(locale)
        }

        Scaffold(
            topBar = { Toolbar(titleResId = R.string.settings, showUpButton = true) },
        ) { contentPadding ->
            LazyColumn(
                contentPadding = contentPadding,
            ) {
                item {
                    PreferenceGroupHeader(title = stringResource(R.string.pref_header_general))
                }
                item {
                    ListPreference(
                        title = stringResource(R.string.pref_title_language),
                        subtitle = LocaleUtil.getDisplayName(currentLanguage),
                        entries = langs,
                        value = currentLanguage,
                        onValueChange = { newValue ->
                            currentLanguage = newValue
                        },
                    )
                }

                item {
                    PreferenceGroupHeader(title = stringResource(R.string.pref_header_music))
                }
                item {
                    SwitchPreference(
                        title = stringResource(R.string.pref_title_general_romaji),
                        subtitle = stringResource(R.string.pref_title_general_romaji_summary),
                        preference = screenModel.preferenceUtil.shouldPreferRomaji(),
                    )
                }
                item {
                    SwitchPreference(
                        title = stringResource(R.string.pref_title_general_random_request_title),
                        subtitle = stringResource(R.string.pref_title_general_random_request_summary),
                        preference = screenModel.preferenceUtil.shouldShowRandomRequestTitle(),
                    )
                }

                item {
                    PreferenceGroupHeader(title = stringResource(R.string.pref_header_audio))
                }
                item {
                    SwitchPreference(
                        title = stringResource(R.string.pref_title_audio_pause_on_loss_title),
                        subtitle = stringResource(R.string.pref_title_audio_pause_on_loss_summary),
                        preference = screenModel.preferenceUtil.shouldPauseAudioOnLoss(),
                    )
                }
                item {
                    SwitchPreference(
                        title = stringResource(R.string.pref_title_pause_on_noisy_title),
                        subtitle = stringResource(R.string.pref_title_pause_on_noisy_summary),
                        preference = screenModel.preferenceUtil.shouldPauseOnNoisy(),
                    )
                }
                item {
                    SwitchPreference(
                        title = stringResource(R.string.pref_title_audio_duck_title),
                        subtitle = stringResource(R.string.pref_title_audio_duck_summary),
                        preference = screenModel.preferenceUtil.shouldDuckAudio(),
                    )
                }
            }
        }
    }

    private fun getLangs(context: Context): Map<String, String> {
        val langs = mutableListOf<Pair<String, String>>()
        val parser = context.resources.getXml(R.xml.locales_config)
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "locale") {
                for (i in 0 until parser.attributeCount) {
                    if (parser.getAttributeName(i) == "name") {
                        val langTag = parser.getAttributeValue(i)
                        val displayName = LocaleUtil.getDisplayName(langTag)
                        if (displayName.isNotEmpty()) {
                            langs.add(Pair(langTag, displayName))
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        langs.sortBy { it.second }
        langs.add(0, Pair("", context.getString(R.string.system_default)))

        return langs.toMap()
    }
}
