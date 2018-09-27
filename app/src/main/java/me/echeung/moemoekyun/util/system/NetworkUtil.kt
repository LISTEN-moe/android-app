package me.echeung.moemoekyun.util.system

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build

import me.echeung.moemoekyun.App
import me.echeung.moemoekyun.BuildConfig

object NetworkUtil {

    @JvmStatic
    val userAgent: String
        get() = String.format("%s/%s (%s; %s; Android %s)",
                BuildConfig.APPLICATION_ID,
                BuildConfig.VERSION_NAME,
                Build.DEVICE,
                Build.BRAND,
                Build.VERSION.SDK_INT)

    @JvmStatic
    fun isNetworkAvailable(context: Context?): Boolean {
        if (context != null) {
            val activeNetworkInfo = getActiveNetworkInfo(context)

            val isAvailable = activeNetworkInfo != null && activeNetworkInfo.isConnected

            App.radioViewModel!!.isConnected = isAvailable

            return isAvailable
        }

        return false
    }

    @JvmStatic
    fun isWifi(context: Context?): Boolean {
        if (context == null || !isNetworkAvailable(context)) {
            return false
        }

        val activeNetworkInfo = getActiveNetworkInfo(context)

        return (activeNetworkInfo != null
                && activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI
                && activeNetworkInfo.isConnectedOrConnecting)
    }

    @JvmStatic
    private fun getActiveNetworkInfo(context: Context): NetworkInfo? {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return connectivityManager.activeNetworkInfo
    }

}
