package me.echeung.moemoekyun.utils;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.util.TypedValue;

import me.echeung.moemoekyun.R;

public final class ThemeUtil {

    @ColorInt
    public static int getAccentColor(Context context) {
        final TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.themeColorAccent, typedValue, true);
        return typedValue.data;
    }

}
