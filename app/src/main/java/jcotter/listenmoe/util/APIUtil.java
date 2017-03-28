package jcotter.listenmoe.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import jcotter.listenmoe.R;
import jcotter.listenmoe.constants.AuthMessages;
import jcotter.listenmoe.constants.Endpoints;
import jcotter.listenmoe.interfaces.IAPIListener;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class APIUtil {
    private IAPIListener apiListener;
    private OkHttpClient http;

    public APIUtil(IAPIListener apiListener) {
        this.apiListener = apiListener;
        this.http = new OkHttpClient();
    }

    public void favoriteList(Context applicationContext) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        final String userToken = sharedPreferences.getString("userToken", "NULL");
        if (userToken.equals("NULL")) {
            apiListener.favoriteCallback("NULL");
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
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                apiListener.favoriteListCallback(response.body().string());
            }
        });
    }

    public void authenticate(final Context applicationContext, final String username, final String password) {
        try {
            String passwordE = URLEncoder.encode(password.trim(), "UTF-8");
            String usernameE = URLEncoder.encode(username.trim(), "UTF-8");
            Request request = new Request.Builder()
                    .url(Endpoints.AUTH)
                    .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), "password=" + passwordE + "&username=" + usernameE))
                    .build();
            http.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    apiListener.authenticateCallback("error-general");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String body = response.body().string();
                    if (body.contains(applicationContext.getString(R.string.errorPass))) {
                        apiListener.authenticateCallback(AuthMessages.INVALID_PASS);
                        return;
                    } else if (body.contains(AuthMessages.INVALID_USER)) {
                        apiListener.authenticateCallback(AuthMessages.INVALID_USER);
                        return;
                    }
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
                    SharedPreferences.Editor editor = sharedPreferences.edit()
                            .putString("userToken", body.substring(25, body.length() - 2));
                    editor.apply();
                    apiListener.authenticateCallback(body.substring(25, body.length() - 2));
                }
            });
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
    }

    public void request(int songID, Context applicationContext) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        String token = sharedPreferences.getString("userToken", "NULL");
        if (token.equals("NULL")) return;
        Request request = new Request.Builder()
                .url(Endpoints.REQUEST)
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), "song=" + String.valueOf(songID)))
                .addHeader("authorization", token)
                .build();
        http.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                apiListener.requestCallback(response.body().string());
            }
        });
    }

    public void favorite(int songID, Context applicationContext) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        String token = sharedPreferences.getString("userToken", "NULL");
        if (token.equals("NULL")) {
            apiListener.favoriteCallback("error-login");
            return;
        }
        Request request = new Request.Builder()
                .url(Endpoints.FAVORITE)
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), "song=" + String.valueOf(songID)))
                .addHeader("authorization", token)
                .build();
        http.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                apiListener.favoriteCallback("error_general");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                apiListener.favoriteCallback(response.body().string());
            }
        });
    }

    public void search(String query, Context applicationContext) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        String token = sharedPreferences.getString("userToken", "NULL");
        if (token.equals("NULL")) return;
        Request request = new Request.Builder()
                .url(Endpoints.SEARCH)
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), "query=" + query))
                .addHeader("authorization", token)
                .build();
        http.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                apiListener.searchCallback(response.body().string());
            }
        });
    }
}
