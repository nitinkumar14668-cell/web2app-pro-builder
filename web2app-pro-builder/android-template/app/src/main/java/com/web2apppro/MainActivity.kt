package com.web2apppro

import android.annotation.SuppressLint
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

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar

    private lateinit var adView: AdView
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    // ✅ User can select Ads ON/OFF
    private var adsEnabled = true

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        progressBar = findViewById(R.id.progressBar)

        val btnInterstitial: Button = findViewById(R.id.btnInterstitial)
        val btnRewarded: Button = findViewById(R.id.btnRewarded)

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

        // Load local website from assets
        webView.loadUrl("file:///android_asset/www/index.html")

        // --- AdMob Init ---
        MobileAds.initialize(this) {}

        // Banner
        adView = findViewById(R.id.adView)
        adView.setAdSize(AdSize.BANNER)
        adView.adUnitId = "ca-app-pub-3940256099942544/6300978111" // TEST Banner
        if (adsEnabled) {
            adView.loadAd(AdRequest.Builder().build())
        } else {
            adView.visibility = View.GONE
        }

        // Load ads
        loadInterstitial()
        loadRewarded()

        // Buttons (User clicks then show)
        btnInterstitial.setOnClickListener {
            if (!adsEnabled) {
                toast("Ads disabled")
                return@setOnClickListener
            }

            if (interstitialAd != null) {
                interstitialAd?.show(this)
            } else {
                toast("Interstitial not ready, loading...")
                loadInterstitial()
            }
        }

        btnRewarded.setOnClickListener {
            if (!adsEnabled) {
                toast("Ads disabled")
                return@setOnClickListener
            }

            val ad = rewardedAd
            if (ad != null) {
                ad.show(this) { rewardItem: RewardItem ->
                    toast("Reward: ${rewardItem.amount} ${rewardItem.type}")
                }
            } else {
                toast("Rewarded not ready, loading...")
                loadRewarded()
            }
        }

        // Honeygain SDK placeholder
        // initHoneygainSdk()
    }

    private fun loadInterstitial() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this,
            "ca-app-pub-3940256099942544/1033173712", // TEST Interstitial
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            interstitialAd = null
                            loadInterstitial()
                        }
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }

    private fun loadRewarded() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            this,
            "ca-app-pub-3940256099942544/5224354917", // TEST Rewarded
            adRequest,
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
        if (::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    // Honeygain placeholder (structure ready)
    /*
    private fun initHoneygainSdk() {
        // TODO: add Honeygain SDK init here later
    }
    */
}
