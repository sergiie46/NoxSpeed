package com.noxforgestudios.noxspeed.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxforgestudios.noxspeed.ui.theme.NoxCyan
import com.noxforgestudios.noxspeed.ui.theme.NoxMuted
import com.noxforgestudios.noxspeed.ui.theme.NoxSurface2

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.padding(top = 18.dp, bottom = 8.dp),
        color = NoxMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.6.sp,
    )
}

@Composable
fun NoxCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(NoxSurface2.copy(alpha = 0.92f))
            .border(1.dp, Color.White.copy(alpha = 0.055f), RoundedCornerShape(20.dp))
            .padding(16.dp),
    ) { content() }
}

@Composable
fun Metric(label: String, value: String, modifier: Modifier = Modifier, accent: Boolean = false) {
    Column(modifier = modifier) {
        Text(label, color = NoxMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Text(
            value,
            color = if (accent) NoxCyan else MaterialTheme.colorScheme.onSurface,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun MetricRow(vararg items: Pair<String, String>, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        items.forEachIndexed { index, item ->
            Metric(item.first, item.second, modifier = Modifier.weight(1f), accent = index == 0)
        }
    }
}
