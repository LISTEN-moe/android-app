package me.echeung.moemoekyun.client.network

import android.content.Context
import me.echeung.moemoekyun.client.auth.AuthUtil
import me.echeung.moemoekyun.util.system.NetworkUtil
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

class NetworkClient(
    context: Context,
    authUtil: AuthUtil,
) {

    val client = OkHttpClient.Builder()
        .addNetworkInterceptor { chain ->
            val request = chain.request()

            val newRequest = request.newBuilder()
                .header("User-Agent", NetworkUtil.userAgent)
                .header("Content-Type", "application/json")

            // MFA login
            if (authUtil.mfaToken != null) {
                newRequest.header(HEADER_AUTHZ, authUtil.mfaAuthTokenWithPrefix)
            }

            // Authorized calls
            if (authUtil.isAuthenticated) {
                newRequest.header(HEADER_AUTHZ, authUtil.authTokenWithPrefix)
            }

            chain.proceed(newRequest.build())
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val apolloCache = File(context.externalCacheDir, "apolloCache")
}

private const val HEADER_AUTHZ = "Authorization"
