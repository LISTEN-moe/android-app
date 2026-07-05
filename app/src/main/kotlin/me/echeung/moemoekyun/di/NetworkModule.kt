package me.echeung.moemoekyun.di

import android.content.Context
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.http.DefaultHttpRequestComposer
import com.apollographql.apollo.network.http.DefaultHttpEngine
import com.apollographql.apollo.network.http.HttpNetworkTransport
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import me.echeung.moemoekyun.BuildConfig
import me.echeung.moemoekyun.client.api.ListenMoeApi
import me.echeung.moemoekyun.client.auth.AuthUtil
import me.echeung.moemoekyun.util.system.NetworkUtil
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

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
        .networkTransport(
            HttpNetworkTransport.Builder()
                .httpRequestComposer(
                    DefaultHttpRequestComposer(
                        serverUrl = "https://listen.moe/graphql",
                        enablePostCaching = true,
                    ),
                )
                .httpEngine(
                    DefaultHttpEngine {
                        okHttpClient.newBuilder()
                            .cache(Cache(File(context.externalCacheDir, "apolloCache"), maxSize = 1024 * 1024))
                            .build()
                    },
                )
                .build(),
        )
        .build()

    @Provides
    @Singleton
    fun listenMoeApi(okHttpClient: OkHttpClient, json: Json): ListenMoeApi = Retrofit.Builder()
        .baseUrl("https://listen.moe/")
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create()
}

private const val HEADER_AUTHZ = "Authorization"
