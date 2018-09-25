package me.echeung.moemoekyun.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.TaskStackBuilder;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import me.echeung.moemoekyun.BuildConfig;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.ui.base.BaseActivity;
import me.echeung.moemoekyun.util.ImageUtil;
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
                recreateBackStack();
                return true;
            });

            Preference languageSetting = findPreference(PreferenceUtil.PREF_GENERAL_LANGUAGE);
            setSummary(languageSetting);
            languageSetting.setOnPreferenceChangeListener((preference, o) -> {
                setSummary(languageSetting, o);
                recreateBackStack();
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
                    ImageUtil.clearCache(getContext());
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

        private void recreateBackStack() {
            TaskStackBuilder.create(getActivity())
                    .addNextIntent(new Intent(getActivity(), MainActivity.class))
                    .addNextIntent(getActivity().getIntent())
                    .startActivities();
        }

    }

}
