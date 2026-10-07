package com.cafemanager.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

// همه رنگ‌های سطح (کارت، منو، دیالوگ) آبی کم‌رنگ هستند، نه صورتی/بنفش پیش‌فرض
private val Light = lightColorScheme(
    primary = Color(0xFF0F766E), onPrimary = Color.White,
    primaryContainer = Color(0xFFB9ECE2), onPrimaryContainer = Color(0xFF00201C),
    secondary = Color(0xFF2F6F9E), onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFE6F7), onSecondaryContainer = Color(0xFF0B3350),
    tertiary = Color(0xFF3B7EA8), onTertiary = Color.White,
    background = Color(0xFFF3F8FB), onBackground = Color(0xFF14232E),
    surface = Color(0xFFF3F8FB), onSurface = Color(0xFF14232E),
    surfaceVariant = Color(0xFFDCEAF4), onSurfaceVariant = Color(0xFF3E5566),
    surfaceTint = Color(0xFF0F766E),
    outline = Color(0xFF6C8799), outlineVariant = Color(0xFFBBD0DE),
    surfaceBright = Color(0xFFF3F8FB), surfaceDim = Color(0xFFD3E1EB),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFEEF5FA),
    surfaceContainer = Color(0xFFE6F0F7),
    surfaceContainerHigh = Color(0xFFDFEBF4),
    surfaceContainerHighest = Color(0xFFD8E6F1)
)

private val Dark = darkColorScheme(
    primary = Color(0xFF5EEAD4), onPrimary = Color(0xFF003731),
    primaryContainer = Color(0xFF00504A), onPrimaryContainer = Color(0xFFB9ECE2),
    secondary = Color(0xFF8CC8F0), onSecondary = Color(0xFF00344F),
    secondaryContainer = Color(0xFF1F4A66), onSecondaryContainer = Color(0xFFCFE6F7),
    tertiary = Color(0xFF8CC8F0), onTertiary = Color(0xFF00344F),
    background = Color(0xFF0E161C), onBackground = Color(0xFFE1EAF0),
    surface = Color(0xFF0E161C), onSurface = Color(0xFFE1EAF0),
    surfaceVariant = Color(0xFF2A3A46), onSurfaceVariant = Color(0xFFB9CAD6),
    surfaceTint = Color(0xFF5EEAD4),
    outline = Color(0xFF8399A8), outlineVariant = Color(0xFF3A4D5A),
    surfaceBright = Color(0xFF2B3A45), surfaceDim = Color(0xFF0E161C),
    surfaceContainerLowest = Color(0xFF0A1116),
    surfaceContainerLow = Color(0xFF141E25),
    surfaceContainer = Color(0xFF18242C),
    surfaceContainerHigh = Color(0xFF1E2B34),
    surfaceContainerHighest = Color(0xFF25343E)
)

private fun TextStyle.b() = copy(fontWeight = FontWeight.Bold)

private val Base = Typography()
private val BoldTypography = Typography(
    displayLarge = Base.displayLarge.b(), displayMedium = Base.displayMedium.b(),
    displaySmall = Base.displaySmall.b(), headlineLarge = Base.headlineLarge.b(),
    headlineMedium = Base.headlineMedium.b(), headlineSmall = Base.headlineSmall.b(),
    titleLarge = Base.titleLarge.b(), titleMedium = Base.titleMedium.b(),
    titleSmall = Base.titleSmall.b(), bodyLarge = Base.bodyLarge.b(),
    bodyMedium = Base.bodyMedium.b(), bodySmall = Base.bodySmall.b(),
    labelLarge = Base.labelLarge.b(), labelMedium = Base.labelMedium.b(),
    labelSmall = Base.labelSmall.b()
)

@Composable
fun CafeTheme(dark: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (dark) Dark else Light,
        typography = BoldTypography,
        content = content
    )
}
