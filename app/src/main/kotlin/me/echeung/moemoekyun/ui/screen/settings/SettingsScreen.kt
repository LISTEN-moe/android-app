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
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.plus
import kotlinx.collections.immutable.toImmutableMap
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.ui.common.Toolbar
import me.echeung.moemoekyun.ui.common.preferences.ListPreference
import me.echeung.moemoekyun.ui.common.preferences.PreferenceGroupHeader
import me.echeung.moemoekyun.ui.common.preferences.SwitchPreference
import me.echeung.moemoekyun.util.system.LocaleUtil

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    val screenModel = hiltViewModel<SettingsScreenModel>()

    val langs = remember { getLangs(context) }
    var currentLanguage by remember {
        mutableStateOf(AppCompatDelegate.getApplicationLocales().get(0)?.toLanguageTag() ?: "")
    }

    LaunchedEffect(currentLanguage) {
        val locale = if (currentLanguage.isEmpty()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(currentLanguage)
        }
        AppCompatDelegate.setApplicationLocales(locale)
    }

    Scaffold(
        topBar = { Toolbar(titleResId = R.string.settings, onBack = onBack) },
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
                    title = stringResource(R.string.pref_title_pause_on_noisy_title),
                    subtitle = stringResource(R.string.pref_title_pause_on_noisy_summary),
                    preference = screenModel.preferenceUtil.shouldPauseOnNoisy(),
                )
            }
        }
    }
}

private val SUPPORTED_LOCALES = listOf(
    "ca-ES", "cs-CZ", "de-DE", "en", "en-GB", "eo-UY", "es-ES", "fr-FR",
    "in-ID", "it-IT", "ja-JP", "ko-KR", "lt-LT", "ms-MY", "nl-NL", "pl-PL",
    "pt-PT", "ru-RU", "si-LK", "sv-SE", "tr-TR", "vi-VN", "zh-CN", "zh-TW",
)

private fun getLangs(context: Context): ImmutableMap<String, String> =
    persistentMapOf("" to context.getString(R.string.system_default)) +
        SUPPORTED_LOCALES
            .associateWith { LocaleUtil.getDisplayName(it) }
            .toImmutableMap()
