package me.echeung.moemoekyun

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import me.echeung.moemoekyun.client.RadioClient
import me.echeung.moemoekyun.client.auth.AuthUtil
import me.echeung.moemoekyun.service.RadioService
import me.echeung.moemoekyun.util.PreferenceUtil
import me.echeung.moemoekyun.viewmodel.RadioViewModel
import me.echeung.moemoekyun.viewmodel.UserViewModel

class App : Application(), ServiceConnection {

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this

        preferenceUtil = PreferenceUtil(this)

        radioClient = RadioClient(this)

        // UI view models
        radioViewModel = RadioViewModel()
        userViewModel = UserViewModel()

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
        clearService()
    }

    companion object {
        private lateinit var INSTANCE: App

        var service: RadioService? = null
            set(radioService) {
                if (!isServiceBound) {
                    field = radioService
                    isServiceBound = true
                }
            }
        var isServiceBound = false
            private set

        var radioClient: RadioClient? = null
            private set

        var radioViewModel: RadioViewModel? = null
            private set
        var userViewModel: UserViewModel? = null
            private set

        var preferenceUtil: PreferenceUtil? = null
            private set

        val context: Context
            get() = INSTANCE

        val authUtil: AuthUtil
            get() = radioClient!!.authUtil

        fun clearService() {
            isServiceBound = false
            radioClient!!.socket.setListener(null)
        }
    }

}
