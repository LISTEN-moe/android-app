package me.echeung.moemoekyun.ui.activities;

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
import me.echeung.moemoekyun.utils.UrlUtil;

public class AboutActivity extends BaseActivity implements View.OnClickListener {

    private static final String URL_STORE = "https://play.google.com/store/apps/details?id=me.echeung.moemoekyun";
    private static final String URL_GITHUB = "https://github.com/LISTEN-moe/android-app";
    private static final String URL_TRANSLATE = "https://crwd.in/listenmoe-android-app";

    private static final String URL_LISTENMOE = "https://listen.moe";
    private static final String URL_TWITTER_NP = "https://twitter.com/LISTEN_moe_NP";
    private static final String URL_DISCORD = "https://discordapp.com/invite/4S8JYr8"; // https://listen.moe/discord
    private static final String URL_PATREON = "https://www.patreon.com/odysseyradio";

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
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.about_app_rate:
                UrlUtil.openUrl(this, URL_STORE);
                break;

            case R.id.about_app_github:
                UrlUtil.openUrl(this, URL_GITHUB);
                break;

            case R.id.about_app_translate:
                UrlUtil.openUrl(this, URL_TRANSLATE);
                break;

            case R.id.about_app_licenses:
                showLicensesDialog();
                break;

            case R.id.about_listenmoe_website:
                UrlUtil.openUrl(this, URL_LISTENMOE);
                break;

            case R.id.about_listenmoe_play_history:
                UrlUtil.openUrl(this, URL_TWITTER_NP);
                break;

            case R.id.about_listenmoe_discord:
                UrlUtil.openUrl(this, URL_DISCORD);
                break;

            case R.id.about_listenmoe_patreon:
                UrlUtil.openUrl(this, URL_PATREON);
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
