package me.echeung.moemoekyun.util.system

import android.content.Context
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import me.echeung.moemoekyun.R

object ThemeUtil {

    @ColorInt
    fun getBackgroundColor(context: Context): Int {
        return ContextCompat.getColor(context, R.color.darker_grey)
    }
}
