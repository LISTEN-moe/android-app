package me.echeung.moemoekyun.ui.base

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.util.system.LocaleUtil
import org.koin.android.ext.android.inject

abstract class BaseActivity : AppCompatActivity() {

    private val localeUtil: LocaleUtil by inject()

    protected var appbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        localeUtil.setTitle(this)

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
        super.attachBaseContext(localeUtil.setLocale(base))
    }

    open fun initAppbar() {
        appbar = findViewById(R.id.appbar)
        appbar?.let {
            setSupportActionBar(it)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowTitleEnabled(true)
        }
    }
}
