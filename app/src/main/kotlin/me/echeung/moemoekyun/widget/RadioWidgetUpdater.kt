package me.echeung.moemoekyun.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.echeung.moemoekyun.domain.radio.RadioService
import me.echeung.moemoekyun.domain.user.interactor.GetAuthenticatedUser
import me.echeung.moemoekyun.util.ext.launchIO
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RadioWidgetUpdater @Inject constructor(
    @ApplicationContext private val context: Context,
    private val radioService: RadioService,
    private val getAuthenticatedUser: GetAuthenticatedUser,
) {
    private val scope = MainScope()

    // Updated by PlaybackServicePlayerListener when play state changes.
    private val isPlaying = MutableStateFlow(false)

    private var observeJob: Job? = null

    /**
     * Begin observing [RadioService.state] and play state, pushing updates to all widget
     * instances. Safe to call multiple times – a running job is not restarted.
     */
    fun startObserving() {
        if (observeJob?.isActive == true) return

        observeJob = combine(
            radioService.state,
            isPlaying,
            getAuthenticatedUser.asFlow(),
        ) { radioState, playing, user ->
            Triple(radioState, playing, user)
        }.onEach { (radioState, playing, user) ->
            val glanceIds = GlanceAppWidgetManager(context)
                .getGlanceIds(RadioWidget::class.java)
            if (glanceIds.isEmpty()) return@onEach

            val song = radioState.currentSong
            glanceIds.forEach { id ->
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
                    prefs.toMutablePreferences().apply {
                        if (song != null) {
                            this[keyTitle] = song.title
                            if (song.artists != null) {
                                this[keyArtists] = song.artists
                            } else {
                                remove(keyArtists)
                            }
                            if (radioState.albumArtUrl != null) {
                                this[keyAlbumArtUrl] = radioState.albumArtUrl ?: ""
                            } else {
                                remove(keyAlbumArtUrl)
                            }
                            this[keyIsFavorited] = song.favorited
                            this[keySongId] = song.id
                        } else {
                            remove(keyTitle)
                            remove(keyArtists)
                            remove(keyAlbumArtUrl)
                            remove(keyIsFavorited)
                            remove(keySongId)
                        }
                        this[keyStation] = radioState.station.name
                        this[keyIsPlaying] = playing
                        this[keyIsAuthenticated] = user != null
                    }
                }
                RadioWidget().update(context, id)
            }
        }.launchIn(scope)
    }

    /** Called by [me.echeung.moemoekyun.service.PlaybackServicePlayerListener]. */
    fun onPlayStateChanged(playing: Boolean) {
        isPlaying.value = playing
    }

    /**
     * Trigger a one-shot update for all widgets using the current [RadioService] state.
     * Used from [RadioWidgetReceiver.onUpdate] when the system requests a refresh.
     */
    fun requestUpdate() {
        scope.launchIO {
            val glanceIds = GlanceAppWidgetManager(context)
                .getGlanceIds(RadioWidget::class.java)
            glanceIds.forEach { id ->
                RadioWidget().update(context, id)
            }
        }
    }
}
