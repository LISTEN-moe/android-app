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
import me.echeung.moemoekyun.cast.CastDelegate
import me.echeung.moemoekyun.client.RadioClient
import me.echeung.moemoekyun.client.api.socket.Socket
import me.echeung.moemoekyun.client.auth.AuthUtil
import me.echeung.moemoekyun.client.network.NetworkClient
import me.echeung.moemoekyun.client.stream.Stream
import me.echeung.moemoekyun.service.RadioService
import me.echeung.moemoekyun.service.notification.EventNotification
import me.echeung.moemoekyun.service.notification.MusicNotifier
import me.echeung.moemoekyun.util.AlbumArtUtil
import me.echeung.moemoekyun.util.PreferenceUtil
import me.echeung.moemoekyun.util.SongActionsUtil
import me.echeung.moemoekyun.util.SongSortUtil
import me.echeung.moemoekyun.util.ext.notificationManager
import me.echeung.moemoekyun.util.system.LocaleUtil
import me.echeung.moemoekyun.viewmodel.RadioViewModel
import me.echeung.moemoekyun.viewmodel.UserViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module

class App : Application(), ServiceConnection {

    override fun onCreate() {
        super.onCreate()

        // TODO: instantiate/access all of these via Koin
        preferenceUtil = PreferenceUtil(this)

        val appModule = module {
            single { AlbumArtUtil(androidContext()) }
            single { AuthUtil(androidContext()) }
            single { LocaleUtil(get()) }
            single { NetworkClient(androidContext(), get()) }
            single { preferenceUtil }
            single { SongActionsUtil(get(), get(), get(), get()) }
            single { SongSortUtil(get()) }
        }

        val radioModule = module {
            single { Stream(androidContext()) }
            single { Socket(androidContext(), get()) }
            single { RadioClient(get(), get(), get(), get(), get()) }
            single { CastDelegate(androidContext(), get(), get(), get()) }
        }

        val viewModelModule = module {
            single { UserViewModel() }
            single { RadioViewModel(get(), get()) }
        }

        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.INFO)
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
                    NotificationManager.IMPORTANCE_LOW
                ),
                // Events
                NotificationChannel(
                    EventNotification.NOTIFICATION_CHANNEL_ID,
                    EventNotification.NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            ).forEach(notificationManager::createNotificationChannel)
        }
    }

    companion object {
        var service: RadioService? = null

        var preferenceUtil: PreferenceUtil? = null
            private set
    }
}
