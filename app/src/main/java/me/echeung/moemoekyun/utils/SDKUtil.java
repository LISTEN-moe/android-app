package me.echeung.moemoekyun.utils;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;

/**
 * Miscellaneous helpers to handle slightly different APIs between different Android SDK versions.
 */
public class SDKUtil {
    public static Spanned fromHtml(final String html) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT);
        } else {
            return Html.fromHtml(html);
        }
    }
}
