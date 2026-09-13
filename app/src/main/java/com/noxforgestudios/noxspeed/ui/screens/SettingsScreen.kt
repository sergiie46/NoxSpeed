package com.noxforgestudios.noxspeed.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxforgestudios.noxspeed.BuildConfig
import com.noxforgestudios.noxspeed.NoxSpeedApplication
import com.noxforgestudios.noxspeed.R
import com.noxforgestudios.noxspeed.config.AppConfig
import com.noxforgestudios.noxspeed.data.prefs.DashboardStyle
import com.noxforgestudios.noxspeed.data.prefs.AccentPreset
import com.noxforgestudios.noxspeed.data.prefs.GpsFilterMode
import com.noxforgestudios.noxspeed.data.prefs.SpeedUnit
import com.noxforgestudios.noxspeed.data.prefs.UserSettings
import com.noxforgestudios.noxspeed.data.prefs.VehicleMode
import com.noxforgestudios.noxspeed.ui.AppViewModel
import com.noxforgestudios.noxspeed.ui.components.BannerAd
import com.noxforgestudios.noxspeed.ui.components.NoxCard
import com.noxforgestudios.noxspeed.ui.components.SectionLabel
import com.noxforgestudios.noxspeed.ui.theme.NoxCyan
import com.noxforgestudios.noxspeed.ui.theme.NoxMuted
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: AppViewModel,
    settings: UserSettings,
    premium: Boolean,
    tripActive: Boolean,
    onPremium: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val app = context.applicationContext as NoxSpeedApplication
    val scope = rememberCoroutineScope()
    var confirmErase by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(viewModel.exportJson()) }
            }.onSuccess { Toast.makeText(context, context.getString(R.string.data_exported), Toast.LENGTH_SHORT).show() }
                .onFailure { Toast.makeText(context, context.getString(R.string.data_error), Toast.LENGTH_SHORT).show() }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            runCatching {
                val raw = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    ?: error("Empty file")
                viewModel.importJson(raw)
            }.onSuccess { Toast.makeText(context, context.getString(R.string.data_imported), Toast.LENGTH_SHORT).show() }
                .onFailure { Toast.makeText(context, context.getString(R.string.data_error), Toast.LENGTH_SHORT).show() }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp),
    ) {
        Text(stringResource(R.string.settings_title), modifier = Modifier.padding(top = 22.dp, bottom = 6.dp), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)

        SectionLabel(stringResource(R.string.general))
        NoxCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(stringResource(R.string.units), fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SpeedUnit.entries.forEach { unit ->
                        FilterChip(selected = settings.speedUnit == unit, onClick = { viewModel.setUnit(unit) }, label = { Text(unit.name) })
                    }
                }
                Text(stringResource(R.string.vehicle_mode), fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    VehicleMode.entries.forEach { mode ->
                        FilterChip(
                            selected = settings.vehicleMode == mode,
                            onClick = { viewModel.setVehicleMode(mode) },
                            label = { Text(mode.name.take(5)) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Text(stringResource(R.string.dashboard), fontWeight = FontWeight.Bold)
                DashboardStyle.entries.chunked(4).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        row.forEach { style ->
                            FilterChip(
                                selected = settings.dashboard == style,
                                onClick = { viewModel.setDashboard(style) },
                                label = { Text(style.name.take(5)) },
                                enabled = premium || style.ordinal <= DashboardStyle.NIGHT.ordinal,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
                Text(stringResource(R.string.accent_color), fontWeight = FontWeight.Bold)
                AccentPreset.entries.chunked(4).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        row.forEach { preset ->
                            FilterChip(
                                selected = settings.accentPreset == preset,
                                onClick = { viewModel.setAccentPreset(preset) },
                                enabled = premium || preset.ordinal <= AccentPreset.ORANGE.ordinal,
                                label = { Text(preset.name.take(4)) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
                SettingSwitch(stringResource(R.string.keep_screen_on), settings.keepScreenOn) { viewModel.setKeepScreenOn(it) }
                SettingSwitch(stringResource(R.string.auto_trip), settings.autoTripEnabled) { viewModel.setAutoTrip(it) }
            }
        }

        SectionLabel(stringResource(R.string.gps))
        NoxCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.gps_filter), fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GpsFilterMode.entries.forEach { mode ->
                        FilterChip(selected = settings.filterMode == mode, onClick = { viewModel.setFilter(mode) }, label = { Text(mode.name) })
                    }
                }
            }
        }

        SectionLabel(stringResource(R.string.alerts))
        NoxCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SettingSwitch(stringResource(R.string.speed_alert_enabled), settings.speedAlertEnabled) { viewModel.setAlertEnabled(it) }
                Text("${stringResource(R.string.alert_speed)}: ${settings.alertSpeedKmh.toInt()} km/h", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(50.0, 90.0, 120.0, 130.0).forEach { value ->
                        FilterChip(selected = settings.alertSpeedKmh == value, onClick = { viewModel.setAlertSpeedKmh(value) }, label = { Text(value.toInt().toString()) })
                    }
                }
                SettingSwitch(stringResource(R.string.sound), settings.soundEnabled) { viewModel.setSound(it) }
                SettingSwitch(stringResource(R.string.vibration), settings.vibrationEnabled) { viewModel.setVibration(it) }
            }
        }

        SectionLabel(stringResource(R.string.privacy))
        NoxCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (app.consent.privacyOptionsRequired && activity != null) {
                    FullWidthOutline(stringResource(R.string.privacy_options)) { app.consent.showPrivacyOptions(activity) }
                }
                FullWidthOutline(stringResource(R.string.privacy_policy)) { openUrl(context, AppConfig.privacyUrl) }
                FullWidthOutline(stringResource(R.string.terms)) { openUrl(context, AppConfig.termsUrl) }
            }
        }

        SectionLabel(stringResource(R.string.data))
        NoxCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FullWidthOutline(stringResource(R.string.export_data)) { exportLauncher.launch("noxspeed-backup.json") }
                FullWidthOutline(stringResource(R.string.import_data)) { importLauncher.launch(arrayOf("application/json", "text/plain")) }
                FullWidthOutline(stringResource(R.string.erase_data)) { confirmErase = true }
            }
        }

        SectionLabel(stringResource(R.string.premium))
        NoxCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (premium) stringResource(R.string.premium_active) else stringResource(R.string.premium_body), color = if (premium) NoxCyan else NoxMuted)
                Button(onClick = onPremium, modifier = Modifier.fillMaxWidth()) { Text(if (premium) stringResource(R.string.premium_active) else stringResource(R.string.view_premium)) }
                OutlinedButton(onClick = { app.billing.refreshPurchases() }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.restore_purchases)) }
            }
        }

        SectionLabel(stringResource(R.string.about))
        NoxCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("NoxSpeed ${BuildConfig.VERSION_NAME}", fontWeight = FontWeight.Bold)
                Text("NoxForge Studios", color = NoxMuted)
                FullWidthOutline(stringResource(R.string.share_app)) {
                    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "NoxSpeed · ${AppConfig.playStoreUrl}")
                    }, null))
                }
                FullWidthOutline(stringResource(R.string.send_feedback)) {
                    if (AppConfig.supportEmail.isNotBlank()) {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${AppConfig.supportEmail}"))
                        runCatching { context.startActivity(intent) }
                    } else {
                        openUrl(context, "https://noxforgestudios.netlify.app/")
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        BannerAd(visible = !premium && !tripActive)
        Spacer(Modifier.height(110.dp))
    }

    if (confirmErase) {
        AlertDialog(
            onDismissRequest = { confirmErase = false },
            title = { Text(stringResource(R.string.erase_data)) },
            text = { Text(stringResource(R.string.erase_confirm)) },
            confirmButton = { TextButton(onClick = { confirmErase = false; viewModel.eraseAllData() }) { Text(stringResource(R.string.confirm)) } },
            dismissButton = { TextButton(onClick = { confirmErase = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun FullWidthOutline(label: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(label) }
}

private fun openUrl(context: android.content.Context, url: String) {
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
}
