package com.web2apppro

import android.content.Context

object Prefs {
    private const val FILE = "web2app_prefs"

    private const val KEY_BANNER = "ads_banner"
    private const val KEY_INTERSTITIAL = "ads_interstitial"
    private const val KEY_REWARDED = "ads_rewarded"

    fun isBannerEnabled(ctx: Context): Boolean =
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(KEY_BANNER, true)

    fun isInterstitialEnabled(ctx: Context): Boolean =
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(KEY_INTERSTITIAL, true)

    fun isRewardedEnabled(ctx: Context): Boolean =
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(KEY_REWARDED, true)

    fun setBannerEnabled(ctx: Context, enabled: Boolean) {
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_BANNER, enabled).apply()
    }

    fun setInterstitialEnabled(ctx: Context, enabled: Boolean) {
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_INTERSTITIAL, enabled).apply()
    }

    fun setRewardedEnabled(ctx: Context, enabled: Boolean) {
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_REWARDED, enabled).apply()
    }
}
