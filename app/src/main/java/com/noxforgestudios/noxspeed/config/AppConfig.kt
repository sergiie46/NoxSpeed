package com.noxforgestudios.noxspeed.config

import com.noxforgestudios.noxspeed.BuildConfig

object AppConfig {
    const val APP_NAME = "NoxSpeed"
    const val PACKAGE_NAME = "com.noxforgestudios.noxspeed"

    val bannerAdUnitId: String get() = BuildConfig.ADMOB_BANNER_ID
    val interstitialAdUnitId: String get() = BuildConfig.ADMOB_INTERSTITIAL_ID
    val rewardedAdUnitId: String get() = BuildConfig.ADMOB_REWARDED_ID
    val premiumProductId: String get() = BuildConfig.PREMIUM_PRODUCT_ID
    val supportEmail: String get() = BuildConfig.SUPPORT_EMAIL
    val privacyUrl: String get() = BuildConfig.PRIVACY_URL
    val termsUrl: String get() = BuildConfig.TERMS_URL
    val playStoreUrl: String get() = BuildConfig.PLAY_STORE_URL

    const val INTERSTITIAL_COOLDOWN_MS = 8 * 60 * 1000L
}
