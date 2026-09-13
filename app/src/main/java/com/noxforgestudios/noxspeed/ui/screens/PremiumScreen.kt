package com.noxforgestudios.noxspeed.ui.screens

import android.app.Activity
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxforgestudios.noxspeed.NoxSpeedApplication
import com.noxforgestudios.noxspeed.R
import com.noxforgestudios.noxspeed.monetization.BillingManager
import com.noxforgestudios.noxspeed.ui.components.NoxCard
import com.noxforgestudios.noxspeed.ui.theme.NoxBlue
import com.noxforgestudios.noxspeed.ui.theme.NoxCyan
import com.noxforgestudios.noxspeed.ui.theme.NoxMuted

@Composable
fun PremiumScreen(
    billing: BillingManager.State,
    premium: Boolean,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val app = context.applicationContext as NoxSpeedApplication

    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF08131C), Color(0xFF05070B))))
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("NOXSPEED PRO", color = NoxCyan, fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 2.2.sp)
        Spacer(Modifier.height(18.dp))
        Text(stringResource(R.string.premium_title), color = Color.White, fontWeight = FontWeight.Black, fontSize = 34.sp)
        Text(stringResource(R.string.premium_body), color = NoxMuted, modifier = Modifier.padding(top = 8.dp), fontSize = 15.sp)

        Spacer(Modifier.height(22.dp))
        NoxCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                PremiumLine(stringResource(R.string.premium_no_ads))
                PremiumLine(stringResource(R.string.premium_dashboards))
                PremiumLine(stringResource(R.string.premium_huds))
                PremiumLine(stringResource(R.string.premium_custom))
                PremiumLine(stringResource(R.string.premium_stats))
                PremiumLine(stringResource(R.string.premium_export))
            }
        }

        Spacer(Modifier.height(22.dp))
        if (premium) {
            Text(stringResource(R.string.premium_active), color = NoxCyan, fontSize = 24.sp, fontWeight = FontWeight.Black)
        } else {
            Text(billing.price ?: "Google Play", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = { if (activity != null) app.billing.launchPurchase(activity) },
                enabled = billing.productDetails != null && !billing.pending,
                modifier = Modifier.fillMaxWidth().height(58.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NoxCyan, contentColor = Color.Black),
            ) {
                Text(if (billing.pending) stringResource(R.string.purchase_pending) else stringResource(R.string.unlock_premium), fontWeight = FontWeight.Black)
            }
            if (billing.message != null) {
                Text(billing.message, color = NoxMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }
            OutlinedButton(onClick = { app.billing.refreshPurchases() }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text(stringResource(R.string.restore_purchases))
            }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.back)) }
        Spacer(Modifier.height(24.dp))
        Text(stringResource(R.string.purchase_note), color = NoxMuted, fontSize = 11.sp)
    }
}

@Composable
private fun PremiumLine(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("✓", color = NoxCyan, fontSize = 20.sp, fontWeight = FontWeight.Black)
        Text(text, color = Color.White, modifier = Modifier.padding(start = 12.dp), fontWeight = FontWeight.SemiBold)
    }
}
