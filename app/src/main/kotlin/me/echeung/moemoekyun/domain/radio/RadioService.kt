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
import kotlinx.coroutines.flow.distinctUntilChanged
import me.echeung.moemoekyun.client.api.Station
import me.echeung.moemoekyun.client.api.socket.Socket
import me.echeung.moemoekyun.client.api.sse.QueueSse
import me.echeung.moemoekyun.client.api.sse.RadioSse
import me.echeung.moemoekyun.client.model.Event
import me.echeung.moemoekyun.domain.songs.model.DomainSong
import me.echeung.moemoekyun.util.PreferenceUtil
import me.echeung.moemoekyun.util.ext.connectivityManager
import me.echeung.moemoekyun.util.ext.launchIO
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Singleton
class RadioService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferenceUtil: PreferenceUtil,
    private val socket: Socket,
    private val sse: RadioSse,
    private val queueSse: QueueSse,
    private val songUpdateMapper: SongUpdateMapper,
) : ConnectivityManager.NetworkCallback() {

    private val scope = MainScope()

    private val _state = MutableStateFlow(RadioState(station = preferenceUtil.station().get()))
    val state = _state.asStateFlow()

    init {
        scope.launchIO {
            songUpdateMapper.flow(socket.flow).collectLatest { update ->
                _state.value = _state.value.copy(
                    currentSong = update.currentSong,
                    listeners = update.listeners,
                    requester = update.requester,
                    event = update.event,
                )
            }
        }

        scope.launchIO {
            sseStartTimes(sse.flow).collectLatest { startTime ->
                _state.value = _state.value.copy(startTime = startTime)
            }
        }

        scope.launchIO {
            queueSse.flow.collectLatest { queue ->
                _state.value = _state.value.copy(queueCount = queue.amount)
            }
        }

        scope.launchIO {
            preferenceUtil.station().asFlow()
                .distinctUntilChanged()
                .collectLatest { station ->
                    _state.value = _state.value.copy(station = station)
                    socket.reconnect()
                    sse.connect(station)
                    queueSse.connect(station)
                }
        }
    }

    fun setStation(station: Station) {
        if (preferenceUtil.station().get() != station) {
            preferenceUtil.station().set(station)
        }
    }

    fun connect() {
        val station = preferenceUtil.station().get()
        socket.connect()
        sse.connect(station)
        queueSse.connect(station)
        context.connectivityManager.registerNetworkCallback(networkRequest, this)
    }

    fun disconnect() {
        socket.disconnect()
        sse.disconnect()
        queueSse.disconnect()
        context.connectivityManager.unregisterNetworkCallback(this)
    }

    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        val station = preferenceUtil.station().get()
        socket.reconnect()
        sse.connect(station)
        queueSse.connect(station)
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        socket.disconnect()
        sse.disconnect()
        queueSse.disconnect()
    }
}

private val networkRequest = NetworkRequest.Builder()
    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
    .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
    .build()

@OptIn(ExperimentalTime::class)
data class RadioState(
    val station: Station = Station.JPOP,
    val listeners: Int = 0,
    val requester: String? = null,
    val queueCount: Int? = null,
    val currentSong: DomainSong? = null,
    val startTime: Instant? = null,
    val event: Event? = null,
) {
    val albumArtUrl: String?
        get() = currentSong?.albumArtUrl ?: event?.image
}
