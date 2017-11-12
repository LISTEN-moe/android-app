package me.echeung.moemoekyun.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

public class PreferenceUtil {

    public static final String PREF_GENERAL_LANGUAGE = "pref_general_language";
    public static final String PREF_GENERAL_BROADCAST_INTENT = "pref_general_broadcast_intent";
    public static final String PREF_AUDIO_PAUSE_ON_NOISY = "pref_audio_pause_on_noisy";
    public static final String PREF_AUDIO_DUCK = "pref_audio_duck";
    public static final String PREF_AUDIO_PAUSE_ON_LOSS = "pref_audio_pause_on_loss";
    public static final String PREF_LOCKSCREEN_ALBUMART = "pref_lockscreen_albumart";
    public static final String PREF_LOCKSCREEN_ALBUMART_BLUR = "pref_lockscreen_albumart_blur";

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

    public String getLanguage() {
        return preferences.getString(PREF_GENERAL_LANGUAGE, LocaleUtil.DEFAULT);
    }

    public boolean shouldBroadcastIntent() {
        return preferences.getBoolean(PREF_GENERAL_BROADCAST_INTENT, false);
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

    public boolean shouldBlurLockscreenAlbumArt() {
        return preferences.getBoolean(PREF_LOCKSCREEN_ALBUMART_BLUR, false);
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
