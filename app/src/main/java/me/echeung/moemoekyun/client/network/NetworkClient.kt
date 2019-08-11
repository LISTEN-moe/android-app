package me.echeung.moemoekyun.client.network

import me.echeung.moemoekyun.util.system.NetworkUtil
import okhttp3.Interceptor
import okhttp3.OkHttpClient

object NetworkClient {

    private const val HEADER_CONTENT_TYPE = "Content-Type"
    private const val CONTENT_TYPE = "application/json"

    private const val HEADER_USER_AGENT = "User-Agent"

    val client: OkHttpClient = OkHttpClient.Builder()
            .addNetworkInterceptor(object : Interceptor {
                override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
                    val request = chain.request()

                    val newRequest = request.newBuilder()
                            .addHeader(HEADER_USER_AGENT, NetworkUtil.userAgent)
                            .addHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE)
                            .build()

                    return chain.proceed(newRequest)
                }
            })
            .build()
}
