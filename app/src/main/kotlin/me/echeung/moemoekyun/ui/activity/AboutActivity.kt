package me.echeung.moemoekyun.ui.activity

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.view.isVisible
import coil.load
import de.psdev.licensesdialog.LicensesDialog
import me.echeung.moemoekyun.BuildConfig
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.databinding.ActivityAboutBinding
import me.echeung.moemoekyun.ui.base.BaseActivity
import me.echeung.moemoekyun.util.ext.openUrl

class AboutActivity : BaseActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            binding.aboutCardApp.appVersion.text = versionText
        } catch (e: PackageManager.NameNotFoundException) {
            // Ignore
        }

        // Hide Play Store item depending on build flavor
        if (BuildConfig.FLAVOR != "playstore") {
            binding.aboutCardApp.aboutAppRate.isVisible = false
        }

        binding.aboutCardTranslations.kannaImage.load(R.drawable.kanna_dancing)
    }

    private fun setupClickListeners() {
        binding.aboutCardApp.let {
            it.aboutAppRate.setOnClickListener { openUrl("https://play.google.com/store/apps/details?id=me.echeung.moemoekyun") }
            it.aboutAppGithub.setOnClickListener { openUrl("https://github.com/LISTEN-moe/android-app") }
            it.aboutAppTranslate.setOnClickListener { openUrl("https://crwd.in/listenmoe-android-app") }
            it.aboutAppLicenses.setOnClickListener { showLicensesDialog() }
        }

        binding.aboutCardListenmoe.let {
            it.aboutListenmoeWebsite.setOnClickListener { openUrl("https://listen.moe") }
            it.aboutListenmoeDiscord.setOnClickListener { openUrl("https://discordapp.com/invite/4S8JYr8") }
            it.aboutListenmoePatreon.setOnClickListener { openUrl("https://www.patreon.com/odysseyradio") }
        }

        binding.aboutPrivacyPolicy.setOnClickListener { openUrl("https://listen-moe.github.io/android-app/privacy.txt") }
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
