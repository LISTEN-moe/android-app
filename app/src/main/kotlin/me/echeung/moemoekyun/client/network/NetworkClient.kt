package me.echeung.moemoekyun.client.network

import me.echeung.moemoekyun.client.auth.AuthUtil
import me.echeung.moemoekyun.util.system.NetworkUtil
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class NetworkClient(
    authUtil: AuthUtil
) {

    val client = OkHttpClient.Builder()
        .addNetworkInterceptor(
            Interceptor { chain ->
                val request = chain.request()

                val newRequest = request.newBuilder()
                    .addHeader("User-Agent", NetworkUtil.userAgent)
                    .addHeader("Content-Type", "application/json")

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
        )
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

}

private const val HEADER_AUTHZ = "Authorization"
