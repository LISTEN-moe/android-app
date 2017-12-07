package me.echeung.moemoekyun.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;

import com.jakewharton.processphoenix.ProcessPhoenix;

import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.utils.PreferenceUtil;

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
//            addPreferencesFromResource(R.xml.pref_lockscreen);
            addPreferencesFromResource(R.xml.pref_audio);
        }

        @Override
        public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            invalidateSettings();
        }

        private void invalidateSettings() {
            final Preference themeSetting = findPreference(PreferenceUtil.PREF_GENERAL_THEME);
            setSummary(themeSetting);
            themeSetting.setOnPreferenceChangeListener((preference, o) -> {
                setSummary(themeSetting, o);
                promptAppRestart();
                return true;
            });

                final Preference languageSetting = findPreference(PreferenceUtil.PREF_GENERAL_LANGUAGE);
            setSummary(languageSetting);
            languageSetting.setOnPreferenceChangeListener((preference, o) -> {
                setSummary(languageSetting, o);
                promptAppRestart();
                return true;
            });
        }

        private void setSummary(@NonNull Preference preference) {
            setSummary(preference, getPreferenceValue(preference));
        }

        private void setSummary(Preference preference, @NonNull Object value) {
            final String stringValue = value.toString();
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
            final Activity activity = getActivity();
            if (activity != null) {
                new AlertDialog.Builder(activity, R.style.DialogTheme)
                        .setTitle(R.string.restart_app)
                        .setPositiveButton(R.string.restart, (dialogInterface, i) -> ProcessPhoenix.triggerRebirth(getActivity()))
                        .setNegativeButton(android.R.string.cancel, null)
                        .create()
                        .show();
            }
        }
    }
}
