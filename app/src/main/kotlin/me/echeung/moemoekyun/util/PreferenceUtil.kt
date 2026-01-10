package me.echeung.moemoekyun.util

import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import me.echeung.moemoekyun.client.api.Station
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceUtil @Inject constructor(private val prefs: FlowSharedPreferences) {

    fun station() = prefs.getEnum("library_mode_v2", Station.JPOP)

    fun shouldPreferRomaji() = prefs.getBoolean("pref_general_romaji", false)

    fun shouldShowRandomRequestTitle() = prefs.getBoolean("pref_general_random_request_title", true)

    fun shouldPauseOnNoisy() = prefs.getBoolean("pref_audio_pause_on_noisy", true)

    fun songsSortType() = prefs.getEnum("all_songs_sort_type", SortType.TITLE)
    fun songsSortDescending() = prefs.getBoolean("all_songs_sort_desc", false)

    fun favoritesSortType() = prefs.getEnum("favorites_sort_type", SortType.TITLE)
    fun favoritesSortDescending() = prefs.getBoolean("favorites_sort_desc", false)
}
