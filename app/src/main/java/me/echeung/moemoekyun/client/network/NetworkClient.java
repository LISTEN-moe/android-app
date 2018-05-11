package me.echeung.moemoekyun.client.network;

import me.echeung.moemoekyun.util.system.NetworkUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class NetworkClient {

    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE = "application/json";

    private static final String HEADER_ACCEPT = "Accept";
    private static final String ACCEPT = "application/vnd.listen.v4+json";

    private static final String HEADER_USER_AGENT = "User-Agent";

    public static OkHttpClient getClient() {
        return new OkHttpClient.Builder()
                .addNetworkInterceptor(chain -> {
                    final Request request = chain.request();

                    final Request newRequest = request.newBuilder()
                            .addHeader(HEADER_USER_AGENT, NetworkUtil.getUserAgent())
                            .addHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE)
                            .addHeader(HEADER_ACCEPT, ACCEPT)
                            .build();

                    return chain.proceed(newRequest);
                })
                .build();
    }

}
