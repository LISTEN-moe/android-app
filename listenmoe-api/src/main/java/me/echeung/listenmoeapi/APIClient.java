package me.echeung.listenmoeapi;

import android.util.Log;

import me.echeung.listenmoeapi.interfaces.AuthListener;
import me.echeung.listenmoeapi.interfaces.FavoriteSongListener;
import me.echeung.listenmoeapi.interfaces.RequestSongListener;
import me.echeung.listenmoeapi.interfaces.SearchListener;
import me.echeung.listenmoeapi.interfaces.UserFavoritesListener;
import me.echeung.listenmoeapi.interfaces.UserInfoListener;
import me.echeung.listenmoeapi.models.Song;
import me.echeung.listenmoeapi.responses.AuthResponse;
import me.echeung.listenmoeapi.responses.BaseResponse;
import me.echeung.listenmoeapi.responses.FavoriteResponse;
import me.echeung.listenmoeapi.responses.Messages;
import me.echeung.listenmoeapi.responses.SearchResponse;
import me.echeung.listenmoeapi.responses.UserFavoritesResponse;
import me.echeung.listenmoeapi.responses.UserResponse;
import me.echeung.listenmoeapi.services.AuthService;
import me.echeung.listenmoeapi.services.SongsService;
import me.echeung.listenmoeapi.services.UserService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {

    private static final String TAG = APIClient.class.getSimpleName();

    private static final String BASE_URL = "https://listen.moe/api/";

    private static final String HEADER_USER_AGENT = "User-Agent";
    private static final String USER_AGENT = "me.echeung.moemoekyun";

    private final APIHelper helper;

    private final AuthService authService;
    private final SongsService songsService;
    private final UserService userService;

    public APIClient(APIHelper helper) {
        this.helper = helper;

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
     * @param username User's username.
     * @param password User's password.
     * @param listener Listener to handle the response.
     */
    public void authenticate(final String username, final String password, final AuthListener listener) {
        authService.login(username, password)
                .enqueue(new ErrorHandlingAdapter.WrappedCallback<AuthResponse>() {
                    @Override
                    public void success(Response<AuthResponse> response) {
                        final String userToken = response.body().getToken();
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
     * @param listener Listener to handle the response.
     */
    public void getUserInfo(final UserInfoListener listener) {
        if (!helper.isAuthenticated()) {
            listener.onFailure(Messages.AUTH_ERROR);
            return;
        }

        userService.getUserInfo(helper.getAuthToken())
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
     * @param listener Listener to handle the response.
     */
    public void getUserFavorites(final UserFavoritesListener listener) {
        if (!helper.isAuthenticated()) {
            listener.onFailure(Messages.AUTH_ERROR);
            return;
        }

        userService.getFavorites(helper.getAuthToken())
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
     * @param songId   Song to toggle.
     * @param listener Listener to handle the response.
     */
    public void favoriteSong(final int songId, final FavoriteSongListener listener) {
        if (!helper.isAuthenticated()) {
            listener.onFailure(Messages.AUTH_ERROR);
            return;
        }

        songsService.favorite(helper.getAuthToken(), songId)
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
     * @param songId   Song to request.
     * @param listener Listener to handle the response.
     */
    public void requestSong(final int songId, final RequestSongListener listener) {
        if (!helper.isAuthenticated()) {
            listener.onFailure(Messages.AUTH_ERROR);
            return;
        }

        songsService.request(helper.getAuthToken(), songId)
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
     * @param query    Search query string.
     * @param listener Listener to handle the response.
     */
    public void search(final String query, final SearchListener listener) {
        if (!helper.isAuthenticated()) {
            listener.onFailure(Messages.AUTH_ERROR);
            return;
        }

        songsService.search(helper.getAuthToken(), query)
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

    public interface APIHelper {
        boolean isAuthenticated();
        String getAuthToken();
    }
}
