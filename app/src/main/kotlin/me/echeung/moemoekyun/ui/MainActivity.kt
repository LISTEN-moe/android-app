package me.echeung.moemoekyun.ui

import android.media.AudioManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import me.echeung.moemoekyun.ui.navigation.AppNavDisplay
import me.echeung.moemoekyun.ui.theme.AppTheme
import me.echeung.moemoekyun.util.PreferenceUtil
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var preferenceUtil: PreferenceUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        // Sets audio type to media (volume button control)
        volumeControlStream = AudioManager.STREAM_MUSIC

        // Draw edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val station by preferenceUtil.station().asFlow().collectAsStateWithLifecycle(
                initialValue = preferenceUtil.station().get(),
                lifecycle = LocalLifecycleOwner.current.lifecycle,
            )
            AppTheme(station = station) {
                AppNavDisplay()
            }
        }
    }
}
