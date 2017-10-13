package me.echeung.moemoekyun.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

public class PreferenceUtil {

    public static final String PREF_AUDIO_PAUSE_ON_NOISY = "pref_audio_pause_on_noisy";
    public static final String PREF_AUDIO_DUCK = "pref_audio_duck";
    public static final String PREF_LOCKSCREEN_ALBUMART = "pref_lockscreen_albumart";
    public static final String PREF_LOCKSCREEN_ALBUMART_BLUR = "pref_lockscreen_albumart_blur";

    private static final String SLEEP_TIMER_MINS = "pref_sleep_timer";

    private static PreferenceUtil instance;

    private final SharedPreferences preferences;

    private PreferenceUtil(@NonNull final Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static PreferenceUtil getInstance(@NonNull final Context context) {
        if (instance == null) {
            instance = new PreferenceUtil(context.getApplicationContext());
        }
        return instance;
    }

    public void registerListener(SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener) {
        preferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    public void unregisterListener(SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener) {
        preferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    public boolean getAudioPauseOnNoisy() {
        return preferences.getBoolean(PREF_AUDIO_PAUSE_ON_NOISY, true);
    }

    public boolean getAudioDuck() {
        return preferences.getBoolean(PREF_AUDIO_DUCK, true);
    }

    public boolean getLockscreenAlbumArt() {
        return preferences.getBoolean(PREF_LOCKSCREEN_ALBUMART, true);
    }

    public boolean getLockscreenAlbumArtBlur() {
        return preferences.getBoolean(PREF_LOCKSCREEN_ALBUMART_BLUR, false);
    }

    public int getSleepTimer() {
        return preferences.getInt(SLEEP_TIMER_MINS, 0);
    }

    public void setSleepTimer(int timerTime) {
        preferences.edit()
                .putInt(SLEEP_TIMER_MINS, timerTime)
                .apply();
    }

    public void clearSleepTimer() {
        preferences.edit()
                .remove(SLEEP_TIMER_MINS)
                .apply();
    }
}
