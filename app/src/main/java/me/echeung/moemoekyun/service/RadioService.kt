package me.echeung.moemoekyun.service

import android.app.Service
import android.content.*
import android.graphics.Bitmap
import android.media.AudioManager
import android.net.ConnectivityManager
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.RatingCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import me.echeung.moemoekyun.App
import me.echeung.moemoekyun.BuildConfig
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.client.api.callback.FavoriteSongCallback
import me.echeung.moemoekyun.client.api.library.Jpop
import me.echeung.moemoekyun.client.api.library.Kpop
import me.echeung.moemoekyun.client.socket.Socket
import me.echeung.moemoekyun.client.socket.response.UpdateResponse
import me.echeung.moemoekyun.client.stream.Stream
import me.echeung.moemoekyun.service.auto.AutoMediaBrowserService
import me.echeung.moemoekyun.ui.activity.MainActivity
import me.echeung.moemoekyun.util.AlbumArtUtil
import me.echeung.moemoekyun.util.PreferenceUtil
import me.echeung.moemoekyun.util.SongActionsUtil
import me.echeung.moemoekyun.util.system.TimeUtil
import java.text.ParseException
import java.util.*

class RadioService : Service(), Socket.Listener, AlbumArtUtil.Callback, SharedPreferences.OnSharedPreferenceChangeListener {

    private val binder = ServiceBinder()

    private var notification: AppNotification? = null
    private var stream: Stream? = null
    private var socket: Socket? = null

    @Volatile
    var mediaSession: MediaSessionCompat? = null
        private set
    private val mediaSessionLock = Any()

    private var audioManager: AudioManager? = null
    private var audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener? = null
    private var wasPlayingBeforeLoss: Boolean = false

    private var intentReceiver: BroadcastReceiver? = null
    private var receiverRegistered = false

    private var isFirstConnectivityChange = true

    val isStreamStarted: Boolean
        get() = stream!!.isStarted

    val isPlaying: Boolean
        get() = stream!!.isPlaying

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    override fun onCreate() {
        AlbumArtUtil.registerListener(this)

        initBroadcastReceiver()
        initMediaSession()
        initAudioManager()

        stream = App.radioClient!!.stream
        socket = App.radioClient!!.socket

        stream!!.setListener(object : Stream.Listener {
            override fun onStreamPlay() {
                App.radioViewModel!!.isPlaying = true

                updateNotification()
                updateMediaSessionPlaybackState()
            }

            override fun onStreamPause() {
                App.radioViewModel!!.isPlaying = false

                updateNotification()
                updateMediaSessionPlaybackState()
            }

            override fun onStreamStop() {
                audioManager!!.abandonAudioFocus(audioFocusChangeListener)

                stopForeground(true)
                stopSelf()

                App.preferenceUtil!!.clearSleepTimer()
                App.radioViewModel!!.isPlaying = false

                updateMediaSessionPlaybackState()
            }
        })

        socket!!.setListener(this)
        socket!!.connect()

        App.preferenceUtil!!.registerListener(this)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startID: Int): Int {
        handleIntent(intent)

        return Service.START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        if (!isPlaying) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        AlbumArtUtil.unregisterListener(this)

        stop()
        socket!!.disconnect()
        stream!!.removeListener()

        if (receiverRegistered) {
            unregisterReceiver(intentReceiver)
            receiverRegistered = false
        }

        destroyMediaSession()

        App.preferenceUtil!!.unregisterListener(this)

        super.onDestroy()
    }

    override fun onSocketReceive(info: UpdateResponse.Details?) {
        val viewModel = App.radioViewModel

        viewModel!!.listeners = info!!.listeners
        viewModel.setRequester(info.requester)
        viewModel.event = info.event

        if (info.queue != null) {
            viewModel.queueSize = info.queue.inQueue
            viewModel.inQueueByUser = info.queue.inQueueByUser
            viewModel.queuePosition = info.queue.inQueueBeforeUser
        }

        var startTime: Calendar? = null
        try {
            startTime = TimeUtil.toCalendar(info.startTime!!)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        viewModel.setCurrentSong(info.song, startTime)
        viewModel.lastSong = info.lastPlayed!![0]
        viewModel.secondLastSong = info.lastPlayed[1]

        updateMediaSession()
        updateNotification()
    }

    override fun onSocketFailure() {
        App.radioViewModel!!.reset()
        updateNotification()
    }

    private fun updateMediaSession() {
        val currentSong = App.radioViewModel!!.currentSong

        if (currentSong == null) {
            mediaSession!!.setMetadata(null)
            return
        }

        val metaData = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentSong.titleString)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currentSong.artistsString)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, currentSong.albumsString)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, (currentSong.duration * MILLISECONDS_IN_SECOND).toLong())

        if (App.preferenceUtil!!.shouldShowLockscreenAlbumArt()) {
            val albumArt = AlbumArtUtil.currentAlbumArt
            if (albumArt != null && !AlbumArtUtil.isDefaultAlbumArt) {
                metaData.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                updateNotification()
            }
        }

        synchronized(mediaSessionLock) {
            mediaSession!!.setMetadata(metaData.build())
            updateMediaSessionPlaybackState()
        }
    }

    private fun updateMediaSessionPlaybackState() {
        // Play/pause state
        val stateBuilder = PlaybackStateCompat.Builder()
                .setActions(MEDIA_SESSION_ACTIONS)
                .setState(if (isStreamStarted)
                    if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
                else
                    PlaybackStateCompat.STATE_STOPPED,
                        App.radioViewModel!!.currentSongProgress, 1f)

        // Favorite action
        if (App.authUtil.isAuthenticated) {
            val currentSong = App.radioViewModel!!.currentSong
            val favoriteIcon = if (currentSong == null || !currentSong.isFavorite)
                R.drawable.ic_star_border_white_24dp
            else
                R.drawable.ic_star_white_24dp

            stateBuilder.addCustomAction(PlaybackStateCompat.CustomAction.Builder(
                    TOGGLE_FAVORITE, getString(R.string.favorite), favoriteIcon)
                    .build())
        }

        if (mediaSession!!.isActive) {
            mediaSession!!.setPlaybackState(stateBuilder.build())
        }
    }

    override fun onAlbumArtReady(bitmap: Bitmap) {
        updateMediaSession()
    }

    private fun updateNotification() {
        if (isStreamStarted) {
            if (notification == null) {
                notification = AppNotification(this)
            }

            notification!!.update()
        } else {
            stopForeground(true)
        }
    }

    private fun handleIntent(intent: Intent?): Boolean {
        if (intent == null) return true

        val action = intent.action
        if (action != null) {
            when (action) {
                RadioService.PLAY_PAUSE -> togglePlayPause()

                RadioService.STOP -> stop()

                RadioService.TOGGLE_FAVORITE -> favoriteCurrentSong()

                RadioService.UPDATE, SongActionsUtil.REQUEST_EVENT -> socket!!.update()

                RadioService.TIMER_STOP -> timerStop()

                // Pause when headphones unplugged
                AudioManager.ACTION_AUDIO_BECOMING_NOISY -> if (App.preferenceUtil!!.shouldPauseOnNoisy()) {
                    pause()
                }

                // Headphone media button action
                Intent.ACTION_MEDIA_BUTTON -> {
                    val extras = intent.extras ?: return false

                    val keyEvent = extras.get(Intent.EXTRA_KEY_EVENT) as KeyEvent
                    if (keyEvent == null || keyEvent.action != KeyEvent.ACTION_DOWN) {
                        return false
                    }

                    when (keyEvent.keyCode) {
                        KeyEvent.KEYCODE_HEADSETHOOK, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> togglePlayPause()
                        KeyEvent.KEYCODE_MEDIA_PLAY -> play()
                        KeyEvent.KEYCODE_MEDIA_PAUSE -> pause()
                        KeyEvent.KEYCODE_MEDIA_STOP -> stop()
                        KeyEvent.KEYCODE_MEDIA_NEXT, KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                        }
                    }// Do nothing
                }

                MainActivity.AUTH_EVENT -> {
                    socket!!.reconnect()
                    if (!App.authUtil.isAuthenticated) {
                        App.radioViewModel!!.isFavorited = false
                        updateNotification()
                    }
                }

                ConnectivityManager.CONNECTIVITY_ACTION -> {
                    // Ignore the initial sticky broadcast on app start
                    if (isFirstConnectivityChange) {
                        isFirstConnectivityChange = false
                        return false
                    }

                    socket!!.reconnect()
                }
            }
        }

        updateNotification()
        return true
    }

    private fun initMediaSession() {
        synchronized(mediaSessionLock) {
            mediaSession = MediaSessionCompat(this, APP_PACKAGE_NAME, null, null)
            mediaSession!!.setRatingType(RatingCompat.RATING_HEART)
            mediaSession!!.setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    play()
                }

                override fun onPause() {
                    pause()
                }

                override fun onStop() {
                    stop()
                }

                override fun onSkipToNext() {}

                override fun onSkipToPrevious() {}

                override fun onSeekTo(pos: Long) {}

                override fun onSetRating(rating: RatingCompat?) {
                    favoriteCurrentSong()
                }

                override fun onMediaButtonEvent(mediaButtonEvent: Intent): Boolean {
                    return handleIntent(mediaButtonEvent)
                }

                override fun onCustomAction(action: String?, extras: Bundle?) {
                    when (action) {
                        TOGGLE_FAVORITE -> {
                            favoriteCurrentSong()
                            updateMediaSessionPlaybackState()
                        }

                        else -> Log.d(TAG, "Unsupported action: " + action!!)
                    }
                }

                override fun onPlayFromSearch(query: String?, extras: Bundle?) {
                    // We don't support searching for specific things since it's just a radio stream
                    // so just toggle playback
                    togglePlayPause()
                }

                override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
                    super.onPlayFromMediaId(mediaId, extras)

                    // Handles changing library mode via Android Auto
                    when (mediaId) {
                        LIBRARY_JPOP -> App.radioClient!!.changeLibrary(Jpop.NAME)

                        LIBRARY_KPOP -> App.radioClient!!.changeLibrary(Kpop.NAME)
                    }
                }
            })

            mediaSession!!.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS or MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS)

            mediaSession!!.isActive = true
        }
    }

    private fun destroyMediaSession() {
        synchronized(mediaSessionLock) {
            if (mediaSession != null) {
                mediaSession!!.isActive = false
                mediaSession!!.release()
            }
        }
    }

    private fun initBroadcastReceiver() {
        intentReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                handleIntent(intent)
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(RadioService.PLAY_PAUSE)
        intentFilter.addAction(RadioService.STOP)
        intentFilter.addAction(RadioService.TOGGLE_FAVORITE)
        intentFilter.addAction(RadioService.UPDATE)
        intentFilter.addAction(SongActionsUtil.REQUEST_EVENT)
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        intentFilter.addAction(Intent.ACTION_MEDIA_BUTTON)
        intentFilter.addAction(MainActivity.AUTH_EVENT)
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)

        registerReceiver(intentReceiver, intentFilter)
        receiverRegistered = true
    }

    private fun initAudioManager() {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    stream!!.unduck()
                    if (wasPlayingBeforeLoss) {
                        play()
                    }
                }

                AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    wasPlayingBeforeLoss = isPlaying
                    if (wasPlayingBeforeLoss && (App.preferenceUtil!!.shouldPauseAudioOnLoss() || AutoMediaBrowserService.isCarUiMode(this))) {
                        pause()
                    }
                }

                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    wasPlayingBeforeLoss = isPlaying
                    if (App.preferenceUtil!!.shouldDuckAudio()) {
                        stream!!.duck()
                    }
                }
            }
        }
    }

    private fun togglePlayPause() {
        if (isPlaying) {
            pause()
        } else {
            play()
        }
    }

    private fun play() {
        // Request audio focus for playback
        val result = audioManager!!.requestAudioFocus(audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN)

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            stream!!.play()
        }
    }

    private fun pause() {
        stream!!.pause()
    }

    private fun stop() {
        stream!!.stop()
    }

    private fun timerStop() {
        stream!!.fadeOut()
    }

    private fun favoriteCurrentSong() {
        val currentSong = App.radioViewModel!!.currentSong ?: return

        val songId = currentSong.id
        if (songId == -1) return

        if (!App.authUtil.isAuthenticated) {
            showLoginRequiredToast()
            return
        }

        val isCurrentlyFavorite = currentSong.isFavorite

        val callback = object : FavoriteSongCallback {
            override fun onSuccess() {
                val currentSong = App.radioViewModel!!.currentSong
                if (currentSong!!.id == songId) {
                    App.radioViewModel!!.isFavorited = !isCurrentlyFavorite
                }

                val favIntent = Intent(SongActionsUtil.FAVORITE_EVENT)
                sendBroadcast(favIntent)

                updateNotification()
                updateMediaSessionPlaybackState()
            }

            override fun onFailure(message: String) {
                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
            }
        }

        App.radioClient!!.api.toggleFavorite(songId.toString(), isCurrentlyFavorite, callback)
    }

    private fun showLoginRequiredToast() {
        Toast.makeText(applicationContext, R.string.login_required, Toast.LENGTH_SHORT).show()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            PreferenceUtil.PREF_LOCKSCREEN_ALBUMART, PreferenceUtil.PREF_GENERAL_ROMAJI -> updateMediaSession()
        }
    }

    inner class ServiceBinder : Binder() {
        val service: RadioService
            get() = this@RadioService
    }

    companion object {
        private val TAG = RadioService::class.java.simpleName

        private const val APP_PACKAGE_NAME = BuildConfig.APPLICATION_ID
        private const val MILLISECONDS_IN_SECOND = 1000

        const val PLAY_PAUSE = "$APP_PACKAGE_NAME.play_pause"
        const val STOP = "$APP_PACKAGE_NAME.stop"
        const val TOGGLE_FAVORITE = "$APP_PACKAGE_NAME.toggle_favorite"
        const val LIBRARY_JPOP = "$APP_PACKAGE_NAME.library_jpop"
        const val LIBRARY_KPOP = "$APP_PACKAGE_NAME.library_kpop"
        const val UPDATE = "$APP_PACKAGE_NAME.update"
        const val TIMER_STOP = "$APP_PACKAGE_NAME.timer_stop"

        private const val MEDIA_SESSION_ACTIONS = (PlaybackStateCompat.ACTION_PLAY
                or PlaybackStateCompat.ACTION_PAUSE
                or PlaybackStateCompat.ACTION_PLAY_PAUSE
                or PlaybackStateCompat.ACTION_STOP
                or PlaybackStateCompat.ACTION_SET_RATING)
    }

}
