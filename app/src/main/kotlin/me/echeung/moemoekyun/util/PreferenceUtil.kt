package me.echeung.moemoekyun.util

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.tfcporciuncula.flow.FlowSharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import me.echeung.moemoekyun.client.api.Station
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceUtil @Inject constructor(
    @ApplicationContext context: Context,
) {

    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val flowPrefs = FlowSharedPreferences(prefs)

    fun station() = flowPrefs.getEnum("library_mode_v2", Station.JPOP)

    fun isNowPlayingExpanded() = flowPrefs.getBoolean("now_playing_expanded", true)

    fun shouldPreferRomaji() = flowPrefs.getBoolean("pref_general_romaji", false)

    fun shouldShowRandomRequestTitle() = flowPrefs.getBoolean("pref_general_random_request_title", true)

    fun shouldPauseOnNoisy() = flowPrefs.getBoolean("pref_audio_pause_on_noisy", true)

    fun shouldDuckAudio() = flowPrefs.getBoolean("pref_audio_duck", true)

    fun shouldPauseAudioOnLoss() = flowPrefs.getBoolean("pref_audio_pause_on_loss", true)

    fun songsSortType() = flowPrefs.getEnum("all_songs_sort_type", SortType.TITLE)
    fun songsSortDescending() = flowPrefs.getBoolean("all_songs_sort_desc", false)

    fun favoritesSortType() = flowPrefs.getEnum("favorites_sort_type", SortType.TITLE)
    fun favoritesSortDescending() = flowPrefs.getBoolean("favorites_sort_desc", false)
}
