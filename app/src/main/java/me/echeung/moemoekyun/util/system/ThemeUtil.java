package me.echeung.moemoekyun.util.system;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.TypedValue;

import androidx.annotation.ColorInt;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;
import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.util.PreferenceUtil;

public final class ThemeUtil {

    public static Context setTheme(Context context) {
        context.setTheme(getThemeStyle());
        return context;
    }

    @StyleRes
    public static int getThemeStyle() {
        int style;

        switch (App.getPreferenceUtil().getTheme()) {
            case PreferenceUtil.THEME_CHRISTMAS:
                style = R.style.AppThemeChristmas;
                break;

            case PreferenceUtil.THEME_DEFAULT:
            default:
                style = R.style.AppTheme;
                break;
        }

        return style;
    }

    public static void colorNavigationBar(Activity activity) {
        int color = App.getPreferenceUtil().shouldColorNavbar()
                ? ThemeUtil.getAccentColor(activity)
                : Color.BLACK;

        activity.getWindow().setNavigationBarColor(color);
    }

    @ColorInt
    public static int getAccentColor(Context context) {
        return resolveColorAttr(setTheme(context), R.attr.themeColorAccent);
    }

    @ColorInt
    public static int getBackgroundColor(Context context) {
        return resolveColorAttr(setTheme(context), android.R.attr.windowBackground);
    }

    @ColorInt
    public static int getBodyColor(Context context) {
        return resolveColorAttr(setTheme(context), android.R.attr.textColorPrimary);
    }

    @ColorInt
    private static int resolveColorAttr(Context context, int attrId) {
        if (context != null) {
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = context.getTheme();

            boolean wasResolved = theme.resolveAttribute(attrId, typedValue, true);
            if (wasResolved) {
                return typedValue.resourceId == 0
                        ? typedValue.data
                        : ContextCompat.getColor(context, typedValue.resourceId);
            }
        }

        return Color.BLACK;
    }

}
