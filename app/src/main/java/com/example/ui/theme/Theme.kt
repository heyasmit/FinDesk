package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val OmniDarkColorScheme = darkColorScheme(
    primary = EmeraldGreen,
    onPrimary = Color.Black,
    secondary = PremiumGold,
    onSecondary = Color.Black,
    tertiary = CashTeal,
    onTertiary = Color.Black,
    background = DarkMidnightBg,
    onBackground = DarkTextPrimary,
    surface = DarkSlateSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSlateSurfaceVariant,
    onSurfaceVariant = DarkTextPrimary,
    outline = DarkSlateBorder
)

private val OmniLightColorScheme = lightColorScheme(
    primary = EmeraldGreen,
    onPrimary = Color.White,
    secondary = PremiumGold,
    onSecondary = Color.Black,
    tertiary = CashTeal,
    onTertiary = Color.Black,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextPrimary,
    outline = LightBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Configurable theme
    dynamicColor: Boolean = false, // Disable dynamic colors to preserve our cohesive custom color brand
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) OmniDarkColorScheme else OmniLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
