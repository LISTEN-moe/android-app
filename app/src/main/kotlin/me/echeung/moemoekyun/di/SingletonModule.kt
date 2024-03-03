package me.echeung.moemoekyun.di

import android.content.Context
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.http.HttpFetchPolicy
import com.apollographql.apollo3.cache.http.httpCache
import com.apollographql.apollo3.cache.http.httpFetchPolicy
import com.apollographql.apollo3.network.okHttpClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import me.echeung.moemoekyun.BuildConfig
import me.echeung.moemoekyun.client.auth.AuthUtil
import me.echeung.moemoekyun.util.system.NetworkUtil
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SingletonModule {

    @Provides
    @Singleton
    fun json() = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun okhttpClient(authUtil: AuthUtil): OkHttpClient {
        val builder = OkHttpClient.Builder()
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

        if (BuildConfig.DEBUG) {
            val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.HEADERS
            }
            builder.addNetworkInterceptor(httpLoggingInterceptor)
        }

        return builder.build()
    }

    @Provides
    @Singleton
    fun apolloClient(@ApplicationContext context: Context, okHttpClient: OkHttpClient) = ApolloClient.Builder()
        .serverUrl("https://listen.moe/graphql")
        .httpCache(
            directory = File(context.externalCacheDir, "apolloCache"),
            maxSize = 1024 * 1024,
        )
        .httpFetchPolicy(HttpFetchPolicy.NetworkFirst)
        .okHttpClient(okHttpClient)
        .build()
}

private const val HEADER_AUTHZ = "Authorization"
