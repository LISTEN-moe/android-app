package me.echeung.moemoekyun.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.ColorInt
import androidx.databinding.Bindable
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.echeung.moemoekyun.BR
import me.echeung.moemoekyun.client.api.model.Event
import me.echeung.moemoekyun.client.api.model.Song
import me.echeung.moemoekyun.client.api.model.User
import me.echeung.moemoekyun.util.AlbumArtUtil
import me.echeung.moemoekyun.util.PreferenceUtil
import me.echeung.moemoekyun.util.system.ThemeUtil
import java.util.Calendar

class RadioViewModel(
    private val albumArtUtil: AlbumArtUtil,
    preferenceUtil: PreferenceUtil,
) : BaseViewModel() {

    private val scope = MainScope()

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

        notifyPropertyChanged(BR.currentSong)
        notifyPropertyChanged(BR.currentSongProgress)
    }

    @get:Bindable
    var isLoading = false
        set(isLoading) {
            field = isLoading
            notifyPropertyChanged(BR.loading)
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
        return if (requester == null || requester!!.displayName.isBlank()) {
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
        get() = albumArtUtil.currentAlbumArt

    // Indirectly bind to albumArt: https://stackoverflow.com/a/39087434
    @ColorInt
    fun getBackgroundColor(context: Context, _albumArt: Bitmap?): Int {
        if (!albumArtUtil.isDefaultAlbumArt) {
            val accentColor = albumArtUtil.currentAccentColor
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
            return listOf(currentSong!!, lastSong!!, secondLastSong!!)
        }

    init {
        albumArtUtil.flow
            .onEach { notifyPropertyChanged(BR.albumArt) }
            .launchIn(scope)

        preferenceUtil.shouldPreferRomaji().asFlow()
            .onEach { notifyPropertyChanged(BR.currentSong) }
            .launchIn(scope)
    }

    fun reset() {
        setCurrentSong(null, null)

        listeners = 0
        setRequester(null)
        event = null

        lastSong = null
        secondLastSong = null
    }
}
