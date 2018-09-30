package me.echeung.moemoekyun.util.system

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import me.echeung.moemoekyun.R

object ThemeUtil {

    @ColorInt
    fun getAccentColor(context: Context): Int {
        return resolveColorAttr(context, R.attr.themeColorAccent)
    }

    @ColorInt
    fun getBackgroundColor(context: Context): Int {
        return resolveColorAttr(context, android.R.attr.windowBackground)
    }

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
