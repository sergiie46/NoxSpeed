package com.noxforgestudios.noxspeed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.noxforgestudios.noxspeed.ui.AppViewModel
import com.noxforgestudios.noxspeed.ui.NoxSpeedRoot
import com.noxforgestudios.noxspeed.ui.theme.NoxSpeedTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as NoxSpeedApplication
        app.consent.gather(this) {
            lifecycleScope.launch {
                val premium = viewModel.premiumEffective.value
                if (app.consent.canRequestAds) app.ads.initializeAndPreload(premium)
            }
        }

        setContent {
            NoxSpeedTheme {
                NoxSpeedRoot(viewModel = viewModel)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.onForeground()
    }

    override fun onStop() {
        viewModel.onBackground()
        super.onStop()
    }
}
