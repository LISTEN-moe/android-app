package me.echeung.moemoekyun.util

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import me.echeung.moemoekyun.App
import me.echeung.moemoekyun.client.api.library.Jpop
import me.echeung.moemoekyun.util.system.LocaleUtil
import me.echeung.moemoekyun.util.system.NetworkUtil

class PreferenceUtil(context: Context) {

    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    val theme: String
        get() = preferences.getString(PREF_GENERAL_THEME, THEME_DEFAULT)!!

    val language: String
        get() = preferences.getString(PREF_GENERAL_LANGUAGE, LocaleUtil.DEFAULT)!!

    var libraryMode: String
        get() = preferences.getString(LIBRARY_MODE, Jpop.NAME)!!
        set(mode) {
            preferences.edit()
                    .putString(LIBRARY_MODE, mode)
                    .apply()

            App.radioClient!!.changeLibrary(mode)
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

    fun shouldDownloadImage(context: Context): Boolean {
        val pref = preferences.getString(PREF_GENERAL_DOWNLOAD, DOWNLOAD_ALWAYS)
        return pref == DOWNLOAD_ALWAYS || pref == DOWNLOAD_WIFI && NetworkUtil.isWifi(context)
    }

    fun shouldPreferRomaji(): Boolean {
        return preferences.getBoolean(PREF_GENERAL_ROMAJI, false)
    }

    fun shouldShowRandomRequestTitle(): Boolean {
        return preferences.getBoolean(PREF_GENERAL_RANDOM_REQUEST_TITLE, true)
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
        return preferences.getBoolean(PREF_LOCKSCREEN_ALBUMART, true)
    }

    fun shouldColorNowPlaying(): Boolean {
        return preferences.getBoolean(PREF_COLOR_NOW_PLAYING, true)
    }

    fun shouldColorNavbar(): Boolean {
        return preferences.getBoolean(PREF_COLOR_NAVBAR, false)
    }

    fun clearSleepTimer() {
        preferences.edit()
                .remove(SLEEP_TIMER_MINS)
                .apply()
    }

    companion object {
        const val PREF_GENERAL_THEME = "pref_general_theme"
        const val PREF_GENERAL_LANGUAGE = "pref_general_language"
        const val PREF_GENERAL_DOWNLOAD = "pref_general_download"
        const val PREF_GENERAL_ROMAJI = "pref_general_romaji"
        const val PREF_GENERAL_RANDOM_REQUEST_TITLE = "pref_general_random_request_title"

        const val PREF_AUDIO_PAUSE_ON_NOISY = "pref_audio_pause_on_noisy"
        const val PREF_AUDIO_DUCK = "pref_audio_duck"
        const val PREF_AUDIO_PAUSE_ON_LOSS = "pref_audio_pause_on_loss"

        const val PREF_LOCKSCREEN_ALBUMART = "pref_lockscreen_albumart"

        const val PREF_COLOR_NOW_PLAYING = "pref_color_now_playing"
        const val PREF_COLOR_NAVBAR = "pref_color_navbar"

        const val PREF_ADVANCED_CLEAR_IMAGE_CACHE = "pref_advanced_clear_image_cache"

        const val THEME_DEFAULT = "four"
        const val THEME_CHRISTMAS = "christmas"

        const val DOWNLOAD_ALWAYS = "always"
        const val DOWNLOAD_WIFI = "wifi"
        const val DOWNLOAD_NEVER = "never"

        private const val LIBRARY_MODE = "library_mode"
        private const val NOW_PLAYING_EXPANDED = "now_playing_expanded"
        private const val SLEEP_TIMER_MINS = "pref_sleep_timer"
    }

}
