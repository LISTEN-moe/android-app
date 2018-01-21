package me.echeung.listenmoeapi.auth;

import android.content.Context;
import android.preference.PreferenceManager;

public class AndroidAuthUtil implements AuthUtil {

    private static final String USER_TOKEN = "user_token";
    private static final String LAST_AUTH = "last_auth";

    private Context context;

    public AndroidAuthUtil(Context context) {
        this.context = context;
    }

    public boolean isAuthenticated() {
        return getAuthToken() != null;
    }

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

    public String getAuthToken() {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(USER_TOKEN, null);
    }

    public String getAuthTokenWithPrefix() {
        return String.format("Bearer %s", getAuthToken());
    }

    public void setAuthToken(String token) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(USER_TOKEN, token)
                .putLong(LAST_AUTH, System.currentTimeMillis() / 1000)
                .apply();
    }

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
