package jcotter.listenmoe.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import okhttp3.Request;

public class AuthUtil {
    private static final String USER_TOKEN = "user_token";
    private static final String LAST_AUTH = "last_auth";

    public static boolean isAuthenticated(final Context context) {
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        final String userToken = sharedPrefs.getString(USER_TOKEN, null);

        return userToken != null;
    }

    public static String getAuthToken(final Context context) {
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getString(USER_TOKEN, null);
    }

    public static void setAuthToken(final Context context, final String token) {
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPrefs.edit()
                .putString(USER_TOKEN, token)
                .putLong(LAST_AUTH, System.currentTimeMillis() / 1000);
        editor.apply();
    }

    public static void clearAuthToken(final Context context) {
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPrefs.edit()
                .putString(USER_TOKEN, null)
                .putLong(LAST_AUTH, 0);
        editor.apply();
    }

    public static long getTokenAge(final Context context) {
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getLong(LAST_AUTH, 0);
    }

    public static Request.Builder createAuthRequest(final Context context, final String endpoint) {
        return new Request.Builder()
                .url(endpoint)
                .addHeader("authorization", AuthUtil.getAuthToken(context));
    }
}
