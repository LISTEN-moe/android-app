package me.echeung.moemoekyun.ui.base;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.util.PreferenceUtil;
import me.echeung.moemoekyun.util.system.LocaleUtil;
import me.echeung.moemoekyun.util.system.ThemeUtil;

public abstract class BaseActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtil.INSTANCE.setTheme(this);
        LocaleUtil.INSTANCE.setTitle(this);

        ThemeUtil.INSTANCE.colorNavigationBar(this);

        App.Companion.getPreferenceUtil().registerListener(this);

        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finishAfterTransition();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        App.Companion.getPreferenceUtil().unregisterListener(this);

        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleUtil.INSTANCE.setLocale(base));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case PreferenceUtil.PREF_COLOR_NAVBAR:
                ThemeUtil.INSTANCE.colorNavigationBar(this);
                break;
        }
    }

}
