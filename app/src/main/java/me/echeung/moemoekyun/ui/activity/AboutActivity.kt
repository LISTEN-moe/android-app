package me.echeung.moemoekyun.ui.activity

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import de.psdev.licensesdialog.LicensesDialog
import me.echeung.moemoekyun.BuildConfig
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.ui.base.BaseActivity
import me.echeung.moemoekyun.util.ext.openUrl

class AboutActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        initAppbar()

        setupInfo()
        setupClickListeners()
    }

    private fun setupInfo() {
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            var versionText = getString(R.string.version, packageInfo.versionName)
            if (BuildConfig.DEBUG) {
                versionText += " (${packageInfo.packageName})"
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
                .into(findViewById(R.id.kanna_image))
    }

    private fun setupClickListeners() {
        findViewById<View>(R.id.about_app_rate).setOnClickListener { openUrl(getString(R.string.url_store)) }
        findViewById<View>(R.id.about_app_github).setOnClickListener { openUrl(getString(R.string.url_github)) }
        findViewById<View>(R.id.about_app_translate).setOnClickListener { openUrl(getString(R.string.url_translate)) }
        findViewById<View>(R.id.about_app_licenses).setOnClickListener { showLicensesDialog() }

        findViewById<View>(R.id.about_listenmoe_website).setOnClickListener { openUrl(getString(R.string.url_listenmoe)) }
        findViewById<View>(R.id.about_listenmoe_discord).setOnClickListener { openUrl(getString(R.string.url_discord)) }
        findViewById<View>(R.id.about_listenmoe_patreon).setOnClickListener { openUrl(getString(R.string.url_patreon)) }

        findViewById<View>(R.id.about_privacy_policy).setOnClickListener { openUrl(getString(R.string.url_privacy_policy)) }
    }

    private fun showLicensesDialog() {
        LicensesDialog.Builder(this)
                .setTitle(R.string.licenses)
                .setNotices(R.raw.notices)
                .setIncludeOwnLicense(true)
                .setThemeResourceId(R.style.Theme_Widget_Dialog)
                .setNoticesCssStyle(R.string.licenses_css)
                .build()
                .show()
    }
}
