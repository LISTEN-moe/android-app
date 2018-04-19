package me.echeung.moemoekyun.utils;

import android.content.Context;

public final class PluralsUtil {

    public static String getString(Context context, int pluralId, int value) {
        String text = context.getResources().getQuantityString(pluralId, value);
        return String.format(text, value);
    }

}
