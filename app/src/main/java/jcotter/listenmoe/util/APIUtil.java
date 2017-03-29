package jcotter.listenmoe.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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
import jcotter.listenmoe.interfaces.UserInfoCallback;
import jcotter.listenmoe.model.SongsList;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class APIUtil {
    private final String USER_TOKEN = "userToken";

    private OkHttpClient http;
    private Gson gson;
    private SharedPreferences sharedPrefs;

    public APIUtil(Context context) {
        this.http = new OkHttpClient();
        this.gson = new Gson();
        this.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Authenticates to the radio.
     * /api/authenticate
     *
     * @param username User's username.
     * @param password User's password.
     */
    public void authenticate(final String username, final String password, final AuthCallback callback) {
        try {
            String usernameE = URLEncoder.encode(username.trim(), "UTF-8");
            String passwordE = URLEncoder.encode(password.trim(), "UTF-8");

            Request request = new Request.Builder()
                    .url(Endpoints.AUTH)
                    .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), "password=" + passwordE + "&username=" + usernameE))
                    .build();

            http.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    callback.onFailure(ResponseMessages.ERROR);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String body = response.body().string();
                    if (body.contains(ResponseMessages.INVALID_PASS)) {
                        callback.onFailure(ResponseMessages.INVALID_PASS);
                        return;
                    } else if (body.contains(ResponseMessages.INVALID_USER)) {
                        callback.onFailure(ResponseMessages.INVALID_USER);
                        return;
                    }

                    final String userToken = body.substring(25, body.length() - 2);
                    SharedPreferences.Editor editor = sharedPrefs.edit()
                            .putString(USER_TOKEN, userToken)
                            .putLong("lastAuth", System.currentTimeMillis() / 1000);
                    editor.apply();

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
     */
    public void getUserInfo(final UserInfoCallback callback) {
        final String userToken = sharedPrefs.getString(USER_TOKEN, null);
        if (userToken == null) {
            callback.onFailure(ResponseMessages.AUTH_ERROR);
            return;
        }

        Request request = new Request.Builder()
                .url(Endpoints.USER)
                .get()
                .addHeader("authorization", userToken)
                .build();

        http.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                callback.onFailure(ResponseMessages.ERROR);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // TODO: parse as JSONObject
                callback.onSuccess(response.body().string());
            }
        });
    }

    /**
     * Gets a list of all the user's favorited songs.
     * /api/user/favorites
     */
    public void getUserFavorites(final UserFavoritesCallback callback) {
        final String userToken = sharedPrefs.getString(USER_TOKEN, null);
        if (userToken == null) {
            callback.onFailure(ResponseMessages.AUTH_ERROR);
            return;
        }

        Request request = new Request.Builder()
                .url(Endpoints.USER_FAVORITES)
                .get()
                .addHeader("authorization", userToken)
                .build();

        http.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                callback.onFailure(ResponseMessages.ERROR);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                SongsList songsList = parseSongJson(response.body().string());
                callback.onSuccess(songsList.getSongs());
            }
        });
    }

    /**
     * Toggles the favorited status of a song. If the song is already on favorites, it will remove it and vice-versa.
     * /api/songs/favorite
     *
     * @param songId Song to toggle.
     */
    public void favoriteSong(final int songId, final FavoriteSongCallback callback) {
        final String userToken = sharedPrefs.getString(USER_TOKEN, null);
        if (userToken == null) {
            callback.onFailure(ResponseMessages.AUTH_ERROR);
            return;
        }

        Request request = new Request.Builder()
                .url(Endpoints.FAVORITE)
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), "song=" + String.valueOf(songId)))
                .addHeader("authorization", userToken)
                .build();

        http.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                callback.onFailure(ResponseMessages.ERROR);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callback.onSuccess(response.body().string());
            }
        });
    }

    /**
     * Sends a song request to the queue.
     * /api/songs/request
     *
     * @param songId Song to request.
     */
    public void requestSong(final int songId, final RequestSongCallback callback) {
        final String userToken = sharedPrefs.getString(USER_TOKEN, null);
        if (userToken == null) {
            callback.onFailure(ResponseMessages.AUTH_ERROR);
            return;
        }

        Request request = new Request.Builder()
                .url(Endpoints.REQUEST)
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), "song=" + String.valueOf(songId)))
                .addHeader("authorization", userToken)
                .build();

        http.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                callback.onFailure(ResponseMessages.ERROR);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callback.onSuccess(response.body().string());
            }
        });
    }

    /**
     * Searches for a song.
     * /api/songs/search
     *
     * @param query Search query string.
     */
    public void search(final String query, final SearchCallback callback) {
        final String userToken = sharedPrefs.getString(USER_TOKEN, null);
        if (userToken == null) {
            callback.onFailure(ResponseMessages.AUTH_ERROR);
            return;
        }

        Request request = new Request.Builder()
                .url(Endpoints.SEARCH)
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), "query=" + query))
                .addHeader("authorization", userToken)
                .build();

        http.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                callback.onFailure(ResponseMessages.ERROR);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                SongsList songsList = parseSongJson(response.body().string());
                callback.onSuccess(songsList.getSongs());
            }
        });
    }

    private SongsList parseSongJson(final String jsonString) {
        SongsList songsList = gson.fromJson(jsonString, SongsList.class);
        return songsList;
    }
}
