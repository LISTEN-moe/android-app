package me.echeung.moemoekyun.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.BuildConfig;

public final class NetworkUtil {

    public static boolean isNetworkAvailable(Context context) {
        if (context != null) {
            final NetworkInfo activeNetworkInfo = getActiveNetworkInfo(context);

            boolean isAvailable = activeNetworkInfo != null && activeNetworkInfo.isConnected();

            App.getRadioViewModel().setIsConnected(isAvailable);

            return isAvailable;
        }

        return false;
    }

    public static boolean isWifi(Context context) {
        if (context == null || !isNetworkAvailable(context)) {
            return false;
        }

        final NetworkInfo activeNetworkInfo = getActiveNetworkInfo(context);

        return activeNetworkInfo != null
                && activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI
                && activeNetworkInfo.isConnectedOrConnecting();
    }

    public static String getUserAgent() {
        return String.format("%s/%s (%s; %s; Android %s)",
                BuildConfig.APPLICATION_ID,
                BuildConfig.VERSION_NAME,
                Build.DEVICE,
                Build.BRAND,
                Build.VERSION.SDK_INT);
    }

    private static NetworkInfo getActiveNetworkInfo(Context context) {
        final ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return connectivityManager.getActiveNetworkInfo();
    }

}
