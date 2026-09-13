package com.noxforgestudios.noxspeed.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val NoxBlack = Color(0xFF05070B)
val NoxSurface = Color(0xFF0D121A)
val NoxSurface2 = Color(0xFF121A24)
val NoxCyan = Color(0xFF50E6FF)
val NoxBlue = Color(0xFF4D7CFF)
val NoxGreen = Color(0xFF52E59A)
val NoxAmber = Color(0xFFFFC857)
val NoxRed = Color(0xFFFF5C72)
val NoxText = Color(0xFFF3F7FB)
val NoxMuted = Color(0xFF8C99AA)

private val Dark = darkColorScheme(
    primary = NoxCyan,
    secondary = NoxBlue,
    tertiary = NoxGreen,
    background = NoxBlack,
    surface = NoxSurface,
    surfaceVariant = NoxSurface2,
    onPrimary = NoxBlack,
    onBackground = NoxText,
    onSurface = NoxText,
    error = NoxRed,
)

private val Light = lightColorScheme(
    primary = Color(0xFF006A7B),
    secondary = Color(0xFF3E5FBD),
    background = Color(0xFFF5F8FB),
    surface = Color.White,
    onBackground = Color(0xFF101419),
    onSurface = Color(0xFF101419),
)

@Composable
fun NoxSpeedTheme(content: @Composable () -> Unit) {
    // The product is intentionally dark-first for in-car legibility.
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) Dark else Dark, content = content)
}
