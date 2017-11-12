package me.echeung.moemoekyun.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.annotation.NonNull;

import java.util.Locale;

import me.echeung.moemoekyun.App;

public class LocaleUtil {

    protected static final String DEFAULT = "default";

    public static Context setLocale(@NonNull Context context) {
        if (App.getPreferenceUtil() == null) {
            return context;
        }

        final String language = App.getPreferenceUtil().getLanguage();
        return setLocale(context, language);
    }

    public static Context setLocale(@NonNull Context context, String language) {
        final Resources res = context.getResources();
        final Configuration config = new Configuration(res.getConfiguration());

        if (!language.equals(DEFAULT)) {
            final Locale locale = new Locale(language);
            Locale.setDefault(locale);
            config.setLocale(locale);
        }

        context = context.createConfigurationContext(config);
        return context;
    }
}
