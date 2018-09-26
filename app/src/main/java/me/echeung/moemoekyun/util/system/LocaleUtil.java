package me.echeung.moemoekyun.util.system;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;

import java.util.Locale;

import androidx.annotation.NonNull;
import me.echeung.moemoekyun.App;

// https://proandroiddev.com/change-language-programmatically-at-runtime-on-android-5e6bc15c758
public final class LocaleUtil {

    public static final String DEFAULT = "default";

    public static Context setLocale(@NonNull Context context) {
        if (App.Companion.getPreferenceUtil() == null) {
            return context;
        }

        String language = App.Companion.getPreferenceUtil().getLanguage();
        return setLocale(context, language);
    }

    public static Context setLocale(@NonNull Context context, String language) {
        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());

        if (!language.equals(DEFAULT)) {
            Locale locale;
            if (language.contains("-r")) {
                String[] languageParts = language.split("-r");
                locale = new Locale(languageParts[0], languageParts[1]);
            } else {
                locale = new Locale(language);
            }
            Locale.setDefault(locale);
            config.setLocale(locale);
        }

        context = context.createConfigurationContext(config);
        return context;
    }

    public static void setTitle(@NonNull Activity activity) {
        try {
            int label = activity
                    .getPackageManager()
                    .getActivityInfo(activity.getComponentName(), PackageManager.GET_META_DATA)
                    .labelRes;
            if (label != 0) {
                activity.setTitle(label);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(activity.getLocalClassName(), e.getMessage(), e);
        }
    }

}
