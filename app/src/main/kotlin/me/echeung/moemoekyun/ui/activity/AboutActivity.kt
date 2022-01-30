package me.echeung.moemoekyun.ui.activity

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.mikepenz.aboutlibraries.LibsBuilder
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

        // Kanna GIF
        Glide.with(this)
            .load(R.drawable.kanna_dancing)
            .into(binding.aboutCardTranslations.kannaImage)
    }

    private fun setupClickListeners() {
        binding.aboutCardApp.let {
            it.aboutAppRate.setOnClickListener { openUrl(getString(R.string.url_store)) }
            it.aboutAppGithub.setOnClickListener { openUrl(getString(R.string.url_github)) }
            it.aboutAppTranslate.setOnClickListener { openUrl(getString(R.string.url_translate)) }
            it.aboutAppLicenses.setOnClickListener { LibsBuilder().start(this) }
        }

        binding.aboutCardListenmoe.let {
            it.aboutListenmoeWebsite.setOnClickListener { openUrl(getString(R.string.url_listenmoe)) }
            it.aboutListenmoeDiscord.setOnClickListener { openUrl(getString(R.string.url_discord)) }
            it.aboutListenmoePatreon.setOnClickListener { openUrl(getString(R.string.url_patreon)) }
        }

        binding.aboutPrivacyPolicy.setOnClickListener { openUrl(getString(R.string.url_privacy_policy)) }
    }
}
