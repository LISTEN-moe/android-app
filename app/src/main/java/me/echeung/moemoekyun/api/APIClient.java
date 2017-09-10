package me.echeung.moemoekyun.api;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import me.echeung.moemoekyun.api.interfaces.AuthListener;
import me.echeung.moemoekyun.api.interfaces.FavoriteSongListener;
import me.echeung.moemoekyun.api.interfaces.RequestSongListener;
import me.echeung.moemoekyun.api.interfaces.SearchListener;
import me.echeung.moemoekyun.api.interfaces.UserFavoritesListener;
import me.echeung.moemoekyun.api.interfaces.UserInfoListener;
import me.echeung.moemoekyun.api.models.Song;
import me.echeung.moemoekyun.api.models.UserFavorites;
import me.echeung.moemoekyun.api.models.UserInfo;
import me.echeung.moemoekyun.api.responses.AuthResponse;
import me.echeung.moemoekyun.api.responses.Messages;
import me.echeung.moemoekyun.api.services.AuthService;
import me.echeung.moemoekyun.api.services.SongsService;
import me.echeung.moemoekyun.api.services.UserService;
import me.echeung.moemoekyun.constants.Endpoints;
import me.echeung.moemoekyun.utils.AuthUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
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

    public AuthService getAuthService() {
        return authService;
    }

    public SongsService getSongsService() {
        return songsService;
    }

    public UserService getUserService() {
        return userService;
    }

    /*
    MyCall<Ip> ip = service.getIp();
    ip.enqueue(new MyCallback<Ip>() {
      @Override public void success(Response<Ip> response) {
        System.out.println("SUCCESS! " + response.body().origin);
      }

      @Override public void unauthenticated(Response<?> response) {
        System.out.println("UNAUTHENTICATED");
      }

      @Override public void clientError(Response<?> response) {
        System.out.println("CLIENT ERROR " + response.code() + " " + response.message());
      }

      @Override public void serverError(Response<?> response) {
        System.out.println("SERVER ERROR " + response.code() + " " + response.message());
      }

      @Override public void networkError(IOException e) {
        System.err.println("NETWORK ERROR " + e.getMessage());
      }

      @Override public void unexpectedError(Throwable t) {
        System.err.println("FATAL ERROR " + t.getMessage());
      }
    });
     */

    private static final Gson GSON = new Gson();

    /**
     * Authenticates to the radio.
     * /api/authenticate
     *
     * @param context  Android context to fetch SharedPreferences.
     * @param username User's username.
     * @param password User's password.
     * @param listener Listener to handle the response.
     */
    public static void authenticate(final Context context, final String username, final String password, final AuthListener listener) {
        try {
            final String usernameE = URLEncoder.encode(username.trim(), "UTF-8");
            final String passwordE = URLEncoder.encode(password.trim(), "UTF-8");

            final Request request = RequestUtil.builder(Endpoints.AUTH)
                    .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), "username=" + usernameE + "&password=" + passwordE))
                    .build();

            RequestUtil.getClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(final Call call, final IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                    listener.onFailure(Messages.ERROR);
                }

                @Override
                public void onResponse(final Call call, final Response response) throws IOException {
                    final String body = response.body().string();

                    // Handle error messages
                    if (body.contains(Messages.INVALID_PASS)) {
                        listener.onFailure(Messages.INVALID_PASS);
                        return;
                    } else if (body.contains(Messages.INVALID_USER)) {
                        listener.onFailure(Messages.INVALID_USER);
                        return;
                    } else if (!body.contains(Messages.SUCCESS)) {
                        listener.onFailure(Messages.ERROR);
                        return;
                    }

                    // Get auth token from response
                    final String userToken = GSON.fromJson(body, AuthResponse.class).getToken();
                    AuthUtil.setAuthToken(context, userToken);

                    listener.onSuccess(userToken);
                }
            });
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage(), e);
            listener.onFailure(Messages.ERROR);
        }
    }

    /**
     * Gets the user information such as id, username and in a not so distant future, preferences.
     * /api/user
     *
     * @param context  Android context to fetch SharedPreferences.
     * @param listener Listener to handle the response.
     */
    public static void getUserInfo(final Context context, final UserInfoListener listener) {
        if (!AuthUtil.isAuthenticated(context)) {
            listener.onFailure(Messages.AUTH_ERROR);
            return;
        }

        final Request request = RequestUtil.authBuilder(context, Endpoints.USER)
                .get()
                .build();

        RequestUtil.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                Log.e(TAG, e.getMessage(), e);
                listener.onFailure(Messages.ERROR);
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                final String jsonString = response.body().string();
                listener.onSuccess(GSON.fromJson(jsonString, UserInfo.class));
            }
        });
    }

    /**
     * Gets a list of all the user's favorited songs.
     * /api/user/favorites
     *
     * @param context  Android context to fetch SharedPreferences.
     * @param listener Listener to handle the response.
     */
    public static void getUserFavorites(final Context context, final UserFavoritesListener listener) {
        if (!AuthUtil.isAuthenticated(context)) {
            listener.onFailure(Messages.AUTH_ERROR);
            return;
        }

        final Request request = RequestUtil.authBuilder(context, Endpoints.USER_FAVORITES)
                .get()
                .build();

        RequestUtil.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                Log.e(TAG, e.getMessage(), e);
                listener.onFailure(Messages.ERROR);
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                final UserFavorites userFavorites = APIClient.parseSongJson(context, response.body().string());
                if (userFavorites == null) {
                    listener.onFailure(Messages.ERROR);
                    return;
                }
                for (final Song song : userFavorites.getSongs()) {
                    song.setFavorite(true);
                }
                listener.onSuccess(userFavorites);
            }
        });
    }

    /**
     * Toggles the favorited status of a song. If the song is already on favorites, it will remove it and vice-versa.
     * /api/songs/favorite
     *
     * @param context  Android context to fetch SharedPreferences.
     * @param songId   Song to toggle.
     * @param listener Listener to handle the response.
     */
    public static void favoriteSong(final Context context, final int songId, final FavoriteSongListener listener) {
        if (!AuthUtil.isAuthenticated(context)) {
            listener.onFailure(Messages.AUTH_ERROR);
            return;
        }

        final Request request = RequestUtil.authBuilder(context, Endpoints.FAVORITE)
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), "song=" + String.valueOf(songId)))
                .build();

        RequestUtil.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                Log.e(TAG, e.getMessage(), e);
                listener.onFailure(Messages.ERROR);
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                final String jsonResult = response.body().string();

                if (jsonResult.contains(Messages.SUCCESS)) {
                    listener.onSuccess(jsonResult.contains(Messages.FAVORITED));
                } else if (jsonResult.contains(Messages.AUTH_FAILURE)) {
                    listener.onFailure(Messages.AUTH_FAILURE);
                } else {
                    listener.onFailure(Messages.ERROR);
                }
            }
        });
    }

    /**
     * Sends a song request to the queue.
     * /api/songs/request
     *
     * @param context  Android context to fetch SharedPreferences.
     * @param songId   Song to request.
     * @param listener Listener to handle the response.
     */
    public static void requestSong(final Context context, final int songId, final RequestSongListener listener) {
        if (!AuthUtil.isAuthenticated(context)) {
            listener.onFailure(Messages.AUTH_ERROR);
            return;
        }

        final Request request = RequestUtil.authBuilder(context, Endpoints.REQUEST)
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), "song=" + String.valueOf(songId)))
                .build();

        RequestUtil.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                Log.e(TAG, e.getMessage(), e);
                listener.onFailure(Messages.ERROR);
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                final String jsonResult = response.body().string();

                if (jsonResult.contains(Messages.SUCCESS)) {
                    listener.onSuccess();
                } else {
                    if (jsonResult.contains(Messages.USER_NOT_SUPPORTER)) {
                        listener.onFailure(Messages.USER_NOT_SUPPORTER);
                    } else {
                        listener.onFailure(Messages.ERROR);
                    }
                }
            }
        });
    }

    /**
     * Searches for a song.
     * /api/songs/search
     *
     * @param context  Android context to fetch SharedPreferences.
     * @param query    Search query string.
     * @param listener Listener to handle the response.
     */
    public static void search(final Context context, final String query, final SearchListener listener) {
        if (!AuthUtil.isAuthenticated(context)) {
            listener.onFailure(Messages.AUTH_ERROR);
            return;
        }

        final Request request = RequestUtil.authBuilder(context, Endpoints.SEARCH)
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), "query=" + query))
                .build();

        RequestUtil.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                Log.e(TAG, e.getMessage(), e);
                listener.onFailure(Messages.ERROR);
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                final UserFavorites userFavorites = APIClient.parseSongJson(context, response.body().string());
                listener.onSuccess(userFavorites.getSongs());
            }
        });
    }

    /**
     * For internal use: parses the JSON with a list of songs.
     *
     * @param context    Android context to fetch SharedPreferences.
     * @param jsonString JSON returned from the API.
     * @return A UserFavorites object, or null if there was an authentication error.
     */
    private static UserFavorites parseSongJson(final Context context, final String jsonString) {
        if (jsonString.contains(Messages.AUTH_FAILURE)) {
            AuthUtil.clearAuthToken(context);
            return null;
        }

        try {
            return GSON.fromJson(jsonString, UserFavorites.class);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        }
    }
}
