package me.echeung.moemoekyun.util

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import me.echeung.moemoekyun.client.api.library.Jpop
import me.echeung.moemoekyun.util.system.LocaleUtil

class PreferenceUtil(context: Context) {

    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    val language: String
        get() = preferences.getString(PREF_GENERAL_LANGUAGE, LocaleUtil.DEFAULT)!!

    var libraryMode: String
        get() = preferences.getString(LIBRARY_MODE, Jpop.NAME)!!
        set(mode) {
            preferences.edit()
                .putString(LIBRARY_MODE, mode)
                .apply()
        }

    var isNowPlayingExpanded: Boolean
        get() = preferences.getBoolean(NOW_PLAYING_EXPANDED, true)
        set(expanded) = preferences.edit()
            .putBoolean(NOW_PLAYING_EXPANDED, expanded)
            .apply()

    var sleepTimer: Int
        get() = preferences.getInt(SLEEP_TIMER_MINS, 0)
        set(minutes) = preferences.edit()
            .putInt(SLEEP_TIMER_MINS, minutes)
            .apply()

    fun registerListener(sharedPreferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener) {
        preferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
    }

    fun unregisterListener(sharedPreferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener) {
        preferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
    }

    fun shouldPreferRomaji(): Boolean {
        return preferences.getBoolean(PREF_GENERAL_ROMAJI, false)
    }

    fun shouldShowRandomRequestTitle(): Boolean {
        return preferences.getBoolean(PREF_MUSIC_RANDOM_REQUEST_TITLE, true)
    }

    fun shouldPauseOnNoisy(): Boolean {
        return preferences.getBoolean(PREF_AUDIO_PAUSE_ON_NOISY, true)
    }

    fun shouldDuckAudio(): Boolean {
        return preferences.getBoolean(PREF_AUDIO_DUCK, true)
    }

    fun shouldPauseAudioOnLoss(): Boolean {
        return preferences.getBoolean(PREF_AUDIO_PAUSE_ON_LOSS, true)
    }

    fun shouldShowLockscreenAlbumArt(): Boolean {
        return preferences.getBoolean(PREF_MUSIC_LOCKSCREEN_ALBUMART, true)
    }

    fun clearSleepTimer() {
        preferences.edit()
            .remove(SLEEP_TIMER_MINS)
            .apply()
    }

    companion object {
        private const val LIBRARY_MODE = "library_mode"
        private const val NOW_PLAYING_EXPANDED = "now_playing_expanded"
        private const val SLEEP_TIMER_MINS = "pref_sleep_timer"

        const val PREF_GENERAL_LANGUAGE = "pref_general_language"
        const val PREF_GENERAL_ROMAJI = "pref_general_romaji"

        const val PREF_MUSIC_RANDOM_REQUEST_TITLE = "pref_general_random_request_title"
        const val PREF_MUSIC_LOCKSCREEN_ALBUMART = "pref_lockscreen_albumart"

        const val PREF_AUDIO_PAUSE_ON_NOISY = "pref_audio_pause_on_noisy"
        const val PREF_AUDIO_DUCK = "pref_audio_duck"
        const val PREF_AUDIO_PAUSE_ON_LOSS = "pref_audio_pause_on_loss"
    }
}
