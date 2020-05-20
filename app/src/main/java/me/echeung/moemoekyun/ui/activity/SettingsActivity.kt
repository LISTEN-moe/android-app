package me.echeung.moemoekyun.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.app.TaskStackBuilder
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.ui.base.BaseActivity
import me.echeung.moemoekyun.util.PreferenceUtil

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initAppbar()

        supportFragmentManager.beginTransaction()
            .replace(R.id.content_frame, SettingsFragment())
            .commit()
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_general)
            addPreferencesFromResource(R.xml.pref_music)
            addPreferencesFromResource(R.xml.pref_audio)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            invalidateSettings()
        }

        private fun invalidateSettings() {
            val languageSetting = findPreference<ListPreference>(PreferenceUtil.PREF_GENERAL_LANGUAGE)!!
            setSummary(languageSetting)
            languageSetting.setOnPreferenceChangeListener { _, o ->
                setSummary(languageSetting, o)
                recreateBackStack()
                true
            }
        }

        private fun setSummary(preference: Preference, value: Any = getPreferenceValue(preference)) {
            val stringValue = value.toString()
            if (preference is ListPreference) {
                val index = preference.findIndexOfValue(stringValue)
                preference.setSummary(
                    if (index >= 0) {
                        preference.entries[index]
                    } else {
                        null
                    }
                )
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
            val activity = requireActivity()

            TaskStackBuilder.create(activity)
                .addNextIntent(Intent(activity, MainActivity::class.java))
                .addNextIntent(activity.intent)
                .startActivities()
        }
    }
}
