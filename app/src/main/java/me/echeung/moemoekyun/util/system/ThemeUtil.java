package me.echeung.moemoekyun.util.system;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;

import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.util.PreferenceUtil;

public final class ThemeUtil {

    public static Context setTheme(Context context) {
        switch (App.getPreferenceUtil().getTheme()) {
            case PreferenceUtil.THEME_DEFAULT:
                context.setTheme(R.style.AppTheme);
                break;

            case PreferenceUtil.THEME_BLUE:
                context.setTheme(R.style.AppThemeBlue);
                break;

            case PreferenceUtil.THEME_LEGACY:
                context.setTheme(R.style.AppThemeLegacy);
                break;

            case PreferenceUtil.THEME_CHRISTMAS:
                context.setTheme(R.style.AppThemeChristmas);
                break;
        }

        return context;
    }

    public static void colorNavigationBar(Activity activity) {
        int color = App.getPreferenceUtil().shouldColorNavbar()
                ? ThemeUtil.getAccentColor(activity)
                : Color.BLACK;

        activity.getWindow().setNavigationBarColor(color);
    }

    @ColorInt
    public static int getAccentColor(Context context) {
        return resolveColorAttr(context, R.attr.themeColorAccent);
    }

    @ColorInt
    public static int getBackgroundColor(Context context) {
        return resolveColorAttr(context, android.R.attr.windowBackground);
    }

    @ColorInt
    public static int getBodyColor(Context context) {
        return resolveColorAttr(context, android.R.attr.textColorPrimary);
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
