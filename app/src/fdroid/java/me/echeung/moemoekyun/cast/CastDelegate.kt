package me.echeung.moemoekyun.cast

import android.content.Context
import android.view.Menu

/**
 * No-op versions of functions used in Play Store version for Google Cast support
 */
class CastDelegate(context: Context) {

    fun onDestroy() {
    }

    fun initCastButton(menu: Menu?) {
    }

}
