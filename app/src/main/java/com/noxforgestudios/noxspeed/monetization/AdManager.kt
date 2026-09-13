package com.noxforgestudios.noxspeed.monetization

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.noxforgestudios.noxspeed.config.AppConfig

class AdManager(private val context: Context) {
    private var initialized = false
    private var interstitial: InterstitialAd? = null
    private var rewarded: RewardedAd? = null
    private var lastInterstitialAt = 0L

    fun initializeAndPreload(premium: Boolean) {
        if (premium) return
        if (!initialized) {
            initialized = true
            MobileAds.initialize(context) {
                preloadInterstitial()
                preloadRewarded()
            }
        } else {
            preloadInterstitial()
            preloadRewarded()
        }
    }

    private fun preloadInterstitial() {
        if (interstitial != null) return
        InterstitialAd.load(
            context,
            AppConfig.interstitialAdUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) { interstitial = ad }
            },
        )
    }

    private fun preloadRewarded() {
        if (rewarded != null) return
        RewardedAd.load(
            context,
            AppConfig.rewardedAdUnitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) { rewarded = ad }
            },
        )
    }

    fun maybeShowTripCompleteInterstitial(activity: Activity, premium: Boolean) {
        if (premium) return
        val now = System.currentTimeMillis()
        if (now - lastInterstitialAt < AppConfig.INTERSTITIAL_COOLDOWN_MS) return
        val ad = interstitial ?: run { preloadInterstitial(); return }
        interstitial = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() { preloadInterstitial() }
        }
        lastInterstitialAt = now
        ad.show(activity)
    }

    fun showRewardedPreview(activity: Activity, premium: Boolean, onReward: () -> Unit) {
        if (premium) { onReward(); return }
        val ad = rewarded ?: run { preloadRewarded(); return }
        rewarded = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() { preloadRewarded() }
        }
        ad.show(activity) { onReward() }
    }
}
