package me.echeung.moemoekyun.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class UrlUtil {

    public static void openUrl(Context context, String url) {
        final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(browserIntent);
    }
}
