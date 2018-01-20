package me.echeung.listenmoeapi;

import android.content.Context;
import android.util.Log;

import me.echeung.listenmoeapi.auth.AuthUtil;
import me.echeung.listenmoeapi.callbacks.AuthCallback;
import me.echeung.listenmoeapi.callbacks.FavoriteSongCallback;
import me.echeung.listenmoeapi.callbacks.RequestSongCallback;
import me.echeung.listenmoeapi.callbacks.SearchCallback;
import me.echeung.listenmoeapi.callbacks.UserFavoritesCallback;
import me.echeung.listenmoeapi.callbacks.UserInfoCallback;
import me.echeung.listenmoeapi.models.Song;
import me.echeung.listenmoeapi.responses.AuthResponse;
import me.echeung.listenmoeapi.responses.BaseResponse;
import me.echeung.listenmoeapi.responses.FavoriteResponse;
import me.echeung.listenmoeapi.responses.Messages;
import me.echeung.listenmoeapi.responses.SearchResponse;
import me.echeung.listenmoeapi.responses.UserFavoritesResponse;
import me.echeung.listenmoeapi.responses.UserResponse;
import me.echeung.listenmoeapi.services.ArtistsService;
import me.echeung.listenmoeapi.services.AuthService;
import me.echeung.listenmoeapi.services.FavoritesService;
import me.echeung.listenmoeapi.services.RequestsService;
import me.echeung.listenmoeapi.services.SongsService;
import me.echeung.listenmoeapi.services.UploadsService;
import me.echeung.listenmoeapi.services.UserService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {

    private static final String TAG = APIClient.class.getSimpleName();

    private static final String BASE_URL = "https://beta.listen.moe/api/";

    private static final String HEADER_USER_AGENT = "User-Agent";
    public static final String USER_AGENT = "me.echeung.moemoekyun";

    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE = "application/json";

    private static final String HEADER_ACCEPT = "Accept";
    private static final String ACCEPT = "application/vnd.listen.v4+json";

    private final AuthUtil authUtil;

    private final ArtistsService artistsService;
    private final AuthService authService;
    private final FavoritesService favoritesService;
    private final RequestsService requestsService;
    private final SongsService songsService;
    private final UploadsService uploadsService;
    private final UserService userService;

    private final RadioSocket socket;
    private final RadioStream stream;

    public APIClient(Context context, AuthUtil authUtil) {
        this.authUtil = authUtil;

        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(chain -> {
                    final Request request = chain.request();

                    final Request newRequest = request.newBuilder()
                            .addHeader(HEADER_USER_AGENT, USER_AGENT)
                            .addHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE)
                            .addHeader(HEADER_ACCEPT, ACCEPT)
                            .build();

                    return chain.proceed(newRequest);
                })
                .build();

        final Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addCallAdapterFactory(new ErrorHandlingAdapter.ErrorHandlingCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        artistsService = restAdapter.create(ArtistsService.class);
        authService = restAdapter.create(AuthService.class);
        favoritesService = restAdapter.create(FavoritesService.class);
        requestsService = restAdapter.create(RequestsService.class);
        songsService = restAdapter.create(SongsService.class);
        uploadsService = restAdapter.create(UploadsService.class);
        userService = restAdapter.create(UserService.class);

        socket = new RadioSocket(okHttpClient, authUtil);
        stream = new RadioStream(context);
    }

    /**
     * Authenticates to the radio.
     *
     * @param username User's username.
     * @param password User's password.
     * @param callback Listener to handle the response.
     */
    public void authenticate(final String username, final String password, final AuthCallback callback) {
        authService.login(new AuthService.LoginBody(username, password))
                .enqueue(new ErrorHandlingAdapter.WrappedCallback<AuthResponse>() {
                    @Override
                    public void success(final AuthResponse response) {
                        final String userToken = response.getToken();
                        callback.onSuccess(userToken);
                    }

                    @Override
                    public void error(final String message) {
                        Log.e(TAG, message);
                        callback.onFailure(message);
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
            callback.onFailure(Messages.AUTH_ERROR);
            return;
        }

        userService.getUserInfo(authUtil.getAuthToken())
                .enqueue(new ErrorHandlingAdapter.WrappedCallback<UserResponse>() {
                    @Override
                    public void success(final UserResponse response) {
                        callback.onSuccess(response);
                    }

                    @Override
                    public void error(final String message) {
                        Log.e(TAG, message);
                        callback.onFailure(message);
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
            callback.onFailure(Messages.AUTH_ERROR);
            return;
        }

        userService.getFavorites(authUtil.getAuthToken())
                .enqueue(new ErrorHandlingAdapter.WrappedCallback<UserFavoritesResponse>() {
                    @Override
                    public void success(final UserFavoritesResponse response) {
                        for (final Song song : response.getSongs()) {
//                            song.setFavorite(true);
                        }
                        callback.onSuccess(response);
                    }

                    @Override
                    public void error(final String message) {
                        Log.e(TAG, message);
                        callback.onFailure(message);
                    }
                });
    }

    /**
     * Toggles the favorited status of a song. If the song is already favorited, it will remove it and vice-versa.
     *
     * @param songId   Song to toggle.
     * @param callback Listener to handle the response.
     */
    public void favoriteSong(final String songId, final FavoriteSongCallback callback) {
        if (!authUtil.isAuthenticated()) {
            callback.onFailure(Messages.AUTH_ERROR);
            return;
        }

        favoritesService.favorite(authUtil.getAuthToken(), songId)
                .enqueue(new ErrorHandlingAdapter.WrappedCallback<FavoriteResponse>() {
                    @Override
                    public void success(final FavoriteResponse response) {
                        callback.onSuccess(response.isFavorite());
                    }

                    @Override
                    public void error(final String message) {
                        Log.e(TAG, message);
                        callback.onFailure(message);
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
            callback.onFailure(Messages.AUTH_ERROR);
            return;
        }

        requestsService.request(authUtil.getAuthToken(), songId)
                .enqueue(new ErrorHandlingAdapter.WrappedCallback<BaseResponse>() {
                    @Override
                    public void success(final BaseResponse response) {
                        callback.onSuccess();
                    }

                    @Override
                    public void error(final String message) {
                        Log.e(TAG, message);
                        callback.onFailure(message);
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
            callback.onFailure(Messages.AUTH_ERROR);
            return;
        }

        songsService.songs(authUtil.getAuthToken())
                .enqueue(new ErrorHandlingAdapter.WrappedCallback<SearchResponse>() {
                    @Override
                    public void success(final SearchResponse response) {
                        callback.onSuccess(response.getSongs());
                    }

                    @Override
                    public void error(final String message) {
                        Log.e(TAG, message);
                        callback.onFailure(message);
                    }
                });
    }

    public RadioSocket getSocket() {
        return socket;
    }

    public RadioStream getStream() {
        return stream;
    }
}
