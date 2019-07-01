package me.echeung.moemoekyun.ui.base

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.util.system.LocaleUtil

abstract class BaseActivity : AppCompatActivity() {

    protected var appbar: Toolbar? = null

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

    open fun initAppbar() {
        appbar = findViewById(R.id.appbar)
        if (appbar != null) {
            setSupportActionBar(appbar)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowTitleEnabled(true)
        }
    }
}
