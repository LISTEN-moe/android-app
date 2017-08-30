package me.echeung.moemoekyun.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import me.echeung.moemoekyun.viewmodels.AppViewModel;

public class NetworkUtil {

    public static boolean isNetworkAvailable(Context context) {
        if (context != null) {
            final ConnectivityManager connectivityManager
                    = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

            boolean isAvailable = activeNetworkInfo != null && activeNetworkInfo.isConnected();

            AppViewModel.getInstance().setIsConnected(isAvailable);

            return isAvailable;
        }

        return false;
    }
}
