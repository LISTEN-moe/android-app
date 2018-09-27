package me.echeung.moemoekyun.ui.activity

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import de.psdev.licensesdialog.LicensesDialog
import me.echeung.moemoekyun.BuildConfig
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.ui.base.BaseActivity
import me.echeung.moemoekyun.util.system.UrlUtil

class AboutActivity : BaseActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        setSupportActionBar(findViewById(R.id.appbar))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        setupInfo()
        setupClickListeners()
    }

    private fun setupInfo() {
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            var versionText = getString(R.string.version, packageInfo.versionName)
            if (BuildConfig.DEBUG) {
                versionText += String.format(" (%s)", packageInfo.packageName)
            }

            val txtVersion = findViewById<TextView>(R.id.app_version)
            txtVersion.text = versionText
        } catch (e: PackageManager.NameNotFoundException) {
            // Ignore
        }

        // Hide Play Store item depending on build flavor
        if (BuildConfig.FLAVOR != "playstore") {
            findViewById<View>(R.id.about_app_rate).visibility = View.GONE
        }

        // Kanna GIF
        Glide.with(this)
                .load(R.drawable.kanna_dancing)
                .into(findViewById<View>(R.id.kanna_image) as ImageView)
    }

    private fun setupClickListeners() {
        findViewById<View>(R.id.about_app_rate).setOnClickListener(this)
        findViewById<View>(R.id.about_app_github).setOnClickListener(this)
        findViewById<View>(R.id.about_app_translate).setOnClickListener(this)
        findViewById<View>(R.id.about_app_licenses).setOnClickListener(this)

        findViewById<View>(R.id.about_listenmoe_website).setOnClickListener(this)
        findViewById<View>(R.id.about_listenmoe_discord).setOnClickListener(this)
        findViewById<View>(R.id.about_listenmoe_patreon).setOnClickListener(this)

        findViewById<View>(R.id.about_privacy_policy).setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.about_app_rate -> UrlUtil.open(this, getString(R.string.url_store))

            R.id.about_app_github -> UrlUtil.open(this, getString(R.string.url_github))

            R.id.about_app_translate -> UrlUtil.open(this, getString(R.string.url_translate))

            R.id.about_app_licenses -> showLicensesDialog()

            R.id.about_listenmoe_website -> UrlUtil.open(this, getString(R.string.url_listenmoe))

            R.id.about_listenmoe_discord -> UrlUtil.open(this, getString(R.string.url_discord))

            R.id.about_listenmoe_patreon -> UrlUtil.open(this, getString(R.string.url_patreon))

            R.id.about_privacy_policy -> UrlUtil.open(this, getString(R.string.url_privacy_policy))
        }
    }

    private fun showLicensesDialog() {
        LicensesDialog.Builder(this)
                .setTitle(R.string.licenses)
                .setNotices(R.raw.notices)
                .setIncludeOwnLicense(true)
                .setThemeResourceId(R.style.DialogTheme)
                .setNoticesCssStyle(R.string.licenses_css)
                .build()
                .show()
    }

}
