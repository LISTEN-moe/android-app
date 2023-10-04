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
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.RatingCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.KeyEvent
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import logcat.logcat
import me.echeung.moemoekyun.BuildConfig
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.client.api.Station
import me.echeung.moemoekyun.client.api.Stream
import me.echeung.moemoekyun.domain.radio.RadioService
import me.echeung.moemoekyun.domain.radio.RadioState
import me.echeung.moemoekyun.domain.radio.interactor.PlayPause
import me.echeung.moemoekyun.domain.radio.interactor.SetStation
import me.echeung.moemoekyun.domain.songs.interactor.FavoriteSong
import me.echeung.moemoekyun.domain.songs.model.DomainSong
import me.echeung.moemoekyun.domain.user.interactor.GetAuthenticatedUser
import me.echeung.moemoekyun.util.AlbumArtUtil
import me.echeung.moemoekyun.util.PreferenceUtil
import me.echeung.moemoekyun.util.ext.launchIO
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class AppService : Service() {

    private val scope = MainScope()
    private val binder = ServiceBinder()

    @Inject
    lateinit var playPause: PlayPause

    @Inject
    lateinit var favoriteSong: FavoriteSong

    @Inject
    lateinit var setStation: SetStation

    @Inject
    lateinit var radioService: RadioService

    @Inject
    lateinit var getAuthenticatedUser: GetAuthenticatedUser

    @Inject
    lateinit var musicNotifier: MusicNotifier

    @Inject
    lateinit var albumArtUtil: AlbumArtUtil

    @Inject
    lateinit var preferenceUtil: PreferenceUtil

    var mediaSession: MediaSessionCompat? = null
        private set
    private var intentReceiver: BroadcastReceiver? = null

    override fun onBind(intent: Intent): IBinder? = binder

    override fun onCreate() {
        super.onCreate()

        scope.launchIO {
            combine(
                radioService.state,
                albumArtUtil.flow,
                getAuthenticatedUser.asFlow(),
                preferenceUtil.shouldPreferRomaji().asFlow(),
            ) { radioState, _, _, _ -> radioState }
                .collectLatest(::update)
        }

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

    private fun favoriteCurrentSong() {
        scope.launchIO {
            radioService.state.value.currentSong?.id?.let {
                favoriteSong.await(it)
            }
        }
    }

    private fun update(radioState: RadioState) {
        updateMediaSession(radioState)

        if (radioState.streamState != Stream.State.STOPPED) {
            musicNotifier.update(
                this,
                radioState.currentSong,
                radioState.streamState,
                getAuthenticatedUser.isAuthenticated(),
            )
        }
    }

    private fun initMediaSession() {
        mediaSession = MediaSessionCompat(this, APP_PACKAGE_NAME, null, null).apply {
            setRatingType(RatingCompat.RATING_HEART)
            setCallback(
                object : MediaSessionCompat.Callback() {
                    override fun onPlay() {
                        playPause.play()
                    }

                    override fun onPause() {
                        playPause.pause()
                    }

                    override fun onStop() {
                        playPause.stop()
                    }

                    override fun onSetRating(rating: RatingCompat?) {
                        favoriteCurrentSong()
                    }

                    override fun onMediaButtonEvent(mediaButtonEvent: Intent): Boolean {
                        return handleIntent(mediaButtonEvent)
                    }

                    override fun onCustomAction(action: String?, extras: Bundle?) {
                        when (action) {
                            TOGGLE_FAVORITE -> favoriteCurrentSong()
                            else -> logcat { "Unsupported action: $action" }
                        }
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
                        playPause.play()
                    }
                },
            )

            isActive = true
        }
    }

    private fun updateMediaSession(radioState: RadioState) {
        val currentSong = radioState.currentSong
        if (currentSong == null) {
            mediaSession?.setMetadata(null)
            return
        }

        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentSong.title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currentSong.artists.toString())
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, currentSong.albums.toString())
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, currentSong.durationSeconds)

        if (currentSong.albumArtUrl != null) {
            metadata.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArtUtil.getCurrentAlbumArt(500))
        }

        mediaSession?.setMetadata(metadata.build())
        updateMediaSessionPlaybackState(currentSong, radioState.startTime)
    }

    private fun updateMediaSessionPlaybackState(currentSong: DomainSong, songStartTime: Calendar?) {
        val stateBuilder = PlaybackStateCompat.Builder()
            .setActions(MEDIA_SESSION_ACTIONS)
            .setState(
                when (radioService.state.value.streamState) {
                    Stream.State.PLAYING -> PlaybackStateCompat.STATE_PLAYING
                    Stream.State.PAUSED -> PlaybackStateCompat.STATE_PAUSED
                    Stream.State.STOPPED -> PlaybackStateCompat.STATE_STOPPED
                    Stream.State.BUFFERING -> PlaybackStateCompat.STATE_BUFFERING
                },
                songStartTime?.let { System.currentTimeMillis() - it.timeInMillis } ?: 0,
                1f,
            )

        if (getAuthenticatedUser.isAuthenticated()) {
            stateBuilder.addCustomAction(
                PlaybackStateCompat.CustomAction.Builder(
                    TOGGLE_FAVORITE,
                    if (currentSong.favorited) {
                        getString(R.string.action_unfavorite)
                    } else {
                        getString(R.string.action_favorite)
                    },
                    if (currentSong.favorited) {
                        R.drawable.ic_star_24dp
                    } else {
                        R.drawable.ic_star_border_24dp
                    },
                )
                    .build(),
            )
        }

        if (mediaSession?.isActive == true) {
            mediaSession?.setPlaybackState(stateBuilder.build())
        }
    }

    private fun initBroadcastReceiver() {
        intentReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                handleIntent(intent)
            }
        }

        val intentFilter = IntentFilter().apply {
            addAction(PLAY_PAUSE)
            addAction(STOP)
            addAction(TOGGLE_FAVORITE)
            addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
            addAction(Intent.ACTION_MEDIA_BUTTON)
        }

        ContextCompat.registerReceiver(this, intentReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    private fun handleIntent(intent: Intent?): Boolean {
        if (intent == null) return true

        when (intent.action) {
            PLAY_PAUSE -> playPause.toggle()
            STOP -> playPause.stop()
            TOGGLE_FAVORITE -> favoriteCurrentSong()

            // Pause when headphones unplugged
            AudioManager.ACTION_AUDIO_BECOMING_NOISY -> if (preferenceUtil.shouldPauseOnNoisy().get()) {
                playPause.pause()
            }

            // Headphone media button action
            Intent.ACTION_MEDIA_BUTTON -> {
                val extras = intent.extras ?: return false

                val keyEvent = extras.get(Intent.EXTRA_KEY_EVENT) as KeyEvent?
                if (keyEvent == null || keyEvent.action != KeyEvent.ACTION_DOWN) {
                    return false
                }

                when (keyEvent.keyCode) {
                    KeyEvent.KEYCODE_HEADSETHOOK, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> playPause.toggle()
                    KeyEvent.KEYCODE_MEDIA_PLAY -> playPause.play()
                    KeyEvent.KEYCODE_MEDIA_PAUSE -> playPause.pause()
                    KeyEvent.KEYCODE_MEDIA_STOP -> playPause.stop()
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

        const val PLAY_PAUSE = "$APP_PACKAGE_NAME.play_pause"
        const val STOP = "$APP_PACKAGE_NAME.stop"
        const val TOGGLE_FAVORITE = "$APP_PACKAGE_NAME.toggle_favorite"

        private const val MEDIA_SESSION_ACTIONS =
            PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                PlaybackStateCompat.ACTION_STOP or
                PlaybackStateCompat.ACTION_SET_RATING
    }
}
