package me.echeung.moemoekyun.util.system

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import me.echeung.moemoekyun.App
import me.echeung.moemoekyun.BuildConfig

object NetworkUtil {

    val userAgent: String
        get() = String.format("%s/%s (%s; %s; Android %s)",
                BuildConfig.APPLICATION_ID,
                BuildConfig.VERSION_NAME,
                Build.DEVICE,
                Build.BRAND,
                Build.VERSION.SDK_INT)

    fun isNetworkAvailable(context: Context?): Boolean {
        if (context != null) {
            val activeNetworkInfo = context.connectivityManager.activeNetworkInfo

            val isAvailable = activeNetworkInfo != null && activeNetworkInfo.isConnected

            App.radioViewModel!!.isConnected = isAvailable

            return isAvailable
        }

        return false
    }

    fun isWifi(context: Context?): Boolean {
        if (context == null || !isNetworkAvailable(context)) {
            return false
        }

        val activeNetworkInfo = context.connectivityManager.activeNetworkInfo

        return (activeNetworkInfo != null
                && activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI
                && activeNetworkInfo.isConnectedOrConnecting)
    }

}
