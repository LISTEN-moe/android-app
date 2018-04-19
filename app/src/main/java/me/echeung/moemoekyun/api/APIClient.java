package me.echeung.moemoekyun.api;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import me.echeung.moemoekyun.api.cache.SongsCache;
import me.echeung.moemoekyun.api.callbacks.ArtistCallback;
import me.echeung.moemoekyun.api.callbacks.ArtistsCallback;
import me.echeung.moemoekyun.api.callbacks.FavoriteSongCallback;
import me.echeung.moemoekyun.api.callbacks.LoginCallback;
import me.echeung.moemoekyun.api.callbacks.RegisterCallback;
import me.echeung.moemoekyun.api.callbacks.RequestSongCallback;
import me.echeung.moemoekyun.api.callbacks.SearchCallback;
import me.echeung.moemoekyun.api.callbacks.SongsCallback;
import me.echeung.moemoekyun.api.callbacks.UserFavoritesCallback;
import me.echeung.moemoekyun.api.callbacks.UserInfoCallback;
import me.echeung.moemoekyun.api.endpoints.Jpop;
import me.echeung.moemoekyun.api.endpoints.Kpop;
import me.echeung.moemoekyun.api.endpoints.Library;
import me.echeung.moemoekyun.api.radio.Socket;
import me.echeung.moemoekyun.api.radio.Stream;
import me.echeung.moemoekyun.api.responses.ArtistResponse;
import me.echeung.moemoekyun.api.responses.ArtistsResponse;
import me.echeung.moemoekyun.api.responses.AuthResponse;
import me.echeung.moemoekyun.api.responses.BaseResponse;
import me.echeung.moemoekyun.api.responses.FavoritesResponse;
import me.echeung.moemoekyun.api.responses.SongsResponse;
import me.echeung.moemoekyun.api.responses.UserResponse;
import me.echeung.moemoekyun.api.services.ArtistsService;
import me.echeung.moemoekyun.api.services.AuthService;
import me.echeung.moemoekyun.api.services.FavoritesService;
import me.echeung.moemoekyun.api.services.RequestsService;
import me.echeung.moemoekyun.api.services.SongsService;
import me.echeung.moemoekyun.api.services.UsersService;
import me.echeung.moemoekyun.models.Song;
import me.echeung.moemoekyun.models.SongListItem;
import me.echeung.moemoekyun.utils.AuthUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {

    // TODO: better handle this
    public static final String AUTH_ERROR = "api-auth-error";

    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE = "application/json";

    private static final String HEADER_ACCEPT = "Accept";
    private static final String ACCEPT = "application/vnd.listen.v4+json";

    private static final String HEADER_USER_AGENT = "User-Agent";

    @Getter
    private static Retrofit retrofit;

    @Getter
    private final Socket socket;

    @Getter
    private final Stream stream;

    @Getter
    private final AuthUtil authUtil;

    @Getter
    private static Library library;

    private final ArtistsService artistsService;
    private final AuthService authService;
    private final FavoritesService favoritesService;
    private final RequestsService requestsService;
    private final SongsService songsService;
    private final UsersService usersService;
    private final SongsCache songsCache;

    public APIClient(Context context, String userAgent, String libraryName) {
        authUtil = new AuthUtil(context);
        setLibrary(libraryName);

        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(chain -> {
                    final Request request = chain.request();

                    final Request newRequest = request.newBuilder()
                            .addHeader(HEADER_USER_AGENT, userAgent)
                            .addHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE)
                            .addHeader(HEADER_ACCEPT, ACCEPT)
                            .build();

                    return chain.proceed(newRequest);
                })
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(Library.API_BASE)
                .client(okHttpClient)
                .addCallAdapterFactory(new ErrorHandlingAdapter.ErrorHandlingCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        artistsService = retrofit.create(ArtistsService.class);
        authService = retrofit.create(AuthService.class);
        favoritesService = retrofit.create(FavoritesService.class);
        requestsService = retrofit.create(RequestsService.class);
        songsService = retrofit.create(SongsService.class);
        usersService = retrofit.create(UsersService.class);

        songsCache = new SongsCache(this);

        socket = new Socket(okHttpClient, authUtil);
        stream = new Stream(context, userAgent);
    }

    public void changeLibrary(String newMode) {
        setLibrary(newMode);

        socket.reconnect();

        final boolean wasPlaying = stream.isPlaying();
        stream.stop();
        if (wasPlaying) {
            stream.play();
        }
    }

    /**
     * Authenticates to the radio.
     *
     * @param username User's username.
     * @param password User's password.
     * @param callback Listener to handle the response.
     */
    public void authenticate(final String username, final String password, final LoginCallback callback) {
        authService.login(new AuthService.LoginBody(username, password))
                .enqueue(new ErrorHandlingAdapter.WrappedCallback<AuthResponse>(callback) {
                    @Override
                    public void success(final AuthResponse response) {
                        final String userToken = response.getToken();

                        if (response.isMfa()) {
                            authUtil.setMfaAuthToken(userToken);
                            callback.onMfaRequired(userToken);
                            return;
                        }

                        authUtil.setAuthToken(userToken);
                        callback.onSuccess(userToken);
                    }
                });
    }

    /**
     * Second step for MFA authentication.
     *
     * @param otpToken User's one-time password token.
     * @param callback Listener to handle the response.
     */
    public void authenticateMfa(final String otpToken, final LoginCallback callback) {
        authService.mfa(authUtil.getMfaAuthTokenWithPrefix(), new AuthService.LoginMfaBody(otpToken))
                .enqueue(new ErrorHandlingAdapter.WrappedCallback<AuthResponse>(callback) {
                    @Override
                    public void success(final AuthResponse response) {
                        final String userToken = response.getToken();
                        authUtil.setAuthToken(userToken);
                        authUtil.clearMfaAuthToken();
                        callback.onSuccess(userToken);
                    }
                });
    }

    /**
     * Register a new user.
     *
     * @param callback Listener to handle the response.
     */
    public void register(final String email, final String username, final String password, final RegisterCallback callback) {
        authService.register(new AuthService.RegisterBody(email, username, password))
                .enqueue(new ErrorHandlingAdapter.WrappedCallback<BaseResponse>(callback) {
                    @Override
                    public void success(final BaseResponse response) {
                        callback.onSuccess(response.getMessage());
                    }
                });
    }

    /**
     * Gets the user information (id and username).
     *
     * @param callback Listener to handle the response.
     */
    public void getUserInfo(final UserInfoCallback callback) {
        if (!authUtil.isAuthenticated()) {
            callback.onFailure(AUTH_ERROR);
            return;
        }

        usersService.getUserInfo(authUtil.getAuthTokenWithPrefix(), "@me")
                .enqueue(new ErrorHandlingAdapter.WrappedCallback<UserResponse>(callback) {
                    @Override
                    public void success(final UserResponse response) {
                        callback.onSuccess(response.getUser());
                    }
                });
    }

    /**
     * Gets a list of all the user's favorited songs.
     *
     * @param callback Listener to handle the response.
     */
    public void getUserFavorites(final UserFavoritesCallback callback) {
        if (!authUtil.isAuthenticated()) {
            callback.onFailure(AUTH_ERROR);
            return;
        }

        favoritesService.getFavorites(authUtil.getAuthTokenWithPrefix(), library.getName(), "@me")
                .enqueue(new ErrorHandlingAdapter.WrappedCallback<FavoritesResponse>(callback) {
                    @Override
                    public void success(final FavoritesResponse response) {
                        List<Song> favorites = response.getFavorites();
                        for (Song song : favorites) {
                            song.setFavorite(true);
                        }
                        callback.onSuccess(favorites);
                    }
                });
    }

    /**
     * Toggles a song's favorite status
     *
     * @param songId Song to update favorite status of.
     * @param isFavorite Whether the song is currently favorited.
     * @param callback Listener to handle the response.
     */
    public void toggleFavorite(final String songId, final boolean isFavorite, final FavoriteSongCallback callback) {
        if (isFavorite) {
            unfavoriteSong(songId, callback);
        } else {
            favoriteSong(songId, callback);
        }
    }

    /**
     * Favorites a song.
     *
     * @param songId   Song to favorite.
     * @param callback Listener to handle the response.
     */
    public void favoriteSong(final String songId, final FavoriteSongCallback callback) {
        if (!authUtil.isAuthenticated()) {
            callback.onFailure(AUTH_ERROR);
            return;
        }

        favoritesService.favorite(authUtil.getAuthTokenWithPrefix(), songId)
                .enqueue(new ErrorHandlingAdapter.WrappedCallback<BaseResponse>(callback) {
                    @Override
                    public void success(final BaseResponse response) {
                        callback.onSuccess();
                    }
                });
    }

    /**
     * Unfavorites a song.
     *
     * @param songId   Song to unfavorite.
     * @param callback Listener to handle the response.
     */
    public void unfavoriteSong(final String songId, final FavoriteSongCallback callback) {
        if (!authUtil.isAuthenticated()) {
            callback.onFailure(AUTH_ERROR);
            return;
        }

        favoritesService.removeFavorite(authUtil.getAuthTokenWithPrefix(), songId)
                .enqueue(new ErrorHandlingAdapter.WrappedCallback<BaseResponse>(callback) {
                    @Override
                    public void success(final BaseResponse response) {
                        callback.onSuccess();
                    }
                });
    }

    /**
     * Sends a song request to the queue.
     *
     * @param songId   Song to request.
     * @param callback Listener to handle the response.
     */
    public void requestSong(final String songId, final RequestSongCallback callback) {
        if (!authUtil.isAuthenticated()) {
            callback.onFailure(AUTH_ERROR);
            return;
        }

        requestsService.request(authUtil.getAuthTokenWithPrefix(), songId)
                .enqueue(new ErrorHandlingAdapter.WrappedCallback<BaseResponse>(callback) {
                    @Override
                    public void success(final BaseResponse response) {
                        callback.onSuccess();
                    }
                });
    }

    /**
     * Gets all songs.
     *
     * @param callback Listener to handle the response.
     */
    public void getSongs(final SongsCallback callback) {
        if (!authUtil.isAuthenticated()) {
            callback.onFailure(AUTH_ERROR);
            return;
        }

        songsService.getSongs(authUtil.getAuthTokenWithPrefix(), library.getName())
                .enqueue(new ErrorHandlingAdapter.WrappedCallback<SongsResponse>(callback) {
                    @Override
                    public void success(final SongsResponse response) {
                        callback.onSuccess(response.getSongs());
                    }
                });
    }

    /**
     * Searches for songs.
     *
     * @param query    Search query string.
     * @param callback Listener to handle the response.
     */
    public void search(final String query, final SearchCallback callback) {
        if (!authUtil.isAuthenticated()) {
            callback.onFailure(AUTH_ERROR);
            return;
        }

        songsCache.getSongs(new SongsCache.Callback() {
            @Override
            public void onRetrieve(List<SongListItem> songs) {
                List<Song> filteredSongs = filterSongs(songs, query);
                callback.onSuccess(filteredSongs);
            }

            @Override
            public void onFailure(final String message) {
                callback.onFailure(message);
            }
        });
    }

    /**
     * Gets a list of all artists.
     *
     * @param callback Listener to handle the response.
     */
    public void getArtists(final ArtistsCallback callback) {
        if (!authUtil.isAuthenticated()) {
            callback.onFailure(AUTH_ERROR);
            return;
        }

        artistsService.getArtists(authUtil.getAuthTokenWithPrefix(), library.getName())
                .enqueue(new ErrorHandlingAdapter.WrappedCallback<ArtistsResponse>(callback) {
                    @Override
                    public void success(final ArtistsResponse response) {
                        callback.onSuccess(response.getArtists());
                    }
                });
    }

    /**
     * Gets an artist's info.
     *
     * @param artistId Artist to get.
     * @param callback Listener to handle the response.
     */
    public void getArtist(final String artistId, final ArtistCallback callback) {
        if (!authUtil.isAuthenticated()) {
            callback.onFailure(AUTH_ERROR);
            return;
        }

        artistsService.getArtist(authUtil.getAuthTokenWithPrefix(), library.getName(), artistId)
                .enqueue(new ErrorHandlingAdapter.WrappedCallback<ArtistResponse>(callback) {
                    @Override
                    public void success(final ArtistResponse response) {
                        callback.onSuccess(response.getArtist());
                    }
                });
    }

    private void setLibrary(String libraryName) {
        APIClient.library = libraryName.equals(Kpop.NAME) ? Kpop.INSTANCE : Jpop.INSTANCE;
    }

    private List<Song> filterSongs(List<SongListItem> songs, String query) {
        List<Song> filteredSongs = new ArrayList<>();

        for (SongListItem song : songs) {
            if (query == null || song.search(query)) {
                filteredSongs.add(SongListItem.toSong(song));
            }
        }

        return filteredSongs;
    }

}
