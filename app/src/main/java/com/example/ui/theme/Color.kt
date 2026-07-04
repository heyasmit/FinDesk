package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// Raw Color Constants for Dark Mode
val DarkMidnightBg = Color(0xFF0A0C10)       // Rich immersive deep dark background
val DarkSlateSurface = Color(0xFF161B22)     // Immersive card slate surface
val DarkSlateSurfaceVariant = Color(0xFF21262D) // Elevated/active slate variant
val DarkSlateBorder = Color(0xFF30363D)      // GitHub-style dark border
val DarkTextPrimary = Color(0xFFF3F4F6)       // Contrast primary text
val DarkTextSecondary = Color(0xFF9CA3AF)     // Contrast secondary text
val DarkTextMuted = Color(0xFF6B7280)         // Low-contrast hints

// Raw Color Constants for Light Mode
val LightBg = Color(0xFFF4F6F9)              // Warm/cool light slate
val LightSurface = Color(0xFFFFFFFF)         // Crisp white card
val LightSurfaceVariant = Color(0xFFEDF2F7)  // Soft slate variant
val LightBorder = Color(0xFFE2E8F0)          // Clean border line
val LightTextPrimary = Color(0xFF0F172A)     // Deep charcoal
val LightTextSecondary = Color(0xFF475569)   // Muted charcoal
val LightTextMuted = Color(0xFF64748B)       // Slate grey hints

// Premium Growth Accents (Vibrant Emerald)
val EmeraldGreen = Color(0xFF10B981)      // Vibrant emerald theme color
val EmeraldGlow = Color(0xFF34D399)       // Bright emerald glow highlight
val EmeraldDark = Color(0xFF065F46)       // Deep teal / emerald shadow
val EmeraldMuted = Color(0xFF1F2937)      // Dark neutral-slate for secondary states

// Luxury & Utility Colors
val PremiumGold = Color(0xFFF59E0B)       // Net Worth status indicator
val RedLoss = Color(0xFFEF4444)           // Outflows or loss indicator
val BlueMf = Color(0xFF3B82F6)            // Mutual fund indicator
val CashTeal = Color(0xFF06B6D4)          // Cash asset indicator

// Dynamic Theme Getters (Adapt based on MaterialTheme colorScheme background)
val MidnightBg: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.background

val SlateSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surface

val SlateSurfaceVariant: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surfaceVariant

val SlateBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.outline

val TextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onBackground

val TextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = if (MaterialTheme.colorScheme.background == DarkMidnightBg) DarkTextSecondary else LightTextSecondary

val TextMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = if (MaterialTheme.colorScheme.background == DarkMidnightBg) DarkTextMuted else LightTextMuted

