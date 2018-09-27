package me.echeung.moemoekyun.util.system

import android.content.Context

object PluralsUtil {

    @JvmStatic
    fun getString(context: Context, pluralId: Int, value: Int): String {
        val text = context.resources.getQuantityString(pluralId, value)
        return String.format(text, value)
    }

}
