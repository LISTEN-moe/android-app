package me.echeung.moemoekyun.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import me.echeung.moemoekyun.state.AppState;

public class NetworkUtil {

    /**
     * Checks if there's an Internet connection and returns true iff there is.
     *
     * @return True iff there is an Internet connection available.
     */
    public static boolean isNetworkAvailable(Context context) {
        if (context != null) {
            final ConnectivityManager connectivityManager
                    = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

            boolean isAvailable = activeNetworkInfo != null && activeNetworkInfo.isConnected();

            AppState.getInstance().hasNetworkConnection.set(isAvailable);

            return isAvailable;
        }

        return false;
    }
}
