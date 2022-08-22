package me.echeung.moemoekyun.ui.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import me.echeung.moemoekyun.ui.base.BaseActivity
import me.echeung.moemoekyun.ui.screen.about.AboutScreen
import me.echeung.moemoekyun.ui.theme.AppTheme

class AboutActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                AboutScreen()
            }
        }
    }
}
