package me.echeung.moemoekyun.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

public final class PreferenceUtil {

    public static final String PREF_GENERAL_THEME = "pref_general_theme";
    public static final String PREF_GENERAL_LANGUAGE = "pref_general_language";
    public static final String PREF_GENERAL_BROADCAST_INTENT = "pref_general_broadcast_intent";
    public static final String PREF_GENERAL_RANDOM_REQUEST_TITLE = "pref_general_random_request_title";

    public static final String PREF_AUDIO_PAUSE_ON_NOISY = "pref_audio_pause_on_noisy";
    public static final String PREF_AUDIO_DUCK = "pref_audio_duck";
    public static final String PREF_AUDIO_PAUSE_ON_LOSS = "pref_audio_pause_on_loss";

    public static final String PREF_LOCKSCREEN_ALBUMART = "pref_lockscreen_albumart";

    public static final String PREF_COLOR_NOW_PLAYING = "pref_color_now_playing";
    public static final String PREF_COLOR_NAVBAR = "pref_color_navbar";

    public static final String THEME_DEFAULT = "four";
    public static final String THEME_LEGACY = "three";
    public static final String THEME_CHRISTMAS = "christmas";

    private static final String NOW_PLAYING_EXPANDED = "now_playing_expanded";

    private static final String SLEEP_TIMER_MINS = "pref_sleep_timer";

    private final SharedPreferences preferences;

    public PreferenceUtil(@NonNull final Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void registerListener(SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener) {
        preferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    public void unregisterListener(SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener) {
        preferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    public String getTheme() {
        return preferences.getString(PREF_GENERAL_THEME, THEME_DEFAULT);
    }

    public String getLanguage() {
        return preferences.getString(PREF_GENERAL_LANGUAGE, LocaleUtil.DEFAULT);
    }

    public boolean shouldBroadcastIntent() {
        return preferences.getBoolean(PREF_GENERAL_BROADCAST_INTENT, false);
    }

    public boolean shouldShowRandomRequestTitle() {
        return preferences.getBoolean(PREF_GENERAL_RANDOM_REQUEST_TITLE, true);
    }

    public boolean shouldPauseOnNoisy() {
        return preferences.getBoolean(PREF_AUDIO_PAUSE_ON_NOISY, true);
    }

    public boolean shouldDuckAudio() {
        return preferences.getBoolean(PREF_AUDIO_DUCK, true);
    }

    public boolean shouldPauseAudioOnLoss() {
        return preferences.getBoolean(PREF_AUDIO_PAUSE_ON_LOSS, true);
    }

    public boolean shouldShowLockscreenAlbumArt() {
        return preferences.getBoolean(PREF_LOCKSCREEN_ALBUMART, true);
    }

    public boolean shouldColorNowPlaying() {
        return preferences.getBoolean(PREF_COLOR_NOW_PLAYING, true);
    }

    public boolean shouldColorNavbar() {
        return preferences.getBoolean(PREF_COLOR_NAVBAR, false);
    }

    public boolean isNowPlayingExpanded() {
        return preferences.getBoolean(NOW_PLAYING_EXPANDED, true);
    }

    public void setIsNowPlayingExpanded(boolean expanded) {
        preferences.edit()
                .putBoolean(NOW_PLAYING_EXPANDED, expanded)
                .apply();
    }

    public int getSleepTimer() {
        return preferences.getInt(SLEEP_TIMER_MINS, 0);
    }

    public void setSleepTimer(int minutes) {
        preferences.edit()
                .putInt(SLEEP_TIMER_MINS, minutes)
                .apply();
    }

    public void clearSleepTimer() {
        preferences.edit()
                .remove(SLEEP_TIMER_MINS)
                .apply();
    }

}
