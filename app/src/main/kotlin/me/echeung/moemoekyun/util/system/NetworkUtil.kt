package me.echeung.moemoekyun.util.system

import android.os.Build
import me.echeung.moemoekyun.BuildConfig

object NetworkUtil {

    val userAgent: String
        get() = String.format(
            "%s/%s (%s; %s; Android %s)",
            BuildConfig.APPLICATION_ID,
            BuildConfig.VERSION_NAME,
            Build.DEVICE,
            Build.BRAND,
            Build.VERSION.SDK_INT,
        )
}
