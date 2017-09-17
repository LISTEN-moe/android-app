package me.echeung.moemoekyun.ui.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import de.psdev.licensesdialog.LicensesDialog;
import me.echeung.moemoekyun.R;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String URL_GOOGLEPLAY = "https://play.google.com/store/apps/details?id=me.echeung.moemoekyun";
    private static final String URL_GITHUB = "https://github.com/arkon/listen-moe-android";
    private static final String URL_LISTENMOE = "https://listen.moe";
    private static final String URL_DISCORD = "https://discordapp.com/invite/4S8JYr8"; // https://listen.moe/discord
    private static final String URL_PATREON = "https://www.patreon.com/odysseyradio";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Set up app bar
        setSupportActionBar(findViewById(R.id.appbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupVersion();
        setupClickListeners();
    }

    private void setupVersion() {
        String version = "";
        try {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
        }

        final TextView txtVersion = findViewById(R.id.app_version);
        txtVersion.setText(getString(R.string.version, version));
    }

    private void setupClickListeners() {
        findViewById(R.id.about_app_rate).setOnClickListener(this);
        findViewById(R.id.about_app_github).setOnClickListener(this);
        findViewById(R.id.about_app_licenses).setOnClickListener(this);

        findViewById(R.id.about_listenmoe_website).setOnClickListener(this);
        findViewById(R.id.about_listenmoe_discord).setOnClickListener(this);
        findViewById(R.id.about_listenmoe_patreon).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.about_app_rate:
                openUrl(URL_GOOGLEPLAY);
                break;

            case R.id.about_app_github:
                openUrl(URL_GITHUB);
                break;

            case R.id.about_app_licenses:
                showLicensesDialog();
                break;

            case R.id.about_listenmoe_website:
                openUrl(URL_LISTENMOE);
                break;

            case R.id.about_listenmoe_discord:
                openUrl(URL_DISCORD);
                break;

            case R.id.about_listenmoe_patreon:
                openUrl(URL_PATREON);
                break;
        }
    }

    private void openUrl(String url) {
        final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    private void showLicensesDialog() {
        new LicensesDialog.Builder(this)
                .setNotices(R.raw.notices)
                .setIncludeOwnLicense(true)
                .setThemeResourceId(R.style.DialogTheme)
                .setNoticesCssStyle(R.string.licenses_css)
                .build()
                .show();
    }
}
