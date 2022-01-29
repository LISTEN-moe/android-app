package me.echeung.moemoekyun.util.system

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import logcat.LogPriority
import logcat.asLog
import logcat.logcat
import me.echeung.moemoekyun.util.PreferenceUtil
import java.util.Locale

// Based on https://proandroiddev.com/change-language-programmatically-at-runtime-on-android-5e6bc15c758
class LocaleUtil(
    private val preferenceUtil: PreferenceUtil
) {

    fun setLocale(context: Context): Context {
        return setLocale(context, preferenceUtil.language())
    }

    private fun setLocale(context: Context, language: String): Context {
        val res = context.resources
        val config = Configuration(res.configuration)

        if (language != DEFAULT) {
            val locale: Locale = if (language.contains("-r")) {
                val languageParts = language.split("-r".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                Locale(languageParts[0], languageParts[1])
            } else {
                Locale(language)
            }

            Locale.setDefault(locale)
            config.setLocale(locale)
        }

        return context.createConfigurationContext(config)
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
            logcat(LogPriority.ERROR) { e.asLog() }
        }
    }

    companion object {
        const val DEFAULT = "default"
    }
}
