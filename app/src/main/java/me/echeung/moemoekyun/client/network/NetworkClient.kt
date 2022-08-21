package me.echeung.moemoekyun.client.network

import android.content.Context
import me.echeung.moemoekyun.client.auth.AuthUtil
import me.echeung.moemoekyun.util.system.NetworkUtil
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.File
import java.util.concurrent.TimeUnit

class NetworkClient(
    context: Context,
    authUtil: AuthUtil
) {

    val client: OkHttpClient
    val apolloCache = File(context.externalCacheDir, "apolloCache")

    init {
        client = OkHttpClient.Builder()
            .addNetworkInterceptor(object : Interceptor {
                override fun intercept(chain: Interceptor.Chain): Response {
                    val request = chain.request()

                    val newRequest = request.newBuilder()
                        .addHeader(HEADER_USER_AGENT, NetworkUtil.userAgent)
                        .addHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE)
                        .build()

                    return chain.proceed(newRequest)
                }
            })
            .addNetworkInterceptor(object : Interceptor {
                override fun intercept(chain: Interceptor.Chain): Response {
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

                    return chain.proceed(builder.build())
                }
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    companion object {
        private const val HEADER_CONTENT_TYPE = "Content-Type"
        private const val CONTENT_TYPE = "application/json"

        private const val HEADER_USER_AGENT = "User-Agent"

        private const val HEADER_AUTHZ = "Authorization"
    }
}
