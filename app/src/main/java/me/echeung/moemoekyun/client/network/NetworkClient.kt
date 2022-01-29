package me.echeung.moemoekyun.client.network

import android.content.Context
import me.echeung.moemoekyun.client.auth.AuthUtil
import me.echeung.moemoekyun.util.system.NetworkUtil
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class NetworkClient(
    context: Context,
    authUtil: AuthUtil
) {

    val client: OkHttpClient

    init {
        client = OkHttpClient.Builder()
            .addNetworkInterceptor(
                Interceptor { chain ->
                    val request = chain.request()

                    val newRequest = request.newBuilder()
                        .addHeader(HEADER_USER_AGENT, NetworkUtil.userAgent)
                        .addHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE)
                        .build()

                    chain.proceed(newRequest)
                }
            )
            .addNetworkInterceptor(
                Interceptor { chain ->
                    val original = chain.request()
                    val builder = original.newBuilder().method(original.method, original.body)

                    // MFA login
                    if (authUtil.mfaToken != null) {
                        builder.header(HEADER_AUTHZ, authUtil.mfaAuthTokenWithPrefix)
                    }

                    // Authorized calls
                    if (authUtil.isAuthenticated) {
                        builder.header(HEADER_AUTHZ, authUtil.authTokenWithPrefix)
                    }

                    chain.proceed(builder.build())
                }
            )
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}

private const val HEADER_CONTENT_TYPE = "Content-Type"
private const val CONTENT_TYPE = "application/json"

private const val HEADER_USER_AGENT = "User-Agent"

private const val HEADER_AUTHZ = "Authorization"
