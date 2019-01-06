package me.echeung.moemoekyun.util.system

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.util.Log
import me.echeung.moemoekyun.App
import java.util.Locale

// Based on https://proandroiddev.com/change-language-programmatically-at-runtime-on-android-5e6bc15c758
object LocaleUtil {

    const val DEFAULT = "default"

    fun setLocale(context: Context): Context {
        if (App.preferenceUtil == null) {
            return context
        }

        val language = App.preferenceUtil!!.language
        return setLocale(context, language)
    }

    fun setLocale(context: Context, language: String): Context {
        var context = context
        val res = context.resources
        val config = Configuration(res.configuration)

        if (language != DEFAULT) {
            val locale: Locale
            if (language.contains("-r")) {
                val languageParts = language.split("-r".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                locale = Locale(languageParts[0], languageParts[1])
            } else {
                locale = Locale(language)
            }
            Locale.setDefault(locale)
            config.setLocale(locale)
        }

        context = context.createConfigurationContext(config)
        return context
    }

    fun setTitle(activity: Activity) {
        try {
            val label = activity
                    .packageManager
                    .getActivityInfo(activity.componentName, PackageManager.GET_META_DATA)
                    .labelRes
            if (label != 0) {
                activity.setTitle(label)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(activity.localClassName, e.message, e)
        }
    }
}
