package com.noxforgestudios.noxspeed.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxforgestudios.noxspeed.data.prefs.DashboardStyle
import com.noxforgestudios.noxspeed.data.prefs.AccentPreset
import com.noxforgestudios.noxspeed.ui.theme.NoxAmber
import com.noxforgestudios.noxspeed.ui.theme.NoxBlue
import com.noxforgestudios.noxspeed.ui.theme.NoxCyan
import com.noxforgestudios.noxspeed.ui.theme.NoxGreen
import com.noxforgestudios.noxspeed.ui.theme.NoxMuted
import com.noxforgestudios.noxspeed.ui.theme.NoxRed
import kotlin.math.cos
import kotlin.math.sin

fun dashboardAccent(style: DashboardStyle): Color = when (style) {
    DashboardStyle.DIGITAL -> NoxCyan
    DashboardStyle.CLASSIC -> NoxAmber
    DashboardStyle.SPORT -> NoxRed
    DashboardStyle.NIGHT -> Color(0xFF6DE7FF)
    DashboardStyle.TRACK -> NoxGreen
    DashboardStyle.CYBER -> Color(0xFFB067FF)
    DashboardStyle.NEON -> Color(0xFFFF4FD8)
    DashboardStyle.OLED -> Color.White
    DashboardStyle.PERFORMANCE -> Color(0xFFFFC247)
    DashboardStyle.RETRO -> Color(0xFFFF9B55)
    DashboardStyle.RALLY -> Color(0xFFFFE269)
    DashboardStyle.SUPERCAR -> Color(0xFF48A8FF)
    DashboardStyle.MINIMAL_PRO -> Color(0xFFE6ECF5)
}

fun accentPresetColor(preset: AccentPreset): Color = when (preset) {
    AccentPreset.CYAN -> NoxCyan
    AccentPreset.RED -> NoxRed
    AccentPreset.GREEN -> NoxGreen
    AccentPreset.ORANGE -> NoxAmber
    AccentPreset.PURPLE -> Color(0xFFB067FF)
    AccentPreset.BLUE -> NoxBlue
    AccentPreset.YELLOW -> Color(0xFFFFE269)
    AccentPreset.WHITE -> Color.White
}

@Composable
fun DashboardGauge(style: DashboardStyle, speed: Float, maxScale: Float, unit: String, accentOverride: Color? = null) {
    val accent = accentOverride ?: dashboardAccent(style)
    when (style) {
        DashboardStyle.CLASSIC -> ClassicGauge(speed, maxScale, unit, accent)
        DashboardStyle.DIGITAL, DashboardStyle.OLED -> DigitalGauge(speed, unit, accent, oled = style == DashboardStyle.OLED)
        DashboardStyle.TRACK -> TrackGauge(speed, maxScale, unit, accent)
        DashboardStyle.CYBER -> CyberGauge(speed, maxScale, unit, accent)
        DashboardStyle.NEON -> NeonGauge(speed, maxScale, unit, accent)
        DashboardStyle.PERFORMANCE -> PerformanceGauge(speed, maxScale, unit, accent)
        DashboardStyle.RETRO -> RetroGauge(speed, maxScale, unit, accent)
        DashboardStyle.RALLY -> RallyGauge(speed, maxScale, unit, accent)
        DashboardStyle.SUPERCAR -> SupercarGauge(speed, maxScale, unit, accent)
        DashboardStyle.MINIMAL_PRO -> MinimalProGauge(speed, unit, accent)
        DashboardStyle.SPORT, DashboardStyle.NIGHT -> SpeedGauge(speed, maxScale, unit, accent = accent)
    }
}

@Composable
private fun DigitalGauge(speed: Float, unit: String, accent: Color, oled: Boolean) {
    val animated by animateFloatAsState(speed, spring(dampingRatio = 0.82f, stiffness = 240f), label = "digital")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(310.dp)
            .background(if (oled) Color.Black else Color(0xFF090E14), RoundedCornerShape(28.dp))
            .border(1.dp, accent.copy(alpha = if (oled) 0.08f else 0.25f), RoundedCornerShape(28.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("GPS", color = accent, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
        Text(animated.toInt().toString(), color = Color.White, fontSize = 112.sp, lineHeight = 112.sp, fontWeight = FontWeight.Black)
        Text(unit.uppercase(), color = NoxMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
        Box(Modifier.padding(top = 18.dp).fillMaxWidth(0.5f).height(3.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, accent, Color.Transparent))))
    }
}

@Composable
private fun ClassicGauge(speed: Float, maxScale: Float, unit: String, accent: Color) {
    val animated by animateFloatAsState(speed.coerceIn(0f, maxScale), spring(stiffness = 190f), label = "classic")
    Box(Modifier.size(310.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(300.dp)) {
            val center = this.center
            val radius = size.minDimension * .43f
            drawCircle(Color(0xFF0A0B0C), radius)
            drawCircle(Color.White.copy(alpha = .16f), radius, style = Stroke(2f))
            for (i in 0..32) {
                val deg = 135.0 + 270.0 * i / 32.0
                val rad = Math.toRadians(deg)
                val outer = Offset(center.x + cos(rad).toFloat() * radius, center.y + sin(rad).toFloat() * radius)
                val innerR = radius - if (i % 4 == 0) 28f else 15f
                val inner = Offset(center.x + cos(rad).toFloat() * innerR, center.y + sin(rad).toFloat() * innerR)
                drawLine(if (i % 4 == 0) Color.White else Color.White.copy(alpha = .35f), inner, outer, if (i % 4 == 0) 4f else 2f, StrokeCap.Round)
            }
            val progress = (animated / maxScale).coerceIn(0f, 1f)
            val needleAngle = Math.toRadians((135.0 + 270.0 * progress).toDouble())
            val needle = Offset(center.x + cos(needleAngle).toFloat() * radius * .74f, center.y + sin(needleAngle).toFloat() * radius * .74f)
            drawLine(accent, center, needle, 8f, StrokeCap.Round)
            drawCircle(accent, 12f, center)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 90.dp)) {
            Text(animated.toInt().toString(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 46.sp)
            Text(unit, color = NoxMuted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun TrackGauge(speed: Float, maxScale: Float, unit: String, accent: Color) {
    val progress by animateFloatAsState((speed / maxScale).coerceIn(0f, 1f), label = "track")
    Column(
        Modifier.fillMaxWidth().height(300.dp).background(Color(0xFF07100D), RoundedCornerShape(24.dp)).padding(22.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Text(speed.toInt().toString(), color = Color.White, fontSize = 100.sp, lineHeight = 100.sp, fontWeight = FontWeight.Black)
            Text(unit.uppercase(), color = accent, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
        }
        Canvas(Modifier.fillMaxWidth().height(38.dp)) {
            val gap = 7f
            val segments = 24
            val w = (size.width - gap * (segments - 1)) / segments
            repeat(segments) { i ->
                drawRoundRect(
                    color = if (i / segments.toFloat() <= progress) accent else Color.White.copy(alpha = .08f),
                    topLeft = Offset(i * (w + gap), 0f),
                    size = Size(w, size.height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f),
                )
            }
        }
        Text("LIVE TELEMETRY", color = NoxMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun CyberGauge(speed: Float, maxScale: Float, unit: String, accent: Color) {
    Box(
        Modifier.fillMaxWidth().height(305.dp)
            .background(Brush.radialGradient(listOf(accent.copy(alpha = .14f), Color(0xFF05070B))), RoundedCornerShape(22.dp))
            .border(1.dp, accent.copy(alpha = .5f), RoundedCornerShape(22.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxWidth().height(305.dp)) {
            val p = (speed / maxScale).coerceIn(0f, 1f)
            drawArc(accent.copy(alpha = .22f), 180f, 180f, false, Offset(size.width * .14f, size.height * .25f), Size(size.width * .72f, size.width * .72f), style = Stroke(10f))
            drawArc(accent, 180f, 180f * p, false, Offset(size.width * .14f, size.height * .25f), Size(size.width * .72f, size.width * .72f), style = Stroke(10f, cap = StrokeCap.Square))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("[ VELOCITY ]", color = accent, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            Text(speed.toInt().toString().padStart(3, '0'), color = Color.White, fontSize = 92.sp, fontWeight = FontWeight.Black)
            Text(unit.uppercase(), color = accent, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
        }
    }
}

@Composable
private fun NeonGauge(speed: Float, maxScale: Float, unit: String, accent: Color) {
    Box(
        Modifier.fillMaxWidth().height(305.dp).background(Color(0xFF090510), RoundedCornerShape(32.dp)).border(2.dp, accent.copy(alpha = .55f), RoundedCornerShape(32.dp)),
        contentAlignment = Alignment.Center,
    ) {
        SpeedGauge(speed, maxScale, unit, modifier = Modifier.size(290.dp), accent = accent)
        Text("NEON", color = accent.copy(alpha = .8f), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp, modifier = Modifier.align(Alignment.TopCenter).padding(top = 18.dp))
    }
}


@Composable
private fun PerformanceGauge(speed: Float, maxScale: Float, unit: String, accent: Color) {
    val progress by animateFloatAsState((speed / maxScale).coerceIn(0f, 1f), spring(stiffness = 230f), label = "performance")
    Column(
        Modifier.fillMaxWidth().height(305.dp).background(Color(0xFF0C0D10), RoundedCornerShape(22.dp)).padding(22.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("PERFORMANCE", color = accent, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Text(speed.toInt().toString(), color = Color.White, fontSize = 104.sp, lineHeight = 104.sp, fontWeight = FontWeight.Black)
            Text(unit.uppercase(), color = NoxMuted, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 15.dp))
        }
        Canvas(Modifier.fillMaxWidth().height(50.dp)) {
            val blocks = 18
            val gap = 6f
            val w = (size.width - gap * (blocks - 1)) / blocks
            repeat(blocks) { i ->
                val active = i < (blocks * progress).toInt()
                val h = size.height * (.34f + .66f * (i + 1) / blocks)
                drawRoundRect(
                    color = if (active) accent else Color.White.copy(alpha = .08f),
                    topLeft = Offset(i * (w + gap), size.height - h),
                    size = Size(w, h),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
                )
            }
        }
    }
}

@Composable
private fun RetroGauge(speed: Float, maxScale: Float, unit: String, accent: Color) {
    val animated by animateFloatAsState(speed.coerceIn(0f, maxScale), spring(stiffness = 155f), label = "retro")
    Box(
        Modifier.fillMaxWidth().height(305.dp).background(Color(0xFF17110B), RoundedCornerShape(18.dp)).border(2.dp, Color(0xFF654A2D), RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(280.dp)) {
            val r = size.minDimension * .42f
            drawCircle(Color(0xFF0D0A07), r)
            drawCircle(accent.copy(alpha = .42f), r, style = Stroke(3f))
            for (i in 0..24) {
                val deg = 140.0 + 260.0 * i / 24.0
                val rad = Math.toRadians(deg)
                val out = Offset(center.x + cos(rad).toFloat()*r, center.y + sin(rad).toFloat()*r)
                val ir = r - if (i%3==0) 25f else 13f
                val inn = Offset(center.x + cos(rad).toFloat()*ir, center.y + sin(rad).toFloat()*ir)
                drawLine(Color(0xFFE7D2A7).copy(alpha=if(i%3==0) .9f else .4f), inn, out, if(i%3==0) 4f else 2f)
            }
            val a = Math.toRadians((140.0 + 260.0*(animated/maxScale).coerceIn(0f,1f)).toDouble())
            drawLine(accent, center, Offset(center.x+cos(a).toFloat()*r*.73f,center.y+sin(a).toFloat()*r*.73f), 7f, StrokeCap.Round)
            drawCircle(accent, 10f, center)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 92.dp)) {
            Text(animated.toInt().toString(), color = Color(0xFFF7E7C5), fontSize = 42.sp, fontWeight = FontWeight.Black)
            Text(unit.uppercase(), color = Color(0xFFB99D75), fontSize = 10.sp, letterSpacing = 2.sp)
        }
    }
}

@Composable
private fun RallyGauge(speed: Float, maxScale: Float, unit: String, accent: Color) {
    val p by animateFloatAsState((speed/maxScale).coerceIn(0f,1f), label="rally")
    Column(
        Modifier.fillMaxWidth().height(305.dp).background(Color(0xFF090A0C), RoundedCornerShape(12.dp)).border(1.dp, Color.White.copy(alpha=.18f), RoundedCornerShape(12.dp)).padding(18.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("RALLY DATA", color = accent, fontSize=11.sp, fontWeight=FontWeight.Black, letterSpacing=2.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween, verticalAlignment=Alignment.Bottom) {
            Text(speed.toInt().toString().padStart(3,'0'), color=Color.White, fontSize=96.sp, lineHeight=98.sp, fontWeight=FontWeight.Black)
            Text(unit.uppercase(), color=accent, fontWeight=FontWeight.Black, modifier=Modifier.padding(bottom=15.dp))
        }
        Canvas(Modifier.fillMaxWidth().height(30.dp)) {
            drawRect(Color.White.copy(alpha=.08f))
            drawRect(accent, size=Size(size.width*p,size.height))
            repeat(10){ i -> drawLine(Color.Black.copy(alpha=.7f), Offset(size.width*i/10f,0f), Offset(size.width*i/10f,size.height), 3f) }
        }
    }
}

@Composable
private fun SupercarGauge(speed: Float, maxScale: Float, unit: String, accent: Color) {
    val p by animateFloatAsState((speed/maxScale).coerceIn(0f,1f), spring(stiffness=210f), label="supercar")
    Box(
        Modifier.fillMaxWidth().height(305.dp).background(Brush.horizontalGradient(listOf(Color(0xFF05080D), Color(0xFF0B1625), Color(0xFF05080D))), RoundedCornerShape(30.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxWidth().height(305.dp)) {
            val pad=size.width*.1f
            val arcSize=Size(size.width-pad*2, size.width-pad*2)
            drawArc(Color.White.copy(alpha=.07f), 205f,130f,false,Offset(pad,size.height*.18f),arcSize,style=Stroke(16f,cap=StrokeCap.Round))
            drawArc(accent,205f,130f*p,false,Offset(pad,size.height*.18f),arcSize,style=Stroke(16f,cap=StrokeCap.Round))
        }
        Column(horizontalAlignment=Alignment.CenterHorizontally) {
            Text(speed.toInt().toString(), color=Color.White, fontSize=108.sp, lineHeight=108.sp, fontWeight=FontWeight.Black)
            Text(unit.uppercase(), color=accent, fontSize=12.sp, fontWeight=FontWeight.Bold, letterSpacing=4.sp)
        }
    }
}

@Composable
private fun MinimalProGauge(speed: Float, unit: String, accent: Color) {
    val animated by animateFloatAsState(speed, spring(dampingRatio=.9f, stiffness=260f), label="minimalPro")
    Column(
        Modifier.fillMaxWidth().height(305.dp).background(Color.Black, RoundedCornerShape(34.dp)),
        horizontalAlignment=Alignment.CenterHorizontally,
        verticalArrangement=Arrangement.Center,
    ) {
        Text(animated.toInt().toString(), color=Color.White, fontSize=126.sp, lineHeight=126.sp, fontWeight=FontWeight.Light)
        Row(verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(6.dp).background(accent, RoundedCornerShape(50)))
            Text(unit.uppercase(), color=Color.White.copy(alpha=.52f), fontSize=12.sp, letterSpacing=4.sp)
        }
    }
}
