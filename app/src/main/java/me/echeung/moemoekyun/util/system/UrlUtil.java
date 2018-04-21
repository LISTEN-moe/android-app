package me.echeung.moemoekyun.util.system;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public final class UrlUtil {

    public static void openUrl(Context context, String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(browserIntent);
    }

}
