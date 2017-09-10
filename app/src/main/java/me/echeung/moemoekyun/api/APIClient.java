package me.echeung.moemoekyun.api;

import android.content.Context;
import android.util.Log;

import me.echeung.moemoekyun.api.interfaces.AuthListener;
import me.echeung.moemoekyun.api.interfaces.FavoriteSongListener;
import me.echeung.moemoekyun.api.interfaces.RequestSongListener;
import me.echeung.moemoekyun.api.interfaces.SearchListener;
import me.echeung.moemoekyun.api.interfaces.UserFavoritesListener;
import me.echeung.moemoekyun.api.interfaces.UserInfoListener;
import me.echeung.moemoekyun.api.models.Song;
import me.echeung.moemoekyun.api.responses.AuthResponse;
import me.echeung.moemoekyun.api.responses.BaseResponse;
import me.echeung.moemoekyun.api.responses.FavoriteResponse;
import me.echeung.moemoekyun.api.responses.Messages;
import me.echeung.moemoekyun.api.responses.SearchResponse;
import me.echeung.moemoekyun.api.responses.UserFavoritesResponse;
import me.echeung.moemoekyun.api.responses.UserResponse;
import me.echeung.moemoekyun.api.services.AuthService;
import me.echeung.moemoekyun.api.services.SongsService;
import me.echeung.moemoekyun.api.services.UserService;
import me.echeung.moemoekyun.utils.AuthUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Helper class for performing LISTEN.moe API calls.
 */
public class APIClient {

    private static final String TAG = APIClient.class.getSimpleName();

    private static final String BASE_URL = "https://listen.moe/api/";

    private static final String HEADER_USER_AGENT = "User-Agent";
    private static final String USER_AGENT = "me.echeung.moemoekyun";

    private final AuthService authService;
    private final SongsService songsService;
    private final UserService userService;

    public APIClient() {
        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(chain -> {
                    final Request request = chain.request();

                    final Request newRequest = request.newBuilder()
                            .addHeader(HEADER_USER_AGENT, USER_AGENT)
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

        authService = restAdapter.create(AuthService.class);
        songsService = restAdapter.create(SongsService.class);
        userService = restAdapter.create(UserService.class);
    }

    /**
     * Authenticates to the radio.
     *
     * @param context  Android context to fetch SharedPreferences.
     * @param username User's username.
     * @param password User's password.
     * @param listener Listener to handle the response.
     */
    public void authenticate(final Context context, final String username, final String password, final AuthListener listener) {
        authService.login(username, password)
                .enqueue(new ErrorHandlingAdapter.WrappedCallback<AuthResponse>() {
                    @Override
                    public void success(Response<AuthResponse> response) {
                        final String userToken = response.body().getToken();
                        AuthUtil.setAuthToken(context, userToken);

                        listener.onSuccess(userToken);
                    }

                    @Override
                    public void error(String message) {
                        Log.e(TAG, message);
                        listener.onFailure(message);
                    }
                });
    }

    /**
     * Gets the user information (id and username).
     *
     * @param context  Android context.
     * @param listener Listener to handle the response.
     */
    public void getUserInfo(final Context context, final UserInfoListener listener) {
        if (!AuthUtil.isAuthenticated(context)) {
            listener.onFailure(Messages.AUTH_ERROR);
            return;
        }

        userService.getUserInfo(AuthUtil.getAuthToken(context))
                .enqueue(new ErrorHandlingAdapter.WrappedCallback<UserResponse>() {
                    @Override
                    public void success(Response<UserResponse> response) {
                        listener.onSuccess(response.body());
                    }

                    @Override
                    public void error(String message) {
                        Log.e(TAG, message);
                        listener.onFailure(message);
                    }
                });
    }

    /**
     * Gets a list of all the user's favorited songs.
     *
     * @param context  Android context.
     * @param listener Listener to handle the response.
     */
    public void getUserFavorites(final Context context, final UserFavoritesListener listener) {
        if (!AuthUtil.isAuthenticated(context)) {
            listener.onFailure(Messages.AUTH_ERROR);
            return;
        }

        userService.getFavorites(AuthUtil.getAuthToken(context))
                .enqueue(new ErrorHandlingAdapter.WrappedCallback<UserFavoritesResponse>() {
                    @Override
                    public void success(Response<UserFavoritesResponse> response) {
                        final UserFavoritesResponse userFavorites = response.body();
                        for (final Song song : userFavorites.getSongs()) {
                            song.setFavorite(true);
                        }
                        listener.onSuccess(userFavorites);
                    }

                    @Override
                    public void error(String message) {
                        Log.e(TAG, message);
                        listener.onFailure(message);
                    }
                });
    }

    /**
     * Toggles the favorited status of a song. If the song is already favorited, it will remove it and vice-versa.
     *
     * @param context  Android context.
     * @param songId   Song to toggle.
     * @param listener Listener to handle the response.
     */
    public void favoriteSong(final Context context, final int songId, final FavoriteSongListener listener) {
        if (!AuthUtil.isAuthenticated(context)) {
            listener.onFailure(Messages.AUTH_ERROR);
            return;
        }

        songsService.favorite(AuthUtil.getAuthToken(context), songId)
                .enqueue(new ErrorHandlingAdapter.WrappedCallback<FavoriteResponse>() {
                    @Override
                    public void success(Response<FavoriteResponse> response) {
                        listener.onSuccess(response.body().isFavorite());
                    }

                    @Override
                    public void error(String message) {
                        Log.e(TAG, message);
                        listener.onFailure(message);
                    }
                });
    }

    /**
     * Sends a song request to the queue.
     *
     * @param context  Android context.
     * @param songId   Song to request.
     * @param listener Listener to handle the response.
     */
    public void requestSong(final Context context, final int songId, final RequestSongListener listener) {
        if (!AuthUtil.isAuthenticated(context)) {
            listener.onFailure(Messages.AUTH_ERROR);
            return;
        }

        songsService.request(AuthUtil.getAuthToken(context), songId)
                .enqueue(new ErrorHandlingAdapter.WrappedCallback<BaseResponse>() {
                    @Override
                    public void success(Response<BaseResponse> response) {
                        listener.onSuccess();
                    }

                    @Override
                    public void error(String message) {
                        Log.e(TAG, message);
                        listener.onFailure(message);
                    }
                });
    }

    /**
     * Searches for songs.
     *
     * @param context  Android context.
     * @param query    Search query string.
     * @param listener Listener to handle the response.
     */
    public void search(final Context context, final String query, final SearchListener listener) {
        if (!AuthUtil.isAuthenticated(context)) {
            listener.onFailure(Messages.AUTH_ERROR);
            return;
        }

        songsService.search(AuthUtil.getAuthToken(context), query)
                .enqueue(new ErrorHandlingAdapter.WrappedCallback<SearchResponse>() {
                    @Override
                    public void success(Response<SearchResponse> response) {
                        listener.onSuccess(response.body().getSongs());
                    }

                    @Override
                    public void error(String message) {
                        Log.e(TAG, message);
                        listener.onFailure(message);
                    }
                });
    }
}
