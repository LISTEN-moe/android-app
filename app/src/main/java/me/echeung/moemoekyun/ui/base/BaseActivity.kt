package me.echeung.moemoekyun.ui.base

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import me.echeung.moemoekyun.util.system.LocaleUtil

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        LocaleUtil.setTitle(this)

        super.onCreate(savedInstanceState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finishAfterTransition()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleUtil.setLocale(base))
    }
}
