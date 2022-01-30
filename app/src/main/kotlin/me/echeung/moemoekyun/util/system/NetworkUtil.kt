package me.echeung.moemoekyun.util.system

import android.content.Context
import android.os.Build
import me.echeung.moemoekyun.BuildConfig
import me.echeung.moemoekyun.util.ext.connectivityManager
import me.echeung.moemoekyun.viewmodel.RadioViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

object NetworkUtil : KoinComponent {

    val userAgent: String
        get() = String.format(
            "%s/%s (%s; %s; Android %s)",
            BuildConfig.APPLICATION_ID,
            BuildConfig.VERSION_NAME,
            Build.DEVICE,
            Build.BRAND,
            Build.VERSION.SDK_INT
        )

    fun isNetworkAvailable(context: Context?): Boolean {
        context ?: return false

        val isAvailable = context.connectivityManager
            .getNetworkCapabilities(context.connectivityManager.activeNetwork) != null

        val radioViewModel: RadioViewModel = get()
        radioViewModel.isConnected = isAvailable

        return isAvailable
    }
}
