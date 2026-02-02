package com.web2apppro

import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val swBanner: Switch = findViewById(R.id.swBanner)
        val swInterstitial: Switch = findViewById(R.id.swInterstitial)
        val swRewarded: Switch = findViewById(R.id.swRewarded)
        val btnSave: Button = findViewById(R.id.btnSave)

        // Load saved values
        swBanner.isChecked = Prefs.isBannerEnabled(this)
        swInterstitial.isChecked = Prefs.isInterstitialEnabled(this)
        swRewarded.isChecked = Prefs.isRewardedEnabled(this)

        btnSave.setOnClickListener {
            Prefs.setBannerEnabled(this, swBanner.isChecked)
            Prefs.setInterstitialEnabled(this, swInterstitial.isChecked)
            Prefs.setRewardedEnabled(this, swRewarded.isChecked)

            Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
