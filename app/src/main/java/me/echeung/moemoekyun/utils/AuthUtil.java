package me.echeung.moemoekyun.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import me.echeung.moemoekyun.ui.App;

/**
 * Helper class for handling authorization-related tasks. Helps with the storage of the auth token
 * and actions requiring it.
 */
public class AuthUtil {

    private static final String USER_TOKEN = "user_token";
    private static final String LAST_AUTH = "last_auth";

    /**
     * Checks if the user has previously logged in (i.e. a token is stored).
     *
     * @param context Android context to fetch SharedPreferences.
     * @return Whether the user is authenticated.
     */
    public static boolean isAuthenticated(final Context context) {
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getString(USER_TOKEN, null) != null;
    }

    /**
     * Checks how old the stored auth token is. If it's older than 28 days, it becomes invalidated.
     *
     * @param context Android context to fetch SharedPreferences.
     */
    public static void checkAuthTokenValidity(final Context context) {
        if (!AuthUtil.isAuthenticated(context)) {
            return;
        }

        // Check token is valid (max 28 days)
        final long lastAuth = AuthUtil.getTokenAge(context);
        if (Math.round((System.currentTimeMillis() / 1000 - lastAuth) / 86400.0) >= 28) {
            AuthUtil.clearAuthToken(context);
        }
    }

    /**
     * Fetches the stored auth token.
     *
     * @param context Android context to fetch SharedPreferences.
     * @return The user's auth token.
     */
    public static String getAuthToken(final Context context) {
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getString(USER_TOKEN, null);
    }

    /**
     * Stores the auth token, also tracking the time that it was stored.
     * Android context to fetch SharedPreferences.
     *
     * @param context
     * @param token   The auth token to store, provided via the LISTEN.moe API.
     */
    public static void setAuthToken(final Context context, final String token) {
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPrefs.edit()
                .putString(USER_TOKEN, token)
                .putLong(LAST_AUTH, System.currentTimeMillis() / 1000)
                .apply();
    }

    /**
     * Removes the stored auth token.
     *
     * @param context Android context to fetch SharedPreferences.
     */
    public static void clearAuthToken(final Context context) {
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPrefs.edit()
                .putString(USER_TOKEN, null)
                .putLong(LAST_AUTH, 0)
                .apply();

        App.getUserViewModel().reset();
    }

    /**
     * Checks how old the token is.
     *
     * @param context Android context to fetch SharedPreferences.
     * @return The time in seconds since the stored auth token was stored.
     */
    public static long getTokenAge(final Context context) {
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getLong(LAST_AUTH, 0);
    }
}
