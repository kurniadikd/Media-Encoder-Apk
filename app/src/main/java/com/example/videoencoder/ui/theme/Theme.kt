package com.example.videoencoder.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Rich Vibrant Dark Color Scheme
private val VibrantDarkColorScheme = darkColorScheme(
    primary = Color(0xFF818CF8),             // Neon Indigo
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = Color(0xFF3730A3),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFF22D3EE),           // Electric Cyan
    onSecondary = Color(0xFF083344),
    secondaryContainer = Color(0xFF164E63),
    onSecondaryContainer = Color(0xFFCFFAFE),
    tertiary = Color(0xFFF472B6),            // Radiant Pink
    onTertiary = Color(0xFF500724),
    tertiaryContainer = Color(0xFF831843),
    onTertiaryContainer = Color(0xFFFCE7F3),
    background = Color(0xFF0F172A),          // Sleek Slate Background
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),             // Surface Glass Variant
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF64748B),
    outlineVariant = Color(0xFF475569)
)

// Rich Vibrant Light Color Scheme
private val VibrantLightColorScheme = lightColorScheme(
    primary = Color(0xFF4F46E5),             // Deep Electric Indigo
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF1E1B4B),
    secondary = Color(0xFF0891B2),           // Deep Cyan
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCFFAFE),
    onSecondaryContainer = Color(0xFF083344),
    tertiary = Color(0xFFDB2777),            // Deep Pink
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFCE7F3),
    onTertiaryContainer = Color(0xFF500724),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFF1F5F9),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFF94A3B8),
    outlineVariant = Color(0xFFCBD5E1)
)

@Composable
fun VideoEncoderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Forced false for consistent custom Vibrant theme!
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) VibrantDarkColorScheme else VibrantLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
