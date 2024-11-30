package me.echeung.moemoekyun

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import dagger.hilt.android.HiltAndroidApp
import logcat.AndroidLogcatLogger
import logcat.LogPriority
import logcat.LogcatLogger
import me.echeung.moemoekyun.domain.radio.RadioService
import me.echeung.moemoekyun.service.AppService
import me.echeung.moemoekyun.service.MusicNotifier
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), DefaultLifecycleObserver, ServiceConnection, SingletonImageLoader.Factory {

    @Inject
    lateinit var radioService: RadioService

    @Inject
    lateinit var okHttpClient: OkHttpClient

    override fun onCreate() {
        super<Application>.onCreate()

        if (!LogcatLogger.isInstalled) {
            LogcatLogger.install(AndroidLogcatLogger(if (BuildConfig.DEBUG) LogPriority.VERBOSE else LogPriority.ERROR))
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        initNotificationChannels()
        initRadioService()
    }

    private fun initRadioService() {
        val intent = Intent(this, AppService::class.java)
        bindService(intent, this, Context.BIND_AUTO_CREATE or Context.BIND_IMPORTANT)
    }

    override fun onServiceConnected(className: ComponentName, service: IBinder) {
        val binder = service as AppService.ServiceBinder
        App.service = binder.service
    }

    override fun onServiceDisconnected(className: ComponentName) {
    }

    override fun onStart(owner: LifecycleOwner) {
        radioService.connect()
    }

    override fun onStop(owner: LifecycleOwner) {
        radioService.disconnectIfIdle()
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .apply {
                components {
                    add(OkHttpNetworkFetcherFactory(okHttpClient))

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        add(AnimatedImageDecoder.Factory())
                    } else {
                        add(GifDecoder.Factory())
                    }
                }
            }
            .build()
    }

    private fun initNotificationChannels() {
        val notificationManager = NotificationManagerCompat.from(this)
        listOf(
            // Playing
            NotificationChannelCompat.Builder(
                MusicNotifier.NOTIFICATION_CHANNEL_ID,
                NotificationManagerCompat.IMPORTANCE_LOW,
            )
                .setName(MusicNotifier.NOTIFICATION_CHANNEL_NAME)
                .build(),
        ).forEach(notificationManager::createNotificationChannel)
    }

    companion object {
        var service: AppService? = null
    }
}
