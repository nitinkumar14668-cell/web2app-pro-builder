package com.web2apppro

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar

    private lateinit var adView: AdView
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    private var urlMode: String = "zip"
    private var websiteUrl: String = ""

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        progressBar = findViewById(R.id.progressBar)

        val btnSettings: Button = findViewById(R.id.btnSettings)
        val btnInterstitial: Button = findViewById(R.id.btnInterstitial)
        val btnRewarded: Button = findViewById(R.id.btnRewarded)

        // Load config
        loadConfig()

        // WebView setup
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true
        webView.webViewClient = WebViewClient()

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress = newProgress
                progressBar.visibility = if (newProgress < 100) View.VISIBLE else View.GONE
                if (newProgress == 100) swipeRefresh.isRefreshing = false
            }
        }

        swipeRefresh.setOnRefreshListener { webView.reload() }

        // Load website
        if (urlMode == "url" && websiteUrl.isNotBlank()) {
            webView.loadUrl(websiteUrl)
        } else {
            webView.loadUrl("file:///android_asset/www/index.html")
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // AdMob Init
        MobileAds.initialize(this) {}

        // Banner
        adView = findViewById(R.id.adView)

        // Load ads
        loadInterstitialIfEnabled()
        loadRewardedIfEnabled()

        btnInterstitial.setOnClickListener {
            if (!Prefs.isInterstitialEnabled(this)) {
                toast("Interstitial disabled")
                return@setOnClickListener
            }
            val ad = interstitialAd
            if (ad != null) ad.show(this) else {
                toast("Interstitial not ready")
                loadInterstitialIfEnabled()
            }
        }

        btnRewarded.setOnClickListener {
            if (!Prefs.isRewardedEnabled(this)) {
                toast("Rewarded disabled")
                return@setOnClickListener
            }
            val ad = rewardedAd
            if (ad != null) {
                ad.show(this) { rewardItem: RewardItem ->
                    toast("Reward: ${rewardItem.amount} ${rewardItem.type}")
                }
            } else {
                toast("Rewarded not ready")
                loadRewardedIfEnabled()
            }
        }

        // Honeygain Dummy init
        HoneygainDummy.init()
    }

    private fun loadConfig() {
        try {
            val jsonStr = assets.open("app_config.json").bufferedReader().use { it.readText() }
            val obj = JSONObject(jsonStr)
            urlMode = obj.optString("urlMode", "zip")
            websiteUrl = obj.optString("websiteUrl", "")
        } catch (_: Exception) {
            urlMode = "zip"
            websiteUrl = ""
        }
    }

    override fun onResume() {
        super.onResume()
        if (Prefs.isBannerEnabled(this)) {
            adView.visibility = View.VISIBLE
            adView.loadAd(AdRequest.Builder().build())
        } else {
            adView.visibility = View.GONE
        }
    }

    private fun loadInterstitialIfEnabled() {
        if (!Prefs.isInterstitialEnabled(this)) {
            interstitialAd = null
            return
        }
        InterstitialAd.load(
            this,
            "ca-app-pub-3546008790006961/2919837333",
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            interstitialAd = null
                            loadInterstitialIfEnabled()
                        }
                    }
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }

    private fun loadRewardedIfEnabled() {
        if (!Prefs.isRewardedEnabled(this)) {
            rewardedAd = null
            return
        }
        RewardedAd.load(
            this,
            "ca-app-pub-3546008790006961/5224354917",
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                }
            }
        )
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) webView.goBack()
        else super.onBackPressed()
    }
}

// Honeygain Dummy placeholder
object HoneygainDummy {
    fun init() {
        // Later: replace with real Honeygain SDK init
        // HoneygainSDK.init(...)
    }
}
