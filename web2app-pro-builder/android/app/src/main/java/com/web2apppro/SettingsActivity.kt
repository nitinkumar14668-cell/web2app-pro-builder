package com.web2apppro

import android.os.Bundle
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val chkBanner: CheckBox = findViewById(R.id.chkBanner)
        val chkInterstitial: CheckBox = findViewById(R.id.chkInterstitial)
        val chkRewarded: CheckBox = findViewById(R.id.chkRewarded)

        chkBanner.isChecked = Prefs.isBannerEnabled(this)
        chkInterstitial.isChecked = Prefs.isInterstitialEnabled(this)
        chkRewarded.isChecked = Prefs.isRewardedEnabled(this)

        chkBanner.setOnCheckedChangeListener { _, v -> Prefs.setBanner(this, v) }
        chkInterstitial.setOnCheckedChangeListener { _, v -> Prefs.setInterstitial(this, v) }
        chkRewarded.setOnCheckedChangeListener { _, v -> Prefs.setRewarded(this, v) }
    }
}
