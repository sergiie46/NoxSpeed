package com.noxforgestudios.noxspeed.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxforgestudios.noxspeed.R
import com.noxforgestudios.noxspeed.gps.GpsSnapshot
import com.noxforgestudios.noxspeed.ui.AppViewModel
import com.noxforgestudios.noxspeed.ui.theme.NoxCyan
import com.noxforgestudios.noxspeed.ui.theme.NoxMuted

@Composable
fun AccelerationScreen(
    gps: GpsSnapshot,
    state: AppViewModel.AccelerationUiState,
    onArm: (Int) -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit,
) {
    val currentKmh = gps.speedMps * 3.6
    Column(
        Modifier.fillMaxSize().background(Color(0xFF05070B)).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.accel_title), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
        Text(stringResource(R.string.accel_safety), color = NoxMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(50, 100, 200).forEach { target ->
                FilterChip(selected = state.targetKmh == target, onClick = { if (!state.running) onArm(target) }, label = { Text("0-$target") })
            }
        }

        Spacer(Modifier.height(28.dp))
        Text("${currentKmh.toInt()}", color = Color.White, fontSize = 92.sp, fontWeight = FontWeight.Black, lineHeight = 92.sp)
        Text("km/h", color = NoxMuted, fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(Modifier.height(18.dp))

        val status = when {
            state.resultMs != null -> stringResource(R.string.accel_result)
            state.running -> stringResource(R.string.accel_go)
            state.armed -> stringResource(R.string.accel_ready)
            else -> stringResource(R.string.accel_ready)
        }
        Text(status, color = NoxCyan, fontSize = 18.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        Text("%.2f s".format((state.resultMs ?: state.elapsedMs) / 1000.0), color = Color.White, fontSize = 46.sp, fontWeight = FontWeight.Black)

        Spacer(Modifier.height(28.dp))
        if (!state.armed && state.resultMs == null) {
            Button(
                onClick = { onArm(state.targetKmh) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NoxCyan, contentColor = Color.Black),
            ) { Text("ARM 0-${state.targetKmh}", fontWeight = FontWeight.Black) }
        } else {
            OutlinedButton(onClick = onReset, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.accel_reset)) }
        }
        Spacer(Modifier.weight(1f))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.back)) }
    }
}
