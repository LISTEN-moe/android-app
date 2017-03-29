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

    private static OkHttpClient http = new OkHttpClient();
    private static Gson gson = new Gson();

    /**
     * Authenticates to the radio.
     * /api/authenticate
     *
     * @param username User's username.
     * @param password User's password.
     */
    public static void authenticate(final Context context, final String username, final String password, final AuthCallback callback) {
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
     */
    public static void getUserInfo(final Context context, final UserInfoCallback callback) {
        if (!AuthUtil.isAuthenticated(context)) {
            callback.onFailure(ResponseMessages.AUTH_ERROR);
            return;
        }

        Request request = AuthUtil.createAuthRequest(context, Endpoints.USER)
                .get()
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
    public static void getUserFavorites(final Context context, final UserFavoritesCallback callback) {
        if (!AuthUtil.isAuthenticated(context)) {
            callback.onFailure(ResponseMessages.AUTH_ERROR);
            return;
        }

        Request request = AuthUtil.createAuthRequest(context, Endpoints.USER_FAVORITES)
                .get()
                .build();

        http.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                callback.onFailure(ResponseMessages.ERROR);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                SongsList songsList = APIUtil.parseSongJson(response.body().string());
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
    public static void favoriteSong(final Context context, final int songId, final FavoriteSongCallback callback) {
        if (!AuthUtil.isAuthenticated(context)) {
            callback.onFailure(ResponseMessages.AUTH_ERROR);
            return;
        }

        Request request = AuthUtil.createAuthRequest(context, Endpoints.FAVORITE)
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), "song=" + String.valueOf(songId)))
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
    public static void requestSong(final Context context, final int songId, final RequestSongCallback callback) {
        if (!AuthUtil.isAuthenticated(context)) {
            callback.onFailure(ResponseMessages.AUTH_ERROR);
            return;
        }

        Request request = AuthUtil.createAuthRequest(context, Endpoints.REQUEST)
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), "song=" + String.valueOf(songId)))
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
    public static void search(final Context context, final String query, final SearchCallback callback) {
        if (!AuthUtil.isAuthenticated(context)) {
            callback.onFailure(ResponseMessages.AUTH_ERROR);
            return;
        }

        Request request = AuthUtil.createAuthRequest(context, Endpoints.SEARCH)
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), "query=" + query))
                .build();

        http.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                callback.onFailure(ResponseMessages.ERROR);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                SongsList songsList = APIUtil.parseSongJson(response.body().string());
                callback.onSuccess(songsList.getSongs());
            }
        });
    }

    private static SongsList parseSongJson(final String jsonString) {
        SongsList songsList = gson.fromJson(jsonString, SongsList.class);
        return songsList;
    }
}
