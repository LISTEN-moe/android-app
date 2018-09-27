package me.echeung.moemoekyun.util.system

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import me.echeung.moemoekyun.App
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.util.PreferenceUtil

object ThemeUtil {

    @JvmStatic
    val themeStyle: Int
        @StyleRes
        get() {
            return when (App.preferenceUtil!!.theme) {
                PreferenceUtil.THEME_CHRISTMAS -> R.style.AppThemeChristmas
                PreferenceUtil.THEME_DEFAULT -> R.style.AppTheme
                else -> R.style.AppTheme
            }
        }

    @JvmStatic
    fun setTheme(context: Context): Context {
        context.setTheme(themeStyle)
        return context
    }

    @JvmStatic
    fun colorNavigationBar(activity: Activity) {
        val color = if (App.preferenceUtil!!.shouldColorNavbar())
            ThemeUtil.getAccentColor(activity)
        else
            Color.BLACK

        activity.window.navigationBarColor = color
    }

    @JvmStatic
    @ColorInt
    fun getAccentColor(context: Context): Int {
        return resolveColorAttr(setTheme(context), R.attr.themeColorAccent)
    }

    @JvmStatic
    @ColorInt
    fun getBackgroundColor(context: Context): Int {
        return resolveColorAttr(setTheme(context), android.R.attr.windowBackground)
    }

    @JvmStatic
    @ColorInt
    private fun resolveColorAttr(context: Context?, attrId: Int): Int {
        if (context != null) {
            val typedValue = TypedValue()
            val theme = context.theme

            val wasResolved = theme.resolveAttribute(attrId, typedValue, true)
            if (wasResolved) {
                return if (typedValue.resourceId == 0)
                    typedValue.data
                else
                    ContextCompat.getColor(context, typedValue.resourceId)
            }
        }

        return Color.BLACK
    }

}
