package me.echeung.moemoekyun.utils;

import android.graphics.Bitmap;

public class BitmapUtil {

    // Talk about janky. https://stackoverflow.com/a/2068981
    public static Bitmap blur(Bitmap src) {
        final Bitmap smaller = Bitmap.createScaledBitmap(src, src.getWidth() / 5, src.getHeight() / 5, true);

        return Bitmap.createScaledBitmap(smaller, src.getWidth(), src.getHeight(), true);
    }
}
