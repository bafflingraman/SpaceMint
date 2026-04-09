package com.spacemint.app.ui.theme

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

private val LightColors = lightColorScheme(
    primary          = Color(0xFF1D9E75),
    onPrimary        = Color.White,
    background       = Color(0xFFF8FAF8),
    onBackground     = Color(0xFF1A1A1A),
    surface          = Color.White,
    onSurface        = Color(0xFF1A1A1A),
    surfaceVariant   = Color(0xFFF0F0F0),
    onSurfaceVariant = Color(0xFF777777),
    outline          = Color(0xFFE0E0E0)
)

private val DarkColors = darkColorScheme(
    primary          = Color(0xFF1D9E75),
    onPrimary        = Color.White,
    background       = Color(0xFF0F0F0F),
    onBackground     = Color(0xFFF1F1F1),
    surface          = Color(0xFF1A1A1A),
    onSurface        = Color(0xFFF1F1F1),
    surfaceVariant   = Color(0xFF252525),
    onSurfaceVariant = Color(0xFFAAAAAA),
    outline          = Color(0xFF2A2A2A)
)

@Composable
fun SpaceMintTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content     = content
    )
}