package me.echeung.moemoekyun.domain.radio

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import logcat.LogPriority
import logcat.asLog
import logcat.logcat
import me.echeung.moemoekyun.client.api.Station
import me.echeung.moemoekyun.client.api.socket.Socket
import me.echeung.moemoekyun.client.model.Event
import me.echeung.moemoekyun.client.stream.Stream
import me.echeung.moemoekyun.domain.songs.interactor.GetFavoriteSongs
import me.echeung.moemoekyun.domain.songs.model.DomainSong
import me.echeung.moemoekyun.domain.songs.model.SongConverter
import me.echeung.moemoekyun.util.PreferenceUtil
import me.echeung.moemoekyun.util.ext.connectivityManager
import me.echeung.moemoekyun.util.ext.launchIO
import me.echeung.moemoekyun.util.system.TimeUtil.toCalendar
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RadioService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferenceUtil: PreferenceUtil,
    private val stream: Stream,
    private val socket: Socket,
    private val songConverter: SongConverter,
    private val getFavoriteSongs: GetFavoriteSongs,
) {

    private val scope = MainScope()

    private val _state = MutableStateFlow(
        RadioState(
            station = preferenceUtil.station().get(),
        ),
    )
    val state = _state.asStateFlow()

    init {
        connect()
        initNetworkStateCallback()

        scope.launchIO {
            combine(
                socket.flow,
                getFavoriteSongs.asFlow(),
                preferenceUtil.shouldPreferRomaji().asFlow(),
            ) { socketResponse, _, _ -> socketResponse }
                .filterIsInstance<Socket.SocketResponse>()
                .collectLatest { socketResponse ->
                    val info = socketResponse.info

                    val startTime = runCatching {
                        info?.startTime?.toCalendar()
                    }.onFailure { e ->
                        logcat(LogPriority.ERROR) { "Error parsing time: ${e.asLog()}" }
                    }

                    _state.value = _state.value.copy(
                        currentSong = info?.song?.let(songConverter::toDomainSong)?.copy(
                            favorited = getFavoriteSongs.isFavorite(info.song.id),
                        ),
                        startTime = startTime.getOrNull(),
                        pastSongs = info?.lastPlayed.orEmpty().map(songConverter::toDomainSong),
                        listeners = info?.listeners ?: 0,
                        requester = info?.requester?.displayName,
                        event = info?.event,
                    )
                }
        }

        scope.launchIO {
            stream.flow
                .collectLatest {
                    _state.value = _state.value.copy(
                        streamState = it,
                    )
                }
        }

        scope.launchIO {
            preferenceUtil.station().asFlow()
                .distinctUntilChanged()
                .collectLatest {
                    _state.value = _state.value.copy(
                        station = it,
                    )

                    socket.reconnect()

                    // Force it to play with new stream
                    if (stream.isPlaying) {
                        stop()
                        play()
                    }
                }
        }
    }

    fun play() {
        stream.play()
    }

    fun pause() {
        stream.pause()
    }

    fun stop() {
        stream.stop()
    }

    fun togglePlayState() {
        if (_state.value.streamState == Stream.State.PLAY) {
            stream.pause()
        } else {
            stream.play()
        }
    }

    fun setStation(station: Station) {
        if (preferenceUtil.station().get() != station) {
            preferenceUtil.station().set(station)
        }
    }

    fun connect() {
        socket.connect()
    }

    fun disconnectIfIdle() {
        if (!stream.isPlaying) {
            stream.stop()
            socket.disconnect()
        }
    }

    private fun initNetworkStateCallback() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
            .build()

        val callback: ConnectivityManager.NetworkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                socket.reconnect()
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                socket.disconnect()
            }
        }

        context.connectivityManager.registerNetworkCallback(networkRequest, callback)
    }
}

data class RadioState(
    val station: Station = Station.JPOP,
    val streamState: Stream.State = Stream.State.STOP,
    val listeners: Int = 0,
    val requester: String? = null,
    val currentSong: DomainSong? = null,
    val startTime: Calendar? = null,
    val pastSongs: List<DomainSong> = emptyList(),
    val event: Event? = null,
)
