package com.example.lsqrd.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary            = Blue40,
    onPrimary          = androidx.compose.ui.graphics.Color.White,
    primaryContainer   = Blue90,
    onPrimaryContainer = Blue10,
    secondary            = BlueGrey40,
    onSecondary          = androidx.compose.ui.graphics.Color.White,
    secondaryContainer   = BlueGrey90,
    onSecondaryContainer = BlueGrey10,
    background  = BackgroundLight,
    surface     = SurfaceLight,
    error       = ErrorRed,
)
private val DarkColorScheme = darkColorScheme(
    primary            = Blue80,
    onPrimary          = Blue20,
    primaryContainer   = Blue30,
    onPrimaryContainer = Blue90,
    secondary            = BlueGrey80,
    onSecondary          = BlueGrey10,
    secondaryContainer   = BlueGrey30,
    onSecondaryContainer = BlueGrey90,
    background  = BackgroundDark,
    surface     = SurfaceDark,
    error       = ErrorRedDark,
)

@Composable
fun LsqrdTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}