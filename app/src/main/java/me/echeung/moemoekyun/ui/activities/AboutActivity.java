package me.echeung.moemoekyun.ui.activities;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import de.psdev.licensesdialog.LicensesDialog;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.utils.UrlUtil;

public class AboutActivity extends BaseActivity implements View.OnClickListener {

    private static final String URL_GOOGLEPLAY = "https://play.google.com/store/apps/details?id=me.echeung.moemoekyun";
    private static final String URL_GITHUB = "https://github.com/LISTEN-moe/android-app";
    private static final String URL_TRANSLATE = "https://osfmofb.oneskyapp.com/collaboration/project?id=271507";

    private static final String URL_LISTENMOE = "https://listen.moe";
    private static final String URL_DISCORD = "https://discordapp.com/invite/4S8JYr8"; // https://listen.moe/discord
    private static final String URL_PATREON = "https://www.patreon.com/odysseyradio";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupVersion();
        setupClickListeners();
    }

    private void setupVersion() {
        try {
            final String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            final TextView txtVersion = findViewById(R.id.app_version);
            txtVersion.setText(getString(R.string.version, version));
        } catch (PackageManager.NameNotFoundException e) {
        }
    }

    private void setupClickListeners() {
        findViewById(R.id.about_app_rate).setOnClickListener(this);
        findViewById(R.id.about_app_github).setOnClickListener(this);
        findViewById(R.id.about_app_translate).setOnClickListener(this);
        findViewById(R.id.about_app_licenses).setOnClickListener(this);

        findViewById(R.id.about_listenmoe_website).setOnClickListener(this);
        findViewById(R.id.about_listenmoe_discord).setOnClickListener(this);
        findViewById(R.id.about_listenmoe_patreon).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.about_app_rate:
                UrlUtil.openUrl(this, URL_GOOGLEPLAY);
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
