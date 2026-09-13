package com.noxforgestudios.noxspeed.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.noxforgestudios.noxspeed.NoxSpeedApplication
import com.noxforgestudios.noxspeed.R
import com.noxforgestudios.noxspeed.data.db.TripEntity
import com.noxforgestudios.noxspeed.ui.screens.AccelerationScreen
import com.noxforgestudios.noxspeed.ui.screens.HudScreen
import com.noxforgestudios.noxspeed.ui.screens.OnboardingScreen
import com.noxforgestudios.noxspeed.ui.screens.PremiumScreen
import com.noxforgestudios.noxspeed.ui.screens.SettingsScreen
import com.noxforgestudios.noxspeed.ui.screens.SpeedScreen
import com.noxforgestudios.noxspeed.ui.screens.StatsScreen
import com.noxforgestudios.noxspeed.ui.screens.TripsScreen

private object Routes {
    const val SPEED = "speed"
    const val HUD = "hud"
    const val TRIPS = "trips"
    const val STATS = "stats"
    const val SETTINGS = "settings"
    const val PREMIUM = "premium"
    const val ACCEL = "accel"
}

@Composable
fun NoxSpeedRoot(viewModel: AppViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val gps by viewModel.gps.collectAsStateWithLifecycle()
    val activeTrip by viewModel.activeTrip.collectAsStateWithLifecycle()
    val trips by viewModel.trips.collectAsStateWithLifecycle()
    val accelerationResults by viewModel.accelerationResults.collectAsStateWithLifecycle()
    val billing by viewModel.billing.collectAsStateWithLifecycle()
    val premium by viewModel.premiumEffective.collectAsStateWithLifecycle()
    val acceleration by viewModel.acceleration.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? Activity
    val app = context.applicationContext as NoxSpeedApplication

    if (!settings.onboardingComplete) {
        OnboardingScreen(
            onFinished = { viewModel.completeOnboarding() },
            onPermissionResult = { viewModel.refreshPermission() },
        )
        return
    }

    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val current = backStack?.destination?.route ?: Routes.SPEED
    val mainRoutes = setOf(Routes.SPEED, Routes.TRIPS, Routes.STATS, Routes.SETTINGS)
    var completedTrip by remember { mutableStateOf<TripEntity?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { viewModel.refreshPermission() }

    LaunchedEffect(Unit) {
        viewModel.tripCompleted.collect { completedTrip = it }
    }
    LaunchedEffect(premium, app.consent.canRequestAds) {
        if (app.consent.canRequestAds && !premium) app.ads.initializeAndPreload(false)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (current in mainRoutes) {
                NavigationBar {
                    BottomItem(Routes.SPEED, current, stringResource(R.string.nav_speed)) { navController.mainNavigate(Routes.SPEED) }
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate(Routes.HUD) },
                        icon = { Text("▱") },
                        label = { Text(stringResource(R.string.nav_hud)) },
                    )
                    BottomItem(Routes.TRIPS, current, stringResource(R.string.nav_trips)) { navController.mainNavigate(Routes.TRIPS) }
                    BottomItem(Routes.STATS, current, stringResource(R.string.nav_stats)) { navController.mainNavigate(Routes.STATS) }
                    BottomItem(Routes.SETTINGS, current, stringResource(R.string.nav_settings)) { navController.mainNavigate(Routes.SETTINGS) }
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)) {
            NavHost(navController = navController, startDestination = Routes.SPEED) {
                composable(Routes.SPEED) {
                    SpeedScreen(
                        gps = gps,
                        settings = settings,
                        trip = activeTrip,
                        premium = premium,
                        onStartTrip = viewModel::startTrip,
                        onStopTrip = viewModel::stopTrip,
                        onPauseTrip = viewModel::pauseTrip,
                        onResumeTrip = viewModel::resumeTrip,
                        onAcceleration = { navController.navigate(Routes.ACCEL) },
                        onPremium = { navController.navigate(Routes.PREMIUM) },
                        onDemoSpeed = viewModel::injectDemoSpeed,
                    )
                    if (!gps.hasPermission) {
                        PermissionOverlay {
                            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                        }
                    }
                }
                composable(Routes.HUD) {
                    HudScreen(
                        gps = gps,
                        settings = settings,
                        trip = activeTrip,
                        premium = premium,
                        onMirrorChange = viewModel::setHudMirror,
                        onStyleChange = viewModel::setHudStyle,
                        onExit = { navController.popBackStack(); if (navController.currentDestination?.route == null) navController.navigate(Routes.SPEED) },
                    )
                }
                composable(Routes.TRIPS) {
                    TripsScreen(
                        trips = trips,
                        unit = settings.speedUnit,
                        premium = premium,
                        tripActive = activeTrip != null,
                        onDelete = viewModel::deleteTrip,
                        onFavorite = viewModel::toggleFavorite,
                        onShare = { shareTrip(context, viewModel.shareTripText(it, settings.speedUnit)) },
                    )
                }
                composable(Routes.STATS) {
                    StatsScreen(
                        trips = trips,
                        accelerationResults = accelerationResults,
                        unit = settings.speedUnit,
                        premium = premium,
                        tripActive = activeTrip != null,
                    )
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        viewModel = viewModel,
                        settings = settings,
                        premium = premium,
                        tripActive = activeTrip != null,
                        onPremium = { navController.navigate(Routes.PREMIUM) },
                    )
                }
                composable(Routes.PREMIUM) {
                    PremiumScreen(billing = billing, premium = premium, onBack = { navController.popBackStack() })
                }
                composable(Routes.ACCEL) {
                    AccelerationScreen(
                        gps = gps,
                        state = acceleration,
                        onArm = viewModel::armAcceleration,
                        onReset = viewModel::resetAcceleration,
                        onBack = { navController.popBackStack() },
                    )
                }
            }
        }
    }

    completedTrip?.let { trip ->
        AlertDialog(
            onDismissRequest = { completedTrip = null },
            title = { Text(stringResource(R.string.trip_complete)) },
            text = {
                Text(
                    "${com.noxforgestudios.noxspeed.gps.Units.distanceLabel(trip.distanceMeters, settings.speedUnit)} · " +
                        "${formatDuration(trip.durationMs)} · MAX ${com.noxforgestudios.noxspeed.gps.Units.roundedSpeed(trip.maxSpeedMps, settings.speedUnit)} ${com.noxforgestudios.noxspeed.gps.Units.unitLabel(settings.speedUnit)}"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    completedTrip = null
                    if (activity != null) {
                        app.ads.maybeShowTripCompleteInterstitial(activity, premium)
                        if (trips.size >= 3 && !settings.reviewRequested) {
                            requestInAppReview(activity) { viewModel.markReviewRequested() }
                        }
                    }
                }) { Text(stringResource(R.string.done)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    shareTrip(context, viewModel.shareTripText(trip, settings.speedUnit))
                    completedTrip = null
                }) { Text(stringResource(R.string.share)) }
            },
        )
    }
}

@Composable
private fun BottomItem(route: String, current: String, label: String, onClick: () -> Unit) {
    NavigationBarItem(
        selected = current == route,
        onClick = onClick,
        icon = { Text(when (route) {
            Routes.SPEED -> "◉"
            Routes.TRIPS -> "≋"
            Routes.STATS -> "▥"
            else -> "⚙"
        }) },
        label = { Text(label) },
    )
}

@Composable
private fun PermissionOverlay(onGrant: () -> Unit) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.BottomCenter,
    ) {
        androidx.compose.material3.Button(
            onClick = onGrant,
            modifier = Modifier
                .then(Modifier)
                .background(androidx.compose.ui.graphics.Color.Transparent),
        ) { Text(stringResource(R.string.grant_access)) }
    }
}

private fun androidx.navigation.NavHostController.mainNavigate(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun shareTrip(context: android.content.Context, text: String) {
    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }, null))
}
