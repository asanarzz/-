package com.cafemanager.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Light = lightColorScheme(
    primary = Color(0xFF0F766E), onPrimary = Color.White,
    background = Color(0xFFF6F8F8), surface = Color.White
)
private val Dark = darkColorScheme(
    primary = Color(0xFF5EEAD4), onPrimary = Color(0xFF003731),
    background = Color(0xFF0F1417), surface = Color(0xFF161D21)
)

@Composable
fun CafeTheme(dark: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) Dark else Light, content = content)
}
