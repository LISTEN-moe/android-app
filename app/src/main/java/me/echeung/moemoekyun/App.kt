package me.echeung.moemoekyun

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import me.echeung.moemoekyun.client.RadioClient
import me.echeung.moemoekyun.client.auth.AuthUtil
import me.echeung.moemoekyun.client.network.NetworkClient
import me.echeung.moemoekyun.service.RadioService
import me.echeung.moemoekyun.util.AlbumArtUtil
import me.echeung.moemoekyun.util.PreferenceUtil
import me.echeung.moemoekyun.util.SongActionsUtil
import me.echeung.moemoekyun.util.SongSortUtil
import me.echeung.moemoekyun.util.system.LocaleUtil
import me.echeung.moemoekyun.viewmodel.RadioViewModel
import me.echeung.moemoekyun.viewmodel.UserViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module

class App : Application(), ServiceConnection {

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this

        // TODO: instantiate/access all of these via Koin
        preferenceUtil = PreferenceUtil(this)

        val appModule = module {
            single { AlbumArtUtil(androidContext()) }
            single { preferenceUtil }
            single { LocaleUtil(get()) }
            single { SongActionsUtil(get(), get(), get(), get()) }
            single { SongSortUtil(get()) }

            single { NetworkClient(get()) }

            single { RadioClient(androidContext(), get(), get(), get()) }
            single { AuthUtil(androidContext()) }
        }

        val viewModelModule = module {
            single { UserViewModel() }
            single { RadioViewModel(get(), get()) }
        }

        startKoin {
            androidLogger()
            androidContext(this@App)

            modules(listOf(appModule, viewModelModule))
        }

        // Music player service
        initService()
    }

    private fun initService() {
        val intent = Intent(applicationContext, RadioService::class.java)
        applicationContext.bindService(intent, this, Context.BIND_AUTO_CREATE or Context.BIND_IMPORTANT)
    }

    override fun onServiceConnected(className: ComponentName, service: IBinder) {
        val binder = service as RadioService.ServiceBinder
        App.service = binder.service
    }

    override fun onServiceDisconnected(arg0: ComponentName) {
    }

    companion object {
        private lateinit var INSTANCE: App

        var service: RadioService? = null

        var preferenceUtil: PreferenceUtil? = null
            private set

        val context: Context
            get() = INSTANCE
    }
}
