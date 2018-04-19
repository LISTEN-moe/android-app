package me.echeung.moemoekyun.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import me.echeung.moemoekyun.BuildConfig;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.util.PreferenceUtil;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setSupportActionBar(findViewById(R.id.appbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new SettingsFragment()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            addPreferencesFromResource(R.xml.pref_general);
            addPreferencesFromResource(R.xml.pref_lockscreen);
            addPreferencesFromResource(R.xml.pref_color);
            addPreferencesFromResource(R.xml.pref_audio);

            if (BuildConfig.DEBUG) {
                addPreferencesFromResource(R.xml.pref_advanced);
            }
        }

        @Override
        public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            invalidateSettings();
        }

        private void invalidateSettings() {
            Preference themeSetting = findPreference(PreferenceUtil.PREF_GENERAL_THEME);
            setSummary(themeSetting);
            themeSetting.setOnPreferenceChangeListener((preference, o) -> {
                setSummary(themeSetting, o);
                promptAppRestart();
                return true;
            });

            Preference languageSetting = findPreference(PreferenceUtil.PREF_GENERAL_LANGUAGE);
            setSummary(languageSetting);
            languageSetting.setOnPreferenceChangeListener((preference, o) -> {
                setSummary(languageSetting, o);
                promptAppRestart();
                return true;
            });

            Preference downloadSetting = findPreference(PreferenceUtil.PREF_GENERAL_DOWNLOAD);
            setSummary(downloadSetting);
            downloadSetting.setOnPreferenceChangeListener((preference, o) -> {
                setSummary(downloadSetting, o);
                return true;
            });

            if (BuildConfig.DEBUG) {
                Preference clearCache = findPreference(PreferenceUtil.PREF_ADVANCED_CLEAR_IMAGE_CACHE);
                clearCache.setOnPreferenceClickListener(preference -> {
                    clearGlideCache();
                    return true;
                });
            }
        }

        private void setSummary(@NonNull Preference preference) {
            setSummary(preference, getPreferenceValue(preference));
        }

        private void setSummary(Preference preference, @NonNull Object value) {
            String stringValue = value.toString();
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);
            } else {
                preference.setSummary(stringValue);
            }
        }

        @NonNull
        private String getPreferenceValue(@NonNull Preference preference) {
            return PreferenceManager
                    .getDefaultSharedPreferences(preference.getContext())
                    .getString(preference.getKey(), "");
        }

        private void promptAppRestart() {
            Activity activity = getActivity();
            if (activity != null) {
                Toast.makeText(activity, R.string.restart_required, Toast.LENGTH_SHORT).show();
            }
        }

        private void clearGlideCache() {
            Context context = getContext();
            if (context == null) return;

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    Glide.get(context).clearDiskCache();
                    return null;
                }
            }.execute();

            Glide.get(context).clearMemory();
        }

    }

}
