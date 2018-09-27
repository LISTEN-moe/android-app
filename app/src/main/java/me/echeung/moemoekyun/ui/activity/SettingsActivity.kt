package me.echeung.moemoekyun.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.app.TaskStackBuilder
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import me.echeung.moemoekyun.BuildConfig
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.ui.base.BaseActivity
import me.echeung.moemoekyun.util.ImageUtil
import me.echeung.moemoekyun.util.PreferenceUtil

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setSupportActionBar(findViewById(R.id.appbar))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        supportFragmentManager.beginTransaction()
                .replace(R.id.content_frame, SettingsFragment())
                .commit()
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_general)
            addPreferencesFromResource(R.xml.pref_lockscreen)
            addPreferencesFromResource(R.xml.pref_color)
            addPreferencesFromResource(R.xml.pref_audio)

            if (BuildConfig.DEBUG) {
                addPreferencesFromResource(R.xml.pref_advanced)
            }
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            invalidateSettings()
        }

        private fun invalidateSettings() {
            val themeSetting = findPreference(PreferenceUtil.PREF_GENERAL_THEME)
            setSummary(themeSetting)
            themeSetting.setOnPreferenceChangeListener { preference, o ->
                setSummary(themeSetting, o)
                recreateBackStack()
                true
            }

            val languageSetting = findPreference(PreferenceUtil.PREF_GENERAL_LANGUAGE)
            setSummary(languageSetting)
            languageSetting.setOnPreferenceChangeListener { preference, o ->
                setSummary(languageSetting, o)
                recreateBackStack()
                true
            }

            val downloadSetting = findPreference(PreferenceUtil.PREF_GENERAL_DOWNLOAD)
            setSummary(downloadSetting)
            downloadSetting.setOnPreferenceChangeListener { preference, o ->
                setSummary(downloadSetting, o)
                true
            }

            if (BuildConfig.DEBUG) {
                val clearCache = findPreference(PreferenceUtil.PREF_ADVANCED_CLEAR_IMAGE_CACHE)
                clearCache.setOnPreferenceClickListener { preference ->
                    ImageUtil.clearCache(context)
                    true
                }
            }
        }

        private fun setSummary(preference: Preference, value: Any = getPreferenceValue(preference)) {
            val stringValue = value.toString()
            if (preference is ListPreference) {
                val index = preference.findIndexOfValue(stringValue)
                preference.setSummary(
                        if (index >= 0)
                            preference.entries[index]
                        else
                            null)
            } else {
                preference.summary = stringValue
            }
        }

        private fun getPreferenceValue(preference: Preference): String {
            return PreferenceManager
                    .getDefaultSharedPreferences(preference.context)
                    .getString(preference.key, "")!!
        }

        private fun recreateBackStack() {
            TaskStackBuilder.create(activity!!)
                    .addNextIntent(Intent(activity, MainActivity::class.java))
                    .addNextIntent(activity!!.intent)
                    .startActivities()
        }

    }

}
