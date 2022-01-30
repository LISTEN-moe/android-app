package me.echeung.moemoekyun.util

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.tfcporciuncula.flow.FlowSharedPreferences
import me.echeung.moemoekyun.client.api.Library
import me.echeung.moemoekyun.util.system.LocaleUtil

class PreferenceUtil(context: Context) {

    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val flowPrefs = FlowSharedPreferences(prefs)

    fun language() = prefs.getString(PREF_GENERAL_LANGUAGE, LocaleUtil.DEFAULT)!!

    fun libraryMode() = flowPrefs.getEnum(LIBRARY_MODE, Library.jpop)

    fun isNowPlayingExpanded() = flowPrefs.getBoolean(NOW_PLAYING_EXPANDED, true)

    fun sleepTimer() = flowPrefs.getInt(SLEEP_TIMER_MINS, 0)

    fun shouldPreferRomaji() = flowPrefs.getBoolean(PREF_GENERAL_ROMAJI, false)

    fun shouldShowRandomRequestTitle() = prefs.getBoolean(PREF_MUSIC_RANDOM_REQUEST_TITLE, true)

    fun shouldPauseOnNoisy() = prefs.getBoolean(PREF_AUDIO_PAUSE_ON_NOISY, true)

    fun shouldDuckAudio() = prefs.getBoolean(PREF_AUDIO_DUCK, true)

    fun shouldPauseAudioOnLoss() = prefs.getBoolean(PREF_AUDIO_PAUSE_ON_LOSS, true)

    fun shouldShowLockscreenAlbumArt() = flowPrefs.getBoolean(PREF_MUSIC_LOCKSCREEN_ALBUMART, true)

    companion object {
        private const val LIBRARY_MODE = "library_mode_r2"
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
