package jcotter.listenmoe;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

class APIActions {
    interface APIListener{
        void favoriteListCallback(String jsonResult);
        void authenticateCallback(String token);
        void requestCallback(String jsonResult);
        void favoriteCallback(String jsonResult);
        void searchCallback(String jsonResult);
    }

    private APIListener apiListener;

    APIActions(APIListener apiListener){
        this.apiListener = apiListener;
    }

    void favoriteList(Context applicationContext){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        final String userToken = sharedPreferences.getString("userToken", "NULL");
        if(userToken.equals("NULL")) {
            apiListener.favoriteCallback("NULL");
            return;
        }
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(applicationContext.getString(R.string.apiUserFavorites))
                .get()
                .addHeader("authorization", userToken)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {e.printStackTrace();}
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                apiListener.favoriteListCallback(response.body().string());
            }
    });
    }
    void authenticate(final Context applicationContext, final String username, final String password){
        OkHttpClient okHttpClient = new OkHttpClient();
        try {
            String passwordE = URLEncoder.encode(password.trim(), "UTF-8");
            String usernameE = URLEncoder.encode(username.trim(), "UTF-8");
            Request request = new Request.Builder()
                    .url(applicationContext.getString(R.string.apiAuthenticate))
                    .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), "password=" + passwordE + "&username=" + usernameE))
                    .build();
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    apiListener.authenticateCallback("error-general");
                }

                @SuppressLint("CommitPrefEdits")
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String body = response.body().string();
                    if(body.contains(applicationContext.getString(R.string.errorPass))) {
                        apiListener.authenticateCallback(applicationContext.getString(R.string.invalid_pass));
                        return;
                    } else if(body.contains(applicationContext.getString(R.string.invalid_user))){
                        apiListener.authenticateCallback(applicationContext.getString(R.string.invalid_user));
                        return;
                    }
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
                    SharedPreferences.Editor editor = sharedPreferences.edit()
                            .putString("userToken", body.substring(25, body.length() - 2));
                    editor.commit();
                    apiListener.authenticateCallback(body.substring(25, body.length() - 2));
                }
            });
        }catch (UnsupportedEncodingException ex) {ex.printStackTrace();}
    }
    void request(int songID, Context applicationContext){
        OkHttpClient okHttpClient = new OkHttpClient();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        String token = sharedPreferences.getString("userToken", "NULL");
        if (token.equals("NULL")) return;
        Request request = new Request.Builder()
                .url(applicationContext.getString(R.string.apiRequest))
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), "song=" + String.valueOf(songID)))
                .addHeader("authorization", token)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {e.printStackTrace();}
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                apiListener.requestCallback(response.body().string());
            }
        });
    }
    void favorite(int songID, Context applicationContext){
        OkHttpClient okHttpClient = new OkHttpClient();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        String token = sharedPreferences.getString("userToken", "NULL");
        if (token.equals("NULL")){
            apiListener.favoriteCallback("error-login");
            return;
        }
        Request request = new Request.Builder()
                .url(applicationContext.getString(R.string.apiFavorite))
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), "song=" + String.valueOf(songID)))
                .addHeader("authorization", token)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {e.printStackTrace(); apiListener.favoriteCallback("error_general");}
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                apiListener.favoriteCallback(response.body().string());
            }
        });
    }
    void search(String query, Context applicationContext){
        OkHttpClient okHttpClient = new OkHttpClient();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        String token = sharedPreferences.getString("userToken", "NULL");
        if(token.equals("NULL")) return;
        Request request = new Request.Builder()
                .url(applicationContext.getString(R.string.apiSearch))
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), "query=" + query))
                .addHeader("authorization", token)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {e.printStackTrace();}
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                apiListener.searchCallback(response.body().string());
            }
        });
    }
}
