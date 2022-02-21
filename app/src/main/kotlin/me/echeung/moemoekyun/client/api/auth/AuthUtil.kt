package me.echeung.moemoekyun.client.api.auth

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import kotlin.math.roundToInt

/**
 * Helper for handling authorization-related tasks. Helps with the storage of the auth token and
 * actions requiring it.
 */
class AuthUtil(context: Context) {

    private val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)

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
        get() = sharedPrefs.getString(USER_TOKEN, null)
        set(token) {
            sharedPrefs.edit {
                putString(USER_TOKEN, token)
                putLong(LAST_AUTH, System.currentTimeMillis() / 1000)
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
        get() = sharedPrefs.getLong(LAST_AUTH, 0L)

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

        return true
    }

    /**
     * Removes the stored auth token.
     */
    fun clearAuthToken() {
        sharedPrefs.edit {
            putString(USER_TOKEN, null)
            putLong(LAST_AUTH, 0)
        }
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
}

private const val USER_TOKEN = "user_token"
private const val LAST_AUTH = "last_auth"
