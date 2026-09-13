package com.noxforgestudios.noxspeed.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.noxforgestudios.noxspeed.config.AppConfig

@Composable
fun BannerAd(visible: Boolean, modifier: Modifier = Modifier) {
    if (!visible) return
    val context = LocalContext.current
    Box(modifier.fillMaxWidth().height(52.dp)) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = {
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = AppConfig.bannerAdUnitId
                    loadAd(AdRequest.Builder().build())
                }
            },
        )
    }
}
