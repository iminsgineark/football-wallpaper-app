package com.meritshot.footballwallpaper.presentation.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Brand colours ──────────────────────────────────────────────
val GreenPrimary     = Color(0xFF00C853)
val GreenLight       = Color(0xFF69F0AE)
val GreenDark        = Color(0xFF00701A)
val BackgroundDark   = Color(0xFF0D0D0D)
val SurfaceDark      = Color(0xFF1A1A1A)
val SurfaceVariant   = Color(0xFF252525)
val CardBackground   = Color(0xFF1E1E1E)
val TextPrimary      = Color(0xFFFFFFFF)
val TextSecondary    = Color(0xFFB0B0B0)
val ErrorColor       = Color(0xFFCF6679)
val AdminAccent      = Color(0xFFFFD600)

private val DarkColorScheme = darkColorScheme(
    primary          = GreenPrimary,
    onPrimary        = Color.Black,
    primaryContainer = GreenDark,
    onPrimaryContainer = GreenLight,
    secondary        = AdminAccent,
    onSecondary      = Color.Black,
    background       = BackgroundDark,
    onBackground     = TextPrimary,
    surface          = SurfaceDark,
    onSurface        = TextPrimary,
    surfaceVariant   = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error            = ErrorColor,
    onError          = Color.White,
)

@Composable
fun FootballWallpaperTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = Typography(),
        content     = content
    )
}
