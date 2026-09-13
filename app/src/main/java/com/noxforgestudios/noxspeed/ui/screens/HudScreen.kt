package com.noxforgestudios.noxspeed.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxforgestudios.noxspeed.R
import com.noxforgestudios.noxspeed.data.prefs.HudStyle
import com.noxforgestudios.noxspeed.data.prefs.UserSettings
import com.noxforgestudios.noxspeed.gps.GpsSnapshot
import com.noxforgestudios.noxspeed.gps.Units
import com.noxforgestudios.noxspeed.trip.ActiveTrip
import com.noxforgestudios.noxspeed.ui.formatDuration
import com.noxforgestudios.noxspeed.ui.headingLabel
import com.noxforgestudios.noxspeed.ui.theme.NoxCyan
import com.noxforgestudios.noxspeed.ui.theme.NoxMuted

@Composable
fun HudScreen(
    gps: GpsSnapshot,
    settings: UserSettings,
    trip: ActiveTrip?,
    premium: Boolean,
    onMirrorChange: (Boolean) -> Unit,
    onStyleChange: (HudStyle) -> Unit,
    onExit: () -> Unit,
) {
    val activity = LocalContext.current as? Activity
    DisposableEffect(activity, settings.keepScreenOn) {
        if (activity != null) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            if (settings.keepScreenOn) activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            if (activity != null) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    val unit = Units.unitLabel(settings.speedUnit)
    val speed = Units.roundedSpeed(gps.speedMps, settings.speedUnit)
    val allowedStyles = if (premium) HudStyle.entries else HudStyle.entries.take(2)
    val activeStyle = settings.hudStyle.takeIf { it in allowedStyles } ?: HudStyle.BASIC
    val nextStyle = allowedStyles[(allowedStyles.indexOf(activeStyle) + 1) % allowedStyles.size]

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { scaleX = if (settings.hudMirror) -1f else 1f }
                .padding(horizontal = 42.dp, vertical = 18.dp),
            contentAlignment = Alignment.Center,
        ) {
            HudVisual(
                style = activeStyle,
                speed = speed,
                unit = unit,
                heading = headingLabel(gps.bearingDegrees),
                distance = Units.distanceLabel(trip?.distanceMeters ?: 0.0, settings.speedUnit),
                elapsed = formatDuration(trip?.elapsedMs ?: 0L),
                gpsAvailable = gps.available,
            )
        }

        Row(
            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).alpha(0.88f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = { onStyleChange(nextStyle) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF12161D)),
            ) { Text(activeStyle.name) }
            Button(
                onClick = { onMirrorChange(!settings.hudMirror) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF12161D)),
            ) { Text(stringResource(if (settings.hudMirror) R.string.hud_unmirror else R.string.hud_mirror)) }
            Button(
                onClick = onExit,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF12161D)),
            ) { Text(stringResource(R.string.exit)) }
        }
    }
}

@Composable
private fun HudVisual(
    style: HudStyle,
    speed: Int,
    unit: String,
    heading: String,
    distance: String,
    elapsed: String,
    gpsAvailable: Boolean,
) {
    when (style) {
        HudStyle.BASIC -> BasicHud(speed, unit, heading, distance, elapsed, NoxCyan, gpsAvailable)
        HudStyle.NIGHT -> BasicHud(speed, unit, heading, distance, elapsed, Color(0xFF59FF8A), gpsAvailable)
        HudStyle.SPORT -> SportHud(speed, unit, heading, distance, elapsed, gpsAvailable)
        HudStyle.MINIMAL -> MinimalHud(speed, unit, gpsAvailable)
        HudStyle.CYBER -> CyberHud(speed, unit, heading, distance, elapsed, gpsAvailable)
        HudStyle.CLASSIC -> ClassicHud(speed, unit, heading, distance, elapsed, gpsAvailable)
    }
}

@Composable
private fun BasicHud(speed: Int, unit: String, heading: String, distance: String, elapsed: String, accent: Color, gpsAvailable: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(speed.toString(), color = accent, fontWeight = FontWeight.Black, fontSize = 150.sp, lineHeight = 150.sp)
        Text(unit.uppercase(), color = NoxMuted, fontWeight = FontWeight.Bold, fontSize = 22.sp, letterSpacing = 4.sp)
        HudMetrics(heading, distance, elapsed)
        if (!gpsAvailable) HudSearching()
    }
}

@Composable
private fun SportHud(speed: Int, unit: String, heading: String, distance: String, elapsed: String, gpsAvailable: Boolean) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Text(speed.toString(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 140.sp, lineHeight = 140.sp)
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(bottom = 22.dp)) {
                Text(unit.uppercase(), color = Color(0xFFFF4B4B), fontWeight = FontWeight.Black, fontSize = 24.sp)
                Text(heading, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 28.sp)
            }
        }
        Canvas(Modifier.fillMaxWidth().height(18.dp)) {
            val segments = 24
            val gap = 7f
            val w = (size.width - gap * (segments - 1)) / segments
            val active = ((speed / 240f).coerceIn(0f, 1f) * segments).toInt()
            repeat(segments) { i ->
                drawRoundRect(
                    color = if (i < active) Color(0xFFFF4B4B) else Color.White.copy(alpha = .12f),
                    topLeft = Offset(i * (w + gap), 0f), size = Size(w, size.height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f,4f),
                )
            }
        }
        HudMetrics(heading, distance, elapsed)
        if (!gpsAvailable) HudSearching()
    }
}

@Composable
private fun MinimalHud(speed: Int, unit: String, gpsAvailable: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(speed.toString(), color = Color.White, fontWeight = FontWeight.Light, fontSize = 170.sp, lineHeight = 170.sp)
        Text(unit.uppercase(), color = Color.White.copy(alpha=.55f), fontWeight = FontWeight.Bold, fontSize = 18.sp, letterSpacing = 6.sp)
        if (!gpsAvailable) HudSearching()
    }
}

@Composable
private fun CyberHud(speed: Int, unit: String, heading: String, distance: String, elapsed: String, gpsAvailable: Boolean) {
    val accent = Color(0xFFB067FF)
    Column(
        Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(Color.Transparent, accent.copy(alpha=.12f), Color.Transparent)), RoundedCornerShape(28.dp)).border(1.dp, accent.copy(alpha=.5f), RoundedCornerShape(28.dp)).padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("[ HUD // LIVE ]", color = accent, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
        Text(speed.toString().padStart(3,'0'), color = Color.White, fontWeight = FontWeight.Black, fontSize = 135.sp, lineHeight = 135.sp)
        Text(unit.uppercase(), color = accent, fontSize = 18.sp, fontWeight = FontWeight.Black, letterSpacing = 5.sp)
        HudMetrics(heading, distance, elapsed)
        if (!gpsAvailable) HudSearching()
    }
}

@Composable
private fun ClassicHud(speed: Int, unit: String, heading: String, distance: String, elapsed: String, gpsAvailable: Boolean) {
    val accent = Color(0xFFFFC05C)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(Modifier.height(230.dp).fillMaxWidth(.34f)) {
                val radius = size.minDimension*.43f
                drawCircle(Color.White.copy(alpha=.07f), radius)
                drawCircle(accent.copy(alpha=.55f), radius, style=Stroke(6f))
                drawArc(accent, 140f, (speed/240f).coerceIn(0f,1f)*260f, false, style=Stroke(12f, cap=StrokeCap.Round))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(speed.toString(), color=Color.White, fontSize=72.sp, lineHeight=72.sp, fontWeight=FontWeight.Black)
                Text(unit.uppercase(), color=accent, fontWeight=FontWeight.Bold)
            }
        }
        Column(verticalArrangement=Arrangement.spacedBy(14.dp)) {
            Text(heading, color=Color.White, fontSize=36.sp, fontWeight=FontWeight.Bold)
            Text(distance, color=NoxMuted, fontSize=24.sp)
            Text(elapsed, color=NoxMuted, fontSize=24.sp)
            if (!gpsAvailable) HudSearching()
        }
    }
}

@Composable
private fun HudMetrics(heading: String, distance: String, elapsed: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(42.dp), modifier = Modifier.padding(top = 8.dp)) {
        Text(heading, color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.SemiBold)
        Text(distance, color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.SemiBold)
        Text(elapsed, color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun HudSearching() {
    Text(stringResource(R.string.searching_gps), modifier = Modifier.padding(top = 12.dp), color = NoxMuted, fontWeight = FontWeight.Bold)
}
