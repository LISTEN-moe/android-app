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
import me.echeung.moemoekyun.client.api.Station
import me.echeung.moemoekyun.client.api.socket.Socket
import me.echeung.moemoekyun.client.api.sse.RadioSse
import me.echeung.moemoekyun.client.model.Event
import me.echeung.moemoekyun.domain.songs.interactor.GetFavoriteSongs
import me.echeung.moemoekyun.domain.songs.model.DomainSong
import me.echeung.moemoekyun.domain.songs.model.SongConverter
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
    private val songConverter: SongConverter,
    private val getFavoriteSongs: GetFavoriteSongs,
) : ConnectivityManager.NetworkCallback() {

    private val scope = MainScope()

    private val _state = MutableStateFlow(
        RadioState(
            station = preferenceUtil.station().get(),
        ),
    )
    val state = _state.asStateFlow()

    init {
        scope.launchIO {
            combine(
                socket.flow,
                getFavoriteSongs.asFlow(),
                preferenceUtil.shouldPreferRomaji().asFlow(),
            ) { socketResponse, _, _ -> socketResponse }
                .filterIsInstance<Socket.Result.Response>()
                .collectLatest { socketResponse ->
                    val info = socketResponse.info
                    _state.value = _state.value.copy(
                        currentSong = info?.song?.let(songConverter::toDomainSong)?.copy(
                            favorited = getFavoriteSongs.isFavorite(info.song.id),
                        ),
                        listeners = info?.listeners ?: 0,
                        requester = info?.requester?.displayName,
                        event = info?.event,
                    )
                }
        }

        scope.launchIO {
            sse.flow.collectLatest { metadata ->
                val startedAt = metadata.startedAt?.let(Instant::parse) ?: return@collectLatest
                _state.value = _state.value.copy(startTime = startedAt)
            }
        }

        scope.launchIO {
            preferenceUtil.station().asFlow()
                .distinctUntilChanged()
                .collectLatest { station ->
                    _state.value = _state.value.copy(station = station)
                    socket.reconnect()
                    sse.connect(station)
                }
        }
    }

    fun setStation(station: Station) {
        if (preferenceUtil.station().get() != station) {
            preferenceUtil.station().set(station)
        }
    }

    fun connect() {
        socket.connect()
        sse.connect(preferenceUtil.station().get())
        context.connectivityManager.registerNetworkCallback(networkRequest, this)
    }

    fun disconnect() {
        socket.disconnect()
        sse.disconnect()
        context.connectivityManager.unregisterNetworkCallback(this)
    }

    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        socket.reconnect()
        sse.connect(preferenceUtil.station().get())
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        socket.disconnect()
        sse.disconnect()
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
    val currentSong: DomainSong? = null,
    val startTime: Instant? = null,
    val event: Event? = null,
) {
    val albumArtUrl: String?
        get() = currentSong?.albumArtUrl ?: event?.image
}
