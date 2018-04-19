package me.echeung.moemoekyun.utils;

import android.content.Context;
import android.preference.PreferenceManager;

import java.lang.ref.WeakReference;

/**
 * Helper for handling authorization-related tasks. Helps with the storage of the auth token and
 * actions requiring it.
 */
public class AuthUtil {

    private static final String USER_TOKEN = "user_token";
    private static final String LAST_AUTH = "last_auth";

    private String mfaToken;

    private WeakReference<Context> contextRef;

    public AuthUtil(Context context) {
        this.contextRef = new WeakReference<>(context);
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
     * Fetches the stored temporary MFA auth token with the "Bearer" prefix.
     *
     * @return The temporary MFA auth token with the "Bearer" prefix.
     */
    public String getMfaAuthTokenWithPrefix() {
        return getPrefixedToken(mfaToken);
    }

    /**
     * Stores the temporary auth token for MFA.
     *
     * @param token The auth token for MFA to store, provided via the LISTEN.moe API.
     */
    public void setMfaAuthToken(String token) {
        this.mfaToken = token;
    }

    /**
     * Removes the stored temporary MFA auth token.
     */
    public void clearMfaAuthToken() {
        this.mfaToken = null;
    }

    /**
     * Fetches the stored auth token with the "Bearer" prefix.
     *
     * @return The user's auth token with the "Bearer" prefix.
     */
    public String getAuthTokenWithPrefix() {
        return getPrefixedToken(getAuthToken());
    }

    /**
     * Stores the auth token, also tracking the time that it was stored.
     * Android context to fetch SharedPreferences.
     *
     * @param token The auth token to store, provided via the LISTEN.moe API.
     */
    public void setAuthToken(String token) {
        final Context context = contextRef.get();
        if (context == null) {
            return;
        }

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
        final Context context = contextRef.get();
        if (context == null) {
            return;
        }

        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(USER_TOKEN, null)
                .putLong(LAST_AUTH, 0)
                .apply();
    }

    private String getAuthToken() {
        final Context context = contextRef.get();
        if (context == null) {
            return null;
        }

        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(USER_TOKEN, null);
    }

    private String getPrefixedToken(String token) {
        return String.format("Bearer %s", token);
    }

    /**
     * @return The time in seconds since the stored auth token was stored.
     */
    private long getTokenAge() {
        final Context context = contextRef.get();
        if (context == null) {
            return 0L;
        }

        return PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(LAST_AUTH, 0L);
    }

}
