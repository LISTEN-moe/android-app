package me.echeung.moemoekyun.util;

import android.content.Context;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import me.echeung.moemoekyun.constants.Endpoints;
import me.echeung.moemoekyun.constants.ResponseMessages;
import me.echeung.moemoekyun.interfaces.AuthListener;
import me.echeung.moemoekyun.interfaces.FavoriteSongListener;
import me.echeung.moemoekyun.interfaces.RequestSongListener;
import me.echeung.moemoekyun.interfaces.SearchListener;
import me.echeung.moemoekyun.interfaces.UserFavoritesListener;
import me.echeung.moemoekyun.interfaces.UserForumInfoListener;
import me.echeung.moemoekyun.interfaces.UserInfoListener;
import me.echeung.moemoekyun.model.AuthResponse;
import me.echeung.moemoekyun.model.Song;
import me.echeung.moemoekyun.model.SongsList;
import me.echeung.moemoekyun.model.UserForumInfo;
import me.echeung.moemoekyun.model.UserInfo;
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
     * @param listener Listener to handle the response.
     */
    public static void authenticate(final Context context, final String username, final String password, final AuthListener listener) {
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
                    listener.onFailure(ResponseMessages.ERROR);
                }

                @Override
                public void onResponse(final Call call, final Response response) throws IOException {
                    final String body = response.body().string();

                    // Handle error messages
                    if (body.contains(ResponseMessages.INVALID_PASS)) {
                        listener.onFailure(ResponseMessages.INVALID_PASS);
                        return;
                    } else if (body.contains(ResponseMessages.INVALID_USER)) {
                        listener.onFailure(ResponseMessages.INVALID_USER);
                        return;
                    } else if (!body.contains(ResponseMessages.SUCCESS)) {
                        listener.onFailure(ResponseMessages.ERROR);
                        return;
                    }

                    // Get auth token from response
                    final String userToken = gson.fromJson(body, AuthResponse.class).getToken();
                    AuthUtil.setAuthToken(context, userToken);

                    listener.onSuccess(userToken);
                }
            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            listener.onFailure(ResponseMessages.ERROR);
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
            listener.onFailure(ResponseMessages.AUTH_ERROR);
            return;
        }

        final Request request = AuthUtil.createAuthRequest(context, Endpoints.USER)
                .get()
                .build();

        http.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                e.printStackTrace();
                listener.onFailure(ResponseMessages.ERROR);
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                listener.onSuccess(gson.fromJson(response.body().string(), UserInfo.class));
            }
        });
    }

    /**
     * Gets a user's avatar URL from the forum.
     *
     * @param context  Android context to fetch SharedPreferences.
     * @param username The user's username.
     * @param listener Listener to handle the response.
     */
    public static void getUserAvatar(final Context context, final String username, final UserForumInfoListener listener) {
        if (!AuthUtil.isAuthenticated(context)) {
            listener.onFailure(ResponseMessages.AUTH_ERROR);
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
                listener.onFailure(ResponseMessages.ERROR);
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                if (response.isSuccessful()) {
                    // TODO: check if avatar exists
                    listener.onSuccess(gson.fromJson(response.body().string(), UserForumInfo.class).getAvatarUrl());
                } else {
                    // TODO: pass back default avatar
                    listener.onFailure(ResponseMessages.ERROR);
                }
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
            listener.onFailure(ResponseMessages.AUTH_ERROR);
            return;
        }

        final Request request = AuthUtil.createAuthRequest(context, Endpoints.USER_FAVORITES)
                .get()
                .build();

        http.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                e.printStackTrace();
                listener.onFailure(ResponseMessages.ERROR);
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                final SongsList songsList = APIUtil.parseSongJson(response.body().string());
                if (songsList == null) {
                    listener.onFailure(ResponseMessages.ERROR);
                    return;
                }
                for (final Song song : songsList.getSongs()) {
                    song.setFavorite(true);
                }
                listener.onSuccess(songsList);
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
            listener.onFailure(ResponseMessages.AUTH_ERROR);
            return;
        }

        final Request request = AuthUtil.createAuthRequest(context, Endpoints.FAVORITE)
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), "song=" + String.valueOf(songId)))
                .build();

        http.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                e.printStackTrace();
                listener.onFailure(ResponseMessages.ERROR);
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                final String jsonResult = response.body().string();

                if (jsonResult.contains(ResponseMessages.SUCCESS)) {
                    listener.onSuccess(jsonResult.contains(ResponseMessages.FAVORITED));
                } else if (jsonResult.contains(ResponseMessages.AUTH_FAILURE)) {
                    listener.onFailure(ResponseMessages.AUTH_FAILURE);
                } else {
                    listener.onFailure(ResponseMessages.ERROR);
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
            listener.onFailure(ResponseMessages.AUTH_ERROR);
            return;
        }

        final Request request = AuthUtil.createAuthRequest(context, Endpoints.REQUEST)
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), "song=" + String.valueOf(songId)))
                .build();

        http.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                e.printStackTrace();
                listener.onFailure(ResponseMessages.ERROR);
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                final String jsonResult = response.body().string();

                if (jsonResult.contains(ResponseMessages.SUCCESS)) {
                    listener.onSuccess();
                } else {
                    if (jsonResult.contains(ResponseMessages.USER_NOT_SUPPORTER)) {
                        listener.onFailure(ResponseMessages.USER_NOT_SUPPORTER);
                    } else {
                        listener.onFailure(ResponseMessages.ERROR);
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
            listener.onFailure(ResponseMessages.AUTH_ERROR);
            return;
        }

        final Request request = AuthUtil.createAuthRequest(context, Endpoints.SEARCH)
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), "query=" + query))
                .build();

        http.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                e.printStackTrace();
                listener.onFailure(ResponseMessages.ERROR);
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                final SongsList songsList = APIUtil.parseSongJson(response.body().string());
                listener.onSuccess(songsList.getSongs());
            }
        });
    }

    /**
     * For internal use: parses the JSON with a list of songs.
     *
     * @param jsonString JSON returned from the API.
     * @return A SongsList object, or null if there was an authentication error.
     */
    private static SongsList parseSongJson(final String jsonString) {
        if (jsonString.contains(ResponseMessages.AUTH_FAILURE)) {
            // TODO: log user out?
            return null;
        }
        return gson.fromJson(jsonString, SongsList.class);
    }
}
