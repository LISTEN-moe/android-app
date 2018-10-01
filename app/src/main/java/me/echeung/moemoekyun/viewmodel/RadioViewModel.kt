package me.echeung.moemoekyun.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.text.TextUtils
import androidx.annotation.ColorInt
import androidx.databinding.Bindable
import me.echeung.moemoekyun.App
import me.echeung.moemoekyun.BR
import me.echeung.moemoekyun.client.model.Event
import me.echeung.moemoekyun.client.model.Song
import me.echeung.moemoekyun.client.model.User
import me.echeung.moemoekyun.util.AlbumArtUtil
import me.echeung.moemoekyun.util.PreferenceUtil
import me.echeung.moemoekyun.util.system.ThemeUtil
import java.util.*

class RadioViewModel : BaseViewModel(), AlbumArtUtil.Callback, SharedPreferences.OnSharedPreferenceChangeListener {

    // Play state
    // ========================================================================

    @get:Bindable
    var currentSong: Song? = null
        private set

    private var currentSongStart: Calendar? = null

    fun setCurrentSong(currentSong: Song?, startTime: Calendar?) {
        this.currentSong = currentSong
        this.currentSongStart = startTime

        isFavorited = currentSong != null && currentSong.favorite

        AlbumArtUtil.updateAlbumArt(App.context, currentSong)

        notifyPropertyChanged(BR.currentSong)
        notifyPropertyChanged(BR.currentSongProgress)
    }

    @get:Bindable
    var isPlaying = false
        set(isPlaying) {
            field = isPlaying
            notifyPropertyChanged(BR.playing)
        }

    @get:Bindable
    var listeners: Int = 0
        set(listeners) {
            field = listeners
            notifyPropertyChanged(BR.listeners)
        }

    private var requester: User? = null

    @Bindable
    fun getRequester(): String? {
        return if (requester == null || TextUtils.isEmpty(requester!!.displayName)) {
            null
        } else requester!!.displayName

    }

    fun setRequester(requester: User?) {
        this.requester = requester
        notifyPropertyChanged(BR.requester)
    }

    @get:Bindable
    var event: Event? = null
        set(event) {
            field = event
            notifyPropertyChanged(BR.event)
        }

    @get:Bindable
    var lastSong: Song? = null
        set(lastSong) {
            field = lastSong
            notifyPropertyChanged(BR.lastSong)
        }

    @get:Bindable
    var secondLastSong: Song? = null
        set(secondLastSong) {
            field = secondLastSong
            notifyPropertyChanged(BR.secondLastSong)
        }


    // Queue
    // ========================================================================

    @get:Bindable
    var queueSize: Int = 0
        set(queueSize) {
            field = queueSize
            notifyPropertyChanged(BR.queueSize)
        }

    @get:Bindable
    var inQueueByUser: Int = 0
        set(inQueueByUser) {
            field = inQueueByUser
            notifyPropertyChanged(BR.inQueueByUser)
        }

    @get:Bindable
    var queuePosition: Int = 0
        set(queuePosition) {
            field = queuePosition
            notifyPropertyChanged(BR.queuePosition)
        }


    // Mini player
    // ========================================================================

    @get:Bindable
    var miniPlayerAlpha: Float = 0.toFloat()
        set(miniPlayerAlpha) {
            field = miniPlayerAlpha
            notifyPropertyChanged(BR.miniPlayerAlpha)
        }

    val currentSongProgress: Long
        @Bindable
        get() = if (currentSongStart == null || currentSong == null || currentSong!!.duration == 0) {
            0
        } else System.currentTimeMillis() - currentSongStart!!.timeInMillis

    val albumArt: Bitmap?
        @Bindable
        get() = AlbumArtUtil.currentAlbumArt

    // Indirectly bind to albumArt: https://stackoverflow.com/a/39087434
    @ColorInt
    fun getBackgroundColor(context: Context, albumArt: Bitmap?): Int {
        if (App.preferenceUtil!!.shouldColorNowPlaying() && !AlbumArtUtil.isDefaultAlbumArt) {
            val accentColor = AlbumArtUtil.currentAccentColor
            if (accentColor != 0) {
                return accentColor
            }
        }

        return ThemeUtil.getBackgroundColor(context)
    }

    var isFavorited: Boolean
        @Bindable
        get() = if (currentSong == null) {
            false
        } else currentSong!!.favorite
        set(isFavorited) {
            if (currentSong == null) {
                return
            }

            this.currentSong!!.favorite = isFavorited
            notifyPropertyChanged(BR.favorited)
        }


    // History
    // ========================================================================

    val history: List<Song>
        get() {
            val songs = ArrayList<Song>()
            songs.add(currentSong!!)
            songs.add(lastSong!!)
            songs.add(secondLastSong!!)
            return songs
        }

    init {
        AlbumArtUtil.registerListener(this)
        App.preferenceUtil!!.registerListener(this)
    }

    fun reset() {
        setCurrentSong(null, null)

        listeners = 0
        setRequester(null)
        event = null

        lastSong = null
        secondLastSong = null

        queueSize = 0
        inQueueByUser = 0
        queuePosition = 0
    }


    // Misc.
    // ========================================================================

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            PreferenceUtil.PREF_GENERAL_ROMAJI -> notifyPropertyChanged(BR.currentSong)

            PreferenceUtil.PREF_MUSIC_COLOR_NOW_PLAYING -> notifyPropertyChanged(BR.albumArt)
        }
    }

    override fun onAlbumArtReady(bitmap: Bitmap) {
        notifyPropertyChanged(BR.albumArt)
    }

}
