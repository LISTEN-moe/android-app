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

        _state.update {
            LoggedOutState
        }
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
        val userFavorites = api.getUserFavorites()

        _state.update { state ->
            state.copy(
                user = userInfo,
                favorites = userFavorites
                    .map(songConverter::toDomainSong)
                    .sortedBy { it.title },
            )
        }
    }
}

data class UserState(val user: User?, val favorites: List<DomainSong>)

private val LoggedOutState = UserState(
    user = null,
    favorites = emptyList(),
)
