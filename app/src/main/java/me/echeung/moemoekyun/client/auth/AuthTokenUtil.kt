package me.echeung.moemoekyun.client.auth

import android.content.Context
import androidx.preference.PreferenceManager
import me.echeung.moemoekyun.client.api.BridgeAPIClient

import java.lang.ref.WeakReference
import kotlin.math.roundToInt

/**
 * Helper for handling authorization-related tasks. Helps with the storage of the auth token and
 * actions requiring it.
 */
class AuthTokenUtil(context: Context) {

    private val contextRef: WeakReference<Context> = WeakReference(context)

    /**
     * Checks if the user has previously logged in (i.e. a token is stored).
     *
     * @return Whether the user is authenticated.
     */
    val isAuthenticated: Boolean
        get() = authToken != null

    /**
     * Fetches the stored auth token with the "Bearer" prefix.
     *
     * @return The user's auth token with the "Bearer" prefix.
     */
    val authTokenWithPrefix: String
        get() = getPrefixedToken(authToken)

    /**
     * Stores the auth token, also tracking the time that it was stored.
     * Android context to fetch SharedPreferences.
     *
     * @param token The auth token to store, provided via the LISTEN.moe API.
     */
    var authToken: String?
        get() {
            val context = contextRef.get() ?: return null

            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getString(USER_TOKEN, null)
        }
        set(token) {
            val context = contextRef.get() ?: return

            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putString(USER_TOKEN, token)
                    .putLong(LAST_AUTH, System.currentTimeMillis() / 1000)
                    .apply()

            // TODO: temporarily tracking whether we've logged in using the new API or not to force
            // users authenticated with the v4 API to log out
            if (BridgeAPIClient.useApi5) {
                PreferenceManager.getDefaultSharedPreferences(context)
                        .edit()
                        .putBoolean(IS_V5_AUTHED, true)
                        .apply()
            }
        }

    /**
     * Stores the temporary auth token for MFA.
     *
     * @param token The auth token for MFA to store, provided via the LISTEN.moe API.
     */
    var mfaToken: String? = null

    /**
     * Fetches the stored temporary MFA auth token with the "Bearer" prefix.
     *
     * @return The temporary MFA auth token with the "Bearer" prefix.
     */
    val mfaAuthTokenWithPrefix: String
        get() = getPrefixedToken(mfaToken)

    /**
     * @return The time in seconds since the stored auth token was stored.
     */
    private val tokenAge: Long
        get() {
            val context = contextRef.get() ?: return 0L

            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getLong(LAST_AUTH, 0L)
        }

    /**
     * Checks how old the stored auth token is. If it's older than 28 days, it becomes invalidated.
     *
     * @return Whether the token is still valid.
     */
    fun checkAuthTokenValidity(): Boolean {
        if (!isAuthenticated) {
            return false
        }

        // Check token is valid (max 28 days)
        val lastAuth = tokenAge
        if (((System.currentTimeMillis() / 1000 - lastAuth) / 86400.0).roundToInt() >= 28) {
            clearAuthToken()
            return false
        }

        // TODO: force log out v4 users if switched to v5 API
        if (BridgeAPIClient.useApi5) {
            val context = contextRef.get()
            if (context == null || !PreferenceManager.getDefaultSharedPreferences(context).getBoolean(IS_V5_AUTHED, false)) {
                clearAuthToken()
                return false
            }
        }

        return true
    }

    /**
     * Removes the stored auth token.
     */
    fun clearAuthToken() {
        val context = contextRef.get() ?: return

        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(USER_TOKEN, null)
                .putLong(LAST_AUTH, 0)
                .apply()
    }

    /**
     * Removes the stored temporary MFA auth token.
     */
    fun clearMfaAuthToken() {
        this.mfaToken = null
    }

    private fun getPrefixedToken(token: String?): String {
        return "Bearer $token"
    }

    companion object {
        private const val USER_TOKEN = "user_token"
        private const val LAST_AUTH = "last_auth"

        private const val IS_V5_AUTHED = "v5_authed"
    }
}
