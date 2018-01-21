package me.echeung.listenmoeapi.auth;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Helper for handling authorization-related tasks. Helps with the storage of the auth token and
 * actions requiring it.
 */
public class AuthUtil {

    private static final String USER_TOKEN = "user_token";
    private static final String LAST_AUTH = "last_auth";

    private Context context;

    public AuthUtil(Context context) {
        this.context = context;
    }

    /**
     * Checks if the user has previously logged in (i.e. a token is stored).
     *
     * @return Whether the user is authenticated.
     */
    public boolean isAuthenticated() {
        return getAuthToken() != null;
    }

    /**
     * Checks how old the stored auth token is. If it's older than 28 days, it becomes invalidated.
     *
     * @return Whether the token is still valid.
     */
    public boolean checkAuthTokenValidity() {
        if (!isAuthenticated()) {
            return false;
        }

        // Check token is valid (max 28 days)
        final long lastAuth = getTokenAge();
        if (Math.round((System.currentTimeMillis() / 1000 - lastAuth) / 86400.0) >= 28) {
            clearAuthToken();
            return false;
        }

        return true;
    }

    /**
     * Fetches the stored auth token.
     *
     * @return The user's auth token.
     */
    public String getAuthToken() {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(USER_TOKEN, null);
    }

    /**
     * Fetches the stored auth token with the "Bearer" prefix.
     *
     * @return The user's auth token with the "Bearer" prefix.
     */
    public String getAuthTokenWithPrefix() {
        return String.format("Bearer %s", getAuthToken());
    }

    /**
     * Stores the auth token, also tracking the time that it was stored.
     * Android context to fetch SharedPreferences.
     *
     * @param token The auth token to store, provided via the LISTEN.moe API.
     */
    public void setAuthToken(String token) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(USER_TOKEN, token)
                .putLong(LAST_AUTH, System.currentTimeMillis() / 1000)
                .apply();
    }

    /**
     * Removes the stored auth token.
     */
    public void clearAuthToken() {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(USER_TOKEN, null)
                .putLong(LAST_AUTH, 0)
                .apply();
    }

    /**
     * Checks how old the token is.
     *
     * @return The time in seconds since the stored auth token was stored.
     */
    private long getTokenAge() {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(LAST_AUTH, 0);
    }
}
