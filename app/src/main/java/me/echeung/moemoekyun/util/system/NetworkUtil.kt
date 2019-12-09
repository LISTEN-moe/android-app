package me.echeung.moemoekyun.util.system

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import me.echeung.moemoekyun.BuildConfig
import me.echeung.moemoekyun.util.ext.connectivityManager
import me.echeung.moemoekyun.viewmodel.RadioViewModel
import org.koin.core.KoinComponent
import org.koin.core.get

object NetworkUtil : KoinComponent {

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

            val radioViewModel: RadioViewModel = get()
            radioViewModel.isConnected = isAvailable

            return isAvailable
        }

        return false
    }

    fun isWifi(context: Context?): Boolean {
        if (context == null || !isNetworkAvailable(context)) {
            return false
        }

        val activeNetworkInfo = context.connectivityManager.activeNetworkInfo

        return (activeNetworkInfo != null &&
                activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI &&
                activeNetworkInfo.isConnectedOrConnecting)
    }
}
