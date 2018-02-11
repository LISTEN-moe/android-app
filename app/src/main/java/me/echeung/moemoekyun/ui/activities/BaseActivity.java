package me.echeung.moemoekyun.ui.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.utils.LocaleUtil;
import me.echeung.moemoekyun.utils.PreferenceUtil;
import me.echeung.moemoekyun.utils.ThemeUtil;

public abstract class BaseActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        switch (App.getPreferenceUtil().getTheme()) {
            case PreferenceUtil.THEME_DEFAULT:
                setTheme(R.style.AppTheme);
                break;

            case PreferenceUtil.THEME_LEGACY:
                setTheme(R.style.AppThemeLegacy);
                break;

            case PreferenceUtil.THEME_CHRISTMAS:
                setTheme(R.style.AppThemeChristmas);
                break;
        }

        super.onCreate(savedInstanceState);

        LocaleUtil.setTitle(this);

        colorNavigationBar();

        App.getPreferenceUtil().registerListener(this);
    }

    @Override
    protected void onDestroy() {
        App.getPreferenceUtil().unregisterListener(this);

        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleUtil.setLocale(base));
    }

    private void colorNavigationBar() {
        final int color = App.getPreferenceUtil().shouldColorNavbar()
                ? ThemeUtil.getAccentColor(this)
                : Color.BLACK;

        getWindow().setNavigationBarColor(color);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case PreferenceUtil.PREF_COLOR_NAVBAR:
                colorNavigationBar();
                break;
        }
    }

}
