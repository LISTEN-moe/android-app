package me.echeung.moemoekyun.ui.activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import de.psdev.licensesdialog.LicensesDialog;
import me.echeung.moemoekyun.BuildConfig;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.ui.base.BaseActivity;
import me.echeung.moemoekyun.util.system.UrlUtil;

public class AboutActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        setSupportActionBar(findViewById(R.id.appbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupInfo();
        setupClickListeners();
    }

    private void setupInfo() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionText = getString(R.string.version, packageInfo.versionName);
            if (BuildConfig.DEBUG) {
                versionText += String.format(" (%s)", packageInfo.packageName);
            }

            TextView txtVersion = findViewById(R.id.app_version);
            txtVersion.setText(versionText);
        } catch (PackageManager.NameNotFoundException e) {
        }

        // Hide Play Store item depending on build flavor
        if (!BuildConfig.FLAVOR.equals("playstore")) {
            findViewById(R.id.about_app_rate).setVisibility(View.GONE);
        }

        // Kanna GIF
        Glide.with(this)
                .load(R.drawable.kanna_dancing)
                .into((ImageView) findViewById(R.id.kanna_image));
    }

    private void setupClickListeners() {
        findViewById(R.id.about_app_rate).setOnClickListener(this);
        findViewById(R.id.about_app_github).setOnClickListener(this);
        findViewById(R.id.about_app_translate).setOnClickListener(this);
        findViewById(R.id.about_app_licenses).setOnClickListener(this);

        findViewById(R.id.about_listenmoe_website).setOnClickListener(this);
        findViewById(R.id.about_listenmoe_play_history).setOnClickListener(this);
        findViewById(R.id.about_listenmoe_discord).setOnClickListener(this);
        findViewById(R.id.about_listenmoe_patreon).setOnClickListener(this);

        findViewById(R.id.about_privacy_policy).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.about_app_rate:
                UrlUtil.open(this, getString(R.string.url_store));
                break;

            case R.id.about_app_github:
                UrlUtil.open(this, getString(R.string.url_github));
                break;

            case R.id.about_app_translate:
                UrlUtil.open(this, getString(R.string.url_translate));
                break;

            case R.id.about_app_licenses:
                showLicensesDialog();
                break;

            case R.id.about_listenmoe_website:
                UrlUtil.open(this, getString(R.string.url_listenmoe));
                break;

            case R.id.about_listenmoe_play_history:
                UrlUtil.open(this, getString(R.string.url_twitter_np));
                break;

            case R.id.about_listenmoe_discord:
                UrlUtil.open(this, getString(R.string.url_discord));
                break;

            case R.id.about_listenmoe_patreon:
                UrlUtil.open(this, getString(R.string.url_patreon));
                break;

            case R.id.about_privacy_policy:
                UrlUtil.open(this, getString(R.string.url_privacy_policy));
                break;
        }
    }

    private void showLicensesDialog() {
        new LicensesDialog.Builder(this)
                .setTitle(R.string.licenses)
                .setNotices(R.raw.notices)
                .setIncludeOwnLicense(true)
                .setThemeResourceId(R.style.DialogTheme)
                .setNoticesCssStyle(R.string.licenses_css)
                .build()
                .show();
    }

}
