package me.echeung.moemoekyun.ui.base

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem

import androidx.appcompat.app.AppCompatActivity
import me.echeung.moemoekyun.App
import me.echeung.moemoekyun.util.PreferenceUtil
import me.echeung.moemoekyun.util.system.LocaleUtil
import me.echeung.moemoekyun.util.system.ThemeUtil

abstract class BaseActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtil.setTheme(this)
        LocaleUtil.setTitle(this)

        ThemeUtil.colorNavigationBar(this)

        App.preferenceUtil!!.registerListener(this)

        super.onCreate(savedInstanceState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finishAfterTransition()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        App.preferenceUtil!!.unregisterListener(this)

        super.onDestroy()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleUtil.setLocale(base))
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            PreferenceUtil.PREF_COLOR_NAVBAR -> ThemeUtil.colorNavigationBar(this)
        }
    }

}
