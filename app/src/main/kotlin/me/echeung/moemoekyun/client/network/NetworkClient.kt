package me.echeung.moemoekyun.client.network

import android.content.Context
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.http.HttpFetchPolicy
import com.apollographql.apollo3.cache.http.httpCache
import com.apollographql.apollo3.cache.http.httpFetchPolicy
import com.apollographql.apollo3.network.okHttpClient
import me.echeung.moemoekyun.client.api.Library
import me.echeung.moemoekyun.client.api.auth.AuthUtil
import me.echeung.moemoekyun.util.system.NetworkUtil
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

class NetworkClient(
    context: Context,
    authUtil: AuthUtil,
) {

    private val cacheFile = File(context.externalCacheDir, "apolloCache")
    private val cacheSize = 1024 * 1024.toLong()

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

    val apolloClient = ApolloClient.Builder()
        .serverUrl(Library.API_BASE)
        .httpCache(cacheFile, cacheSize)
        .httpFetchPolicy(HttpFetchPolicy.NetworkFirst)
        .okHttpClient(client)
        .build()
}

private const val HEADER_AUTHZ = "Authorization"
