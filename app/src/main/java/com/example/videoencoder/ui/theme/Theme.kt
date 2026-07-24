package com.example.videoencoder.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFA8C7FF),
    onPrimary = Color(0xFF003062),
    primaryContainer = Color(0xFF004689),
    onPrimaryContainer = Color(0xFFD6E3FF),
    secondary = Color(0xFF70DBB8),
    onSecondary = Color(0xFF00382B),
    secondaryContainer = Color(0xFF00513F),
    onSecondaryContainer = Color(0xFF8DF7D4),
    tertiary = Color(0xFFFFB3AC),
    onTertiary = Color(0xFF680007),
    tertiaryContainer = Color(0xFF8B0E14),
    onTertiaryContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0F141C),
    onBackground = Color(0xFFE1E2EC),
    surface = Color(0xFF1B202A),
    onSurface = Color(0xFFE1E2EC),
    surfaceVariant = Color(0xFF2E3544),
    onSurfaceVariant = Color(0xFFC3C6CF)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF005CBA),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD6E3FF),
    onPrimaryContainer = Color(0xFF001B3E),
    secondary = Color(0xFF006B54),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF8DF7D4),
    onSecondaryContainer = Color(0xFF002117),
    tertiary = Color(0xFFAF2B28),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDAD6),
    onTertiaryContainer = Color(0xFF410003),
    background = Color(0xFFF7F9FF),
    onBackground = Color(0xFF181C22),
    surface = Color(0xFFEDF0F9),
    onSurface = Color(0xFF181C22),
    surfaceVariant = Color(0xFFDFE2EC),
    onSurfaceVariant = Color(0xFF43474E)
)

@Composable
fun VideoEncoderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

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
