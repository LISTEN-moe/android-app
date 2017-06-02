package jcotter.listenmoe.util;

import android.content.Context;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import jcotter.listenmoe.constants.Endpoints;
import jcotter.listenmoe.constants.ResponseMessages;
import jcotter.listenmoe.interfaces.AuthCallback;
import jcotter.listenmoe.interfaces.FavoriteSongCallback;
import jcotter.listenmoe.interfaces.RequestSongCallback;
import jcotter.listenmoe.interfaces.SearchCallback;
import jcotter.listenmoe.interfaces.UserFavoritesCallback;
import jcotter.listenmoe.interfaces.UserForumInfoCallback;
import jcotter.listenmoe.interfaces.UserInfoCallback;
import jcotter.listenmoe.model.AuthResponse;
import jcotter.listenmoe.model.Song;
import jcotter.listenmoe.model.SongsList;
import jcotter.listenmoe.model.UserForumInfo;
import jcotter.listenmoe.model.UserInfo;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Helper class for performing LISTEN.moe API calls.
 */
public class APIUtil {
    private static OkHttpClient http = new OkHttpClient();
    private static Gson gson = new Gson();

    /**
     * Authenticates to the radio.
     * /api/authenticate
     *
     * @param context  Android context to fetch SharedPreferences.
     * @param username User's username.
     * @param password User's password.
     * @param callback Listener to handle the response.
     */
    public static void authenticate(final Context context, final String username, final String password, final AuthCallback callback) {
        try {
            final String usernameE = URLEncoder.encode(username.trim(), "UTF-8");
            final String passwordE = URLEncoder.encode(password.trim(), "UTF-8");

            final Request request = new Request.Builder()
                    .url(Endpoints.AUTH)
                    .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), "username=" + usernameE + "&password=" + passwordE))
                    .build();

            http.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(final Call call, final IOException e) {
                    e.printStackTrace();
                    callback.onFailure(ResponseMessages.ERROR);
                }

                @Override
                public void onResponse(final Call call, final Response response) throws IOException {
                    final String body = response.body().string();

                    // Handle error messages
                    if (body.contains(ResponseMessages.INVALID_PASS)) {
                        callback.onFailure(ResponseMessages.INVALID_PASS);
                        return;
                    } else if (body.contains(ResponseMessages.INVALID_USER)) {
                        callback.onFailure(ResponseMessages.INVALID_USER);
                        return;
                    } else if (!body.contains(ResponseMessages.SUCCESS)) {
                        callback.onFailure(ResponseMessages.ERROR);
                        return;
                    }

                    // Get auth token from response
                    final String userToken = gson.fromJson(body, AuthResponse.class).getToken();
                    AuthUtil.setAuthToken(context, userToken);

                    callback.onSuccess(userToken);
                }
            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            callback.onFailure(ResponseMessages.ERROR);
        }
    }

    /**
     * Gets the user information such as id, username and in a not so distant future, preferences.
     * /api/user
     *
     * @param context  Android context to fetch SharedPreferences.
     * @param callback Listener to handle the response.
     */
    public static void getUserInfo(final Context context, final UserInfoCallback callback) {
        if (!AuthUtil.isAuthenticated(context)) {
            callback.onFailure(ResponseMessages.AUTH_ERROR);
            return;
        }

        final Request request = AuthUtil.createAuthRequest(context, Endpoints.USER)
                .get()
                .build();

        http.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                e.printStackTrace();
                callback.onFailure(ResponseMessages.ERROR);
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                callback.onSuccess(gson.fromJson(response.body().string(), UserInfo.class));
            }
        });
    }

    /**
     * Gets a user's avatar URL from the forum.
     *
     * @param context  Android context to fetch SharedPreferences.
     * @param username The user's username.
     * @param callback Listener to handle the response.
     */
    public static void getUserAvatar(final Context context, final String username, final UserForumInfoCallback callback) {
        if (!AuthUtil.isAuthenticated(context)) {
            callback.onFailure(ResponseMessages.AUTH_ERROR);
            return;
        }

        final Request request = new Request.Builder()
                .url(String.format(Endpoints.FORUM_USER, username))
                .get()
                .build();

        http.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                e.printStackTrace();
                callback.onFailure(ResponseMessages.ERROR);
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                callback.onSuccess(gson.fromJson(response.body().string(), UserForumInfo.class).getAvatarUrl());
            }
        });
    }

    /**
     * Gets a list of all the user's favorited songs.
     * /api/user/favorites
     *
     * @param context  Android context to fetch SharedPreferences.
     * @param callback Listener to handle the response.
     */
    public static void getUserFavorites(final Context context, final UserFavoritesCallback callback) {
        if (!AuthUtil.isAuthenticated(context)) {
            callback.onFailure(ResponseMessages.AUTH_ERROR);
            return;
        }

        final Request request = AuthUtil.createAuthRequest(context, Endpoints.USER_FAVORITES)
                .get()
                .build();

        http.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                e.printStackTrace();
                callback.onFailure(ResponseMessages.ERROR);
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                final SongsList songsList = APIUtil.parseSongJson(response.body().string());
                for (final Song song : songsList.getSongs()) {
                    song.setFavorite(true);
                }
                callback.onSuccess(songsList);
            }
        });
    }

    /**
     * Toggles the favorited status of a song. If the song is already on favorites, it will remove it and vice-versa.
     * /api/songs/favorite
     *
     * @param context  Android context to fetch SharedPreferences.
     * @param songId   Song to toggle.
     * @param callback Listener to handle the response.
     */
    public static void favoriteSong(final Context context, final int songId, final FavoriteSongCallback callback) {
        if (!AuthUtil.isAuthenticated(context)) {
            callback.onFailure(ResponseMessages.AUTH_ERROR);
            return;
        }

        final Request request = AuthUtil.createAuthRequest(context, Endpoints.FAVORITE)
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), "song=" + String.valueOf(songId)))
                .build();

        http.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                e.printStackTrace();
                callback.onFailure(ResponseMessages.ERROR);
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                final String jsonResult = response.body().string();

                if (jsonResult.contains(ResponseMessages.SUCCESS)) {
                    callback.onSuccess(jsonResult.contains(ResponseMessages.FAVORITED));
                } else if (jsonResult.contains(ResponseMessages.AUTH_FAILURE)) {
                    callback.onFailure(ResponseMessages.AUTH_FAILURE);
                } else {
                    callback.onFailure(ResponseMessages.ERROR);
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
     * @param callback Listener to handle the response.
     */
    public static void requestSong(final Context context, final int songId, final RequestSongCallback callback) {
        if (!AuthUtil.isAuthenticated(context)) {
            callback.onFailure(ResponseMessages.AUTH_ERROR);
            return;
        }

        final Request request = AuthUtil.createAuthRequest(context, Endpoints.REQUEST)
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), "song=" + String.valueOf(songId)))
                .build();

        http.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                e.printStackTrace();
                callback.onFailure(ResponseMessages.ERROR);
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                final String jsonResult = response.body().string();

                if (jsonResult.contains(ResponseMessages.SUCCESS)) {
                    callback.onSuccess();
                } else {
                    if (jsonResult.contains(ResponseMessages.USER_NOT_SUPPORTER)) {
                        callback.onFailure(ResponseMessages.USER_NOT_SUPPORTER);
                    } else {
                        callback.onFailure(ResponseMessages.ERROR);
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
     * @param callback Listener to handle the response.
     */
    public static void search(final Context context, final String query, final SearchCallback callback) {
        if (!AuthUtil.isAuthenticated(context)) {
            callback.onFailure(ResponseMessages.AUTH_ERROR);
            return;
        }

        final Request request = AuthUtil.createAuthRequest(context, Endpoints.SEARCH)
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), "query=" + query))
                .build();

        http.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                e.printStackTrace();
                callback.onFailure(ResponseMessages.ERROR);
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                final SongsList songsList = APIUtil.parseSongJson(response.body().string());
                callback.onSuccess(songsList.getSongs());
            }
        });
    }

    /**
     * For internal use: parses the JSON with a list of songs.
     *
     * @param jsonString JSON returned from the API.
     * @return A SongsList object.
     */
    private static SongsList parseSongJson(final String jsonString) {
        return gson.fromJson(jsonString, SongsList.class);
    }
}
