package com.example.myapplication.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Safety-focused color palette
private val SafetyGreen = Color(0xFF2E7D32)
private val SafetyGreenLight = Color(0xFF60AD5E)
private val SafetyGreenDark = Color(0xFF005005)
private val AlertRed = Color(0xFFD32F2F)
private val AlertRedLight = Color(0xFFFF6659)
private val WarningOrange = Color(0xFFF57C00)
private val WarningOrangeLight = Color(0xFFFFAD42)
private val InfoBlue = Color(0xFF1976D2)
private val InfoBlueLight = Color(0xFF63A4FF)

private val DarkColorScheme = darkColorScheme(
    primary = SafetyGreenLight,
    onPrimary = Color.Black,
    primaryContainer = SafetyGreenDark,
    onPrimaryContainer = SafetyGreenLight,
    secondary = InfoBlueLight,
    onSecondary = Color.Black,
    secondaryContainer = InfoBlue,
    onSecondaryContainer = InfoBlueLight,
    tertiary = WarningOrangeLight,
    onTertiary = Color.Black,
    tertiaryContainer = WarningOrange,
    onTertiaryContainer = WarningOrangeLight,
    error = AlertRedLight,
    onError = Color.Black,
    errorContainer = AlertRed,
    onErrorContainer = AlertRedLight,
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF1C1B1F),
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = SafetyGreen,
    onPrimary = Color.White,
    primaryContainer = SafetyGreenLight,
    onPrimaryContainer = SafetyGreenDark,
    secondary = InfoBlue,
    onSecondary = Color.White,
    secondaryContainer = InfoBlueLight,
    onSecondaryContainer = InfoBlue,
    tertiary = WarningOrange,
    onTertiary = Color.Black,
    tertiaryContainer = WarningOrangeLight,
    onTertiaryContainer = WarningOrange,
    error = AlertRed,
    onError = Color.White,
    errorContainer = AlertRedLight,
    onErrorContainer = AlertRed,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled to use our safety-focused colors
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
