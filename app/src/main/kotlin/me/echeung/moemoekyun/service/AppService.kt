package me.echeung.moemoekyun.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.RatingCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.KeyEvent
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import me.echeung.moemoekyun.BuildConfig
import me.echeung.moemoekyun.client.api.Station
import me.echeung.moemoekyun.domain.radio.interactor.SetStation
import me.echeung.moemoekyun.util.PreferenceUtil
import javax.inject.Inject

@AndroidEntryPoint
class AppService : Service() {

    private val scope = MainScope()
    private val binder = ServiceBinder()

    @Inject
    lateinit var setStation: SetStation

    @Inject
    lateinit var preferenceUtil: PreferenceUtil

    var mediaSession: MediaSessionCompat? = null
        private set
    private var intentReceiver: BroadcastReceiver? = null

    override fun onBind(intent: Intent): IBinder? = binder

    override fun onCreate() {
        super.onCreate()

        initMediaSession()
        initBroadcastReceiver()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startID: Int): Int {
        handleIntent(intent)
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        stopSelf()
    }

    override fun onDestroy() {
        scope.cancel()

        mediaSession?.isActive = false
        mediaSession?.release()

        intentReceiver?.let(::unregisterReceiver)

        super.onDestroy()
    }

    private fun initMediaSession() {
        mediaSession = MediaSessionCompat(this, APP_PACKAGE_NAME, null, null).apply {
            setRatingType(RatingCompat.RATING_HEART)
            setCallback(
                object : MediaSessionCompat.Callback() {

                    override fun onMediaButtonEvent(mediaButtonEvent: Intent): Boolean {
                        return handleIntent(mediaButtonEvent)
                    }

                    override fun onPlayFromSearch(query: String?, extras: Bundle?) {
                        if (!query.isNullOrEmpty()) {
                            onPlayFromMediaId(query, extras)
                        }
                    }

                    // Handles changing station in Android Auto
                    override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
                        super.onPlayFromMediaId(mediaId, extras)
                        when (mediaId) {
                            Station.JPOP.name -> setStation.set(Station.JPOP)
                            Station.KPOP.name -> setStation.set(Station.KPOP)
                        }
                    }
                },
            )

            isActive = true
        }
    }

    private fun initBroadcastReceiver() {
        intentReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                handleIntent(intent)
            }
        }

        val intentFilter = IntentFilter().apply {
            addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
            addAction(Intent.ACTION_MEDIA_BUTTON)
        }

        ContextCompat.registerReceiver(this, intentReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    private fun handleIntent(intent: Intent?): Boolean {
        if (intent == null) return true

        when (intent.action) {
            // Pause when headphones unplugged
            AudioManager.ACTION_AUDIO_BECOMING_NOISY -> if (preferenceUtil.shouldPauseOnNoisy().get()) {
                // playPause.pause()
            }

            // Headphone media button action
            Intent.ACTION_MEDIA_BUTTON -> {
                val keyEvent = IntentCompat.getParcelableExtra(intent, Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
                if (keyEvent == null || keyEvent.action != KeyEvent.ACTION_DOWN) {
                    return false
                }

                when (keyEvent.keyCode) {
                    KeyEvent.KEYCODE_HEADSETHOOK, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {}//playPause.toggle()
                    KeyEvent.KEYCODE_MEDIA_PLAY -> {}// playPause.play()
                    KeyEvent.KEYCODE_MEDIA_PAUSE -> {}// playPause.pause()
                    KeyEvent.KEYCODE_MEDIA_STOP -> {}// playPause.stop()
                    else -> { /* Irrelevant */ }
                }
            }
        }

        return true
    }

    inner class ServiceBinder : Binder() {
        val service: AppService
            get() = this@AppService
    }

    companion object {
        private const val APP_PACKAGE_NAME = BuildConfig.APPLICATION_ID

        private const val MEDIA_SESSION_ACTIONS =
            PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                PlaybackStateCompat.ACTION_STOP or
                PlaybackStateCompat.ACTION_SET_RATING
    }
}
