package me.echeung.moemoekyun

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import me.echeung.moemoekyun.di.appModule
import me.echeung.moemoekyun.di.radioModule
import me.echeung.moemoekyun.di.viewModelModule
import me.echeung.moemoekyun.service.RadioService
import me.echeung.moemoekyun.service.notification.EventNotification
import me.echeung.moemoekyun.service.notification.MusicNotifier
import me.echeung.moemoekyun.util.PreferenceUtil
import me.echeung.moemoekyun.util.ext.notificationManager
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class App : Application(), ServiceConnection {

    override fun onCreate() {
        super.onCreate()

        // TODO: instantiate/access this with Koin
        preferenceUtil = PreferenceUtil(this)

        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.ERROR)
            androidContext(this@App)

            modules(appModule, radioModule, viewModelModule)
        }

        initNotificationChannels()
        initRadioService()
    }

    private fun initRadioService() {
        val intent = Intent(applicationContext, RadioService::class.java)
        applicationContext.bindService(intent, this, Context.BIND_AUTO_CREATE or Context.BIND_IMPORTANT)
    }

    override fun onServiceConnected(className: ComponentName, service: IBinder) {
        val binder = service as RadioService.ServiceBinder
        App.service = binder.service
    }

    override fun onServiceDisconnected(className: ComponentName) {
    }

    private fun initNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            listOf(
                // Playing
                NotificationChannel(
                    MusicNotifier.NOTIFICATION_CHANNEL_ID,
                    MusicNotifier.NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW,
                ),
                // Events
                NotificationChannel(
                    EventNotification.NOTIFICATION_CHANNEL_ID,
                    EventNotification.NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT,
                ),
            ).forEach(notificationManager::createNotificationChannel)
        }
    }

    companion object {
        var service: RadioService? = null

        var preferenceUtil: PreferenceUtil? = null
            private set
    }
}
