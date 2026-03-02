package me.echeung.moemoekyun.domain.user

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import me.echeung.moemoekyun.client.api.ApiClient
import me.echeung.moemoekyun.client.auth.AuthUtil
import me.echeung.moemoekyun.client.model.User
import me.echeung.moemoekyun.data.database.dao.FavouritesDao
import me.echeung.moemoekyun.data.database.dao.SongsDao
import me.echeung.moemoekyun.data.database.entity.toFavouriteEntity
import me.echeung.moemoekyun.data.database.entity.toSongEntity
import me.echeung.moemoekyun.data.database.toDomainSong
import me.echeung.moemoekyun.domain.songs.SongsService
import me.echeung.moemoekyun.domain.songs.model.DomainSong
import me.echeung.moemoekyun.domain.songs.model.SongConverter
import me.echeung.moemoekyun.util.PreferenceUtil
import me.echeung.moemoekyun.util.ext.launchIO
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserService @Inject constructor(
    private val preferenceUtil: PreferenceUtil,
    private val authUtil: AuthUtil,
    private val api: ApiClient,
    private val songConverter: SongConverter,
    private val songsService: SongsService,
    private val songsDao: SongsDao,
    private val favouritesDao: FavouritesDao,
) {

    private val scope = MainScope()

    private val _state = MutableStateFlow(LoggedOutState)
    val state = _state.asStateFlow()

    init {
        scope.launchIO {
            getUser()
        }

        scope.launchIO {
            preferenceUtil.station().asFlow()
                .distinctUntilChanged()
                .collectLatest {
                    getUser()
                }
        }

        scope.launchIO {
            songsService.favoriteEvents
                .filterNotNull()
                .collectLatest { eventSong ->
                    // Only write to DB when a user is logged in
                    if (_state.value.user != null) {
                        val station = preferenceUtil.station().get()
                        if (eventSong.favorited) {
                            songsDao.upsert(eventSong.toSongEntity())
                            favouritesDao.insert(eventSong.toFavouriteEntity(station))
                        } else {
                            favouritesDao.delete(eventSong.id, station)
                        }
                    }

                    _state.update { state ->
                        state.copy(
                            favorites = when (eventSong.favorited) {
                                true -> state.favorites + listOf(eventSong)
                                false -> state.favorites.filterNot { it.id == eventSong.id }
                            },
                        )
                    }
                }
        }
    }

    val isAuthenticated: Boolean
        get() = authUtil.isAuthenticated

    suspend fun login(username: String, password: String): ApiClient.LoginResult {
        val (state, value) = api.authenticate(username, password)
        when (state) {
            ApiClient.LoginResult.REQUIRE_OTP -> {
                authUtil.mfaToken = value
            }
            ApiClient.LoginResult.COMPLETE -> {
                authUtil.authToken = value
                getUser()
            }
            else -> throw IllegalStateException(value)
        }
        return state
    }

    suspend fun loginMfa(otpToken: String): ApiClient.LoginResult {
        val (state, value) = api.authenticateMfa(otpToken)
        when (state) {
            ApiClient.LoginResult.COMPLETE -> {
                authUtil.authToken = value
                authUtil.clearMfaAuthToken()
                getUser()
            }
            else -> throw IllegalStateException(value)
        }
        return state
    }

    fun logout() {
        authUtil.clearAuthToken()
        scope.launchIO {
            favouritesDao.deleteAll()
        }
        preferenceUtil.lastUserUuid().delete()
        _state.update { LoggedOutState }
    }

    suspend fun register(username: String, email: String, password: String) {
        api.register(email, username, password)
    }

    private suspend fun getUser() {
        if (!authUtil.isAuthenticated || !authUtil.isAuthTokenValid()) {
            logout()
            return
        }

        val userInfo = api.getUserInfo()

        // Drop the favourites cache when a different user logs in
        val lastUserUuid = preferenceUtil.lastUserUuid().get()
        if (lastUserUuid.isNotEmpty() && lastUserUuid != userInfo.uuid) {
            favouritesDao.deleteAll()
        }
        preferenceUtil.lastUserUuid().set(userInfo.uuid)

        val station = preferenceUtil.station().get()

        // Show cached favourites immediately while the network refreshes
        val cached = favouritesDao.getFavouriteSongs(station)
        if (cached.isNotEmpty()) {
            _state.update { state ->
                state.copy(
                    user = userInfo,
                    favorites = cached.map { it.toDomainSong() }.sortedBy { it.title },
                )
            }
        }

        // Refresh from network and update cache
        val userFavorites = api.getUserFavorites().map(songConverter::toDomainSong)
        songsDao.upsertAll(userFavorites.map { it.toSongEntity() })
        favouritesDao.replaceAll(userFavorites.map { it.toFavouriteEntity(station) }, station)

        _state.update { state ->
            state.copy(
                user = userInfo,
                favorites = userFavorites.sortedBy { it.title },
            )
        }
    }
}

data class UserState(val user: User?, val favorites: List<DomainSong>)

private val LoggedOutState = UserState(
    user = null,
    favorites = emptyList(),
)
