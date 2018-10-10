package me.echeung.moemoekyun.util.system

import android.content.Context
import androidx.annotation.ColorInt

object ThemeUtil {

    @ColorInt
    fun getBackgroundColor(context: Context): Int {
        return context.getResourceColor(android.R.attr.windowBackground)
    }

}
