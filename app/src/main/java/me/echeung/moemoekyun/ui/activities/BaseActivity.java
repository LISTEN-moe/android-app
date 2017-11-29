package me.echeung.moemoekyun.ui.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.utils.LocaleUtil;
import me.echeung.moemoekyun.utils.PreferenceUtil;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!App.getPreferenceUtil().isDefaultTheme()) {
            switch (App.getPreferenceUtil().getTheme()) {
                case PreferenceUtil.THEME_CHRISTMAS:
//                    setTheme(android.R.style.Theme_Light);
                    break;

                case PreferenceUtil.THEME_FOUR:
//                    setTheme(android.R.style.Theme_Light);
                    break;
            }
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleUtil.setLocale(base));
    }
}
