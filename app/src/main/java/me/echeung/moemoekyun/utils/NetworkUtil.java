package me.echeung.moemoekyun.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import me.echeung.moemoekyun.App;

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

    private static NetworkInfo getActiveNetworkInfo(Context context) {
        final ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return connectivityManager.getActiveNetworkInfo();
    }

}
