package me.echeung.listenmoeapi.auth;

/**
 * Helper for handling authorization-related tasks. Helps with the storage of the auth token and
 * actions requiring it.
 */
public interface AuthUtil {

    /**
     * Checks if the user has previously logged in (i.e. a token is stored).
     *
     * @return Whether the user is authenticated.
     */
    boolean isAuthenticated();

    /**
     * Checks how old the stored auth token is. If it's older than 28 days, it becomes invalidated.
     *
     * @return Whether the token is still valid.
     */
    boolean checkAuthTokenValidity();

    /**
     * Fetches the stored auth token.
     *
     * @return The user's auth token.
     */
    String getAuthToken();

    /**
     * Fetches the stored auth token with the "Bearer" prefix.
     *
     * @return The user's auth token with the "Bearer" prefix.
     */
    String getAuthTokenWithPrefix();

    /**
     * Stores the auth token, also tracking the time that it was stored.
     * Android context to fetch SharedPreferences.
     *
     * @param token   The auth token to store, provided via the LISTEN.moe API.
     */
    void setAuthToken(String token);

    /**
     * Removes the stored auth token.
     */
    void clearAuthToken();
}
