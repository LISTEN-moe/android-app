package me.echeung.moemoekyun.ui

import android.media.AudioManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.bottomSheet.BottomSheetNavigator
import dagger.hilt.android.AndroidEntryPoint
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.ui.screen.home.HomeScreen
import me.echeung.moemoekyun.ui.theme.AppTheme

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Replace splash screen theme
        setTheme(R.style.Theme_App)
        super.onCreate(savedInstanceState)

        // Sets audio type to media (volume button control)
        volumeControlStream = AudioManager.STREAM_MUSIC

        setContent {
            AppTheme {
                BottomSheetNavigator {
                    Navigator(HomeScreen())
                }
            }
        }
    }
}
