package com.noxforgestudios.noxspeed.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxforgestudios.noxspeed.ui.theme.NoxCyan
import com.noxforgestudios.noxspeed.ui.theme.NoxMuted
import com.noxforgestudios.noxspeed.ui.theme.NoxSurface2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpeedGauge(
    speed: Float,
    maxScale: Float,
    unit: String,
    modifier: Modifier = Modifier,
    accent: Color = NoxCyan,
) {
    val animated by animateFloatAsState(
        targetValue = speed.coerceIn(0f, maxScale),
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 220f),
        label = "speedGauge",
    )
    Box(modifier = modifier.size(310.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = size.minDimension * 0.026f
            val pad = size.minDimension * 0.08f
            val arcSize = Size(size.width - pad * 2, size.height - pad * 2)
            drawArc(
                color = NoxSurface2,
                startAngle = 140f,
                sweepAngle = 260f,
                useCenter = false,
                topLeft = Offset(pad, pad),
                size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
            val progress = if (maxScale <= 0f) 0f else animated / maxScale
            drawArc(
                color = accent,
                startAngle = 140f,
                sweepAngle = 260f * progress,
                useCenter = false,
                topLeft = Offset(pad, pad),
                size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )

            val center = this.center
            val radius = size.minDimension * 0.405f
            for (i in 0..20) {
                val angleDeg = 140f + 260f * (i / 20f)
                val angle = Math.toRadians(angleDeg.toDouble())
                val outer = Offset(
                    center.x + cos(angle).toFloat() * radius,
                    center.y + sin(angle).toFloat() * radius,
                )
                val innerR = radius - if (i % 5 == 0) size.minDimension * 0.045f else size.minDimension * 0.026f
                val inner = Offset(
                    center.x + cos(angle).toFloat() * innerR,
                    center.y + sin(angle).toFloat() * innerR,
                )
                drawLine(
                    color = if (i / 20f <= progress) accent.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.16f),
                    start = inner,
                    end = outer,
                    strokeWidth = if (i % 5 == 0) 4f else 2f,
                    cap = StrokeCap.Round,
                )
            }
        }
        androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = animated.toInt().toString(),
                color = Color.White,
                fontSize = 78.sp,
                lineHeight = 78.sp,
                fontWeight = FontWeight.Black,
            )
            Text(unit.uppercase(), color = NoxMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        }
    }
}
