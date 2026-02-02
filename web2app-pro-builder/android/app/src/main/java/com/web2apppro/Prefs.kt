package com.web2apppro

import android.content.Context

object Prefs {
    private const val NAME = "web2apppro_prefs"

    private fun p(ctx: Context) = ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun isBannerEnabled(ctx: Context) = p(ctx).getBoolean("banner", true)
    fun isInterstitialEnabled(ctx: Context) = p(ctx).getBoolean("interstitial", true)
    fun isRewardedEnabled(ctx: Context) = p(ctx).getBoolean("rewarded", true)

    fun setBanner(ctx: Context, v: Boolean) = p(ctx).edit().putBoolean("banner", v).apply()
    fun setInterstitial(ctx: Context, v: Boolean) = p(ctx).edit().putBoolean("interstitial", v).apply()
    fun setRewarded(ctx: Context, v: Boolean) = p(ctx).edit().putBoolean("rewarded", v).apply()
}
