package me.echeung.moemoekyun.util;

import android.content.Context;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class RequestUtil {

    private static final OkHttpClient http = new OkHttpClient();
    private static final String USER_AGENT = "me.echeung.moemoekyun";

    public static OkHttpClient getClient() {
        return http;
    }

    /**
     * Creates a {@link Request.Builder} for performing requests to the API.
     *
     * @param endpoint The API endpoint to hit.
     * @return A Request.Builder object configured with the provided endpoint.
     */
    public static Request.Builder builder(final String endpoint) {
        return new Request.Builder()
                .addHeader("User-Agent", USER_AGENT)
                .url(endpoint);
    }

    /**
     * Creates a {@link Request.Builder} for performing requests to the API with the auth token
     * added as a header value.
     *
     * @param context  Android context to fetch SharedPreferences.
     * @param endpoint The API endpoint to hit.
     * @return A Request.Builder object configured with the provided endpoint and the auth token.
     */
    public static Request.Builder authBuilder(final Context context, final String endpoint) {
        return builder(endpoint)
                .addHeader("authorization", AuthUtil.getAuthToken(context));
    }
}
