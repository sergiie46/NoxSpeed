package com.noxforgestudios.noxspeed.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxforgestudios.noxspeed.R
import com.noxforgestudios.noxspeed.ui.theme.NoxCyan
import com.noxforgestudios.noxspeed.ui.theme.NoxMuted

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    onPermissionResult: () -> Unit,
) {
    var page by remember { mutableIntStateOf(0) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        onPermissionResult()
        onFinished()
    }

    val titles = listOf(
        stringResource(R.string.onboarding_1_title),
        stringResource(R.string.onboarding_2_title),
        stringResource(R.string.onboarding_3_title),
        stringResource(R.string.onboarding_4_title),
    )
    val bodies = listOf(
        stringResource(R.string.onboarding_1_body),
        stringResource(R.string.onboarding_2_body),
        stringResource(R.string.onboarding_3_body),
        stringResource(R.string.onboarding_4_body),
    )

    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF07131B), Color(0xFF05070B))))
            .padding(24.dp),
    ) {
        Text("NOXSPEED", color = NoxCyan, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        Spacer(Modifier.weight(1f))
        Text(titles[page], color = Color.White, fontSize = 42.sp, lineHeight = 44.sp, fontWeight = FontWeight.Black)
        Text(bodies[page], color = NoxMuted, fontSize = 17.sp, lineHeight = 25.sp, modifier = Modifier.padding(top = 14.dp))
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(4) { index ->
                Box(
                    Modifier
                        .clip(CircleShape)
                        .background(if (index == page) NoxCyan else Color.White.copy(alpha = 0.16f))
                        .height(5.dp)
                        .weight(1f)
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                if (page < 3) page++
                else permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            },
            modifier = Modifier.fillMaxWidth().height(58.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NoxCyan, contentColor = Color.Black),
        ) {
            Text(stringResource(if (page == 3) R.string.enable_location else R.string.continue_label), fontWeight = FontWeight.Black)
        }
        if (page == 3) {
            androidx.compose.material3.TextButton(onClick = onFinished, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(stringResource(R.string.not_now), color = NoxMuted)
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(stringResource(R.string.safety_notice), color = NoxMuted.copy(alpha = 0.8f), fontSize = 11.sp)
    }
}
