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
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import logcat.AndroidLogcatLogger
import logcat.LogPriority
import logcat.LogcatLogger
import me.echeung.moemoekyun.di.appModule
import me.echeung.moemoekyun.di.radioModule
import me.echeung.moemoekyun.di.viewModelModule
import me.echeung.moemoekyun.service.RadioService
import me.echeung.moemoekyun.service.notification.EventNotification
import me.echeung.moemoekyun.service.notification.MusicNotifier
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class App : Application(), ServiceConnection, ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()

        if (!LogcatLogger.isInstalled) {
            LogcatLogger.install(AndroidLogcatLogger(if (BuildConfig.DEBUG) LogPriority.VERBOSE else LogPriority.ERROR))
        }

        startKoin {
            // TODO: https://github.com/InsertKoinIO/koin/issues/1188
            androidLogger(if (BuildConfig.DEBUG) Level.ERROR else Level.NONE)
            // androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.ERROR)
            androidContext(this@App)

            modules(appModule, radioModule, viewModelModule)
        }

        initNotificationChannels()
        initRadioService()
    }

    private fun initRadioService() {
        val intent = Intent(this, RadioService::class.java)
        bindService(intent, this, Context.BIND_AUTO_CREATE or Context.BIND_IMPORTANT)
    }

    override fun onServiceConnected(className: ComponentName, service: IBinder) {
        val binder = service as RadioService.ServiceBinder
        App.service = binder.service
    }

    override fun onServiceDisconnected(className: ComponentName) {
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this).apply {
            componentRegistry {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder(this@App))
                } else {
                    add(GifDecoder())
                }
            }
        }.build()
    }

    private fun initNotificationChannels() {
        val notificationManager = NotificationManagerCompat.from(this)
        listOf(
            // Playing
            NotificationChannelCompat.Builder(MusicNotifier.NOTIFICATION_CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW)
                .setName(MusicNotifier.NOTIFICATION_CHANNEL_NAME)
                .build(),
            // Events
            NotificationChannelCompat.Builder(EventNotification.NOTIFICATION_CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName(EventNotification.NOTIFICATION_CHANNEL_NAME)
                .build()
        ).forEach(notificationManager::createNotificationChannel)
    }

    companion object {
        var service: RadioService? = null
    }
}
