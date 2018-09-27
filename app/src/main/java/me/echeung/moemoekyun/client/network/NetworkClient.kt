package me.echeung.moemoekyun.client.network

import me.echeung.moemoekyun.util.system.NetworkUtil
import okhttp3.OkHttpClient

object NetworkClient {

    private const val HEADER_CONTENT_TYPE = "Content-Type"
    private const val CONTENT_TYPE = "application/json"

    private const val HEADER_ACCEPT = "Accept"
    private const val ACCEPT = "application/vnd.listen.v4+json"

    private const val HEADER_USER_AGENT = "User-Agent"

    @JvmStatic
    val client: OkHttpClient = OkHttpClient.Builder()
                .addNetworkInterceptor { chain ->
                    val request = chain.request()

                    val newRequest = request.newBuilder()
                            .addHeader(HEADER_USER_AGENT, NetworkUtil.userAgent)
                            .addHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE)
                            .addHeader(HEADER_ACCEPT, ACCEPT)
                            .build()

                    chain.proceed(newRequest)
                }
                .build()

}
