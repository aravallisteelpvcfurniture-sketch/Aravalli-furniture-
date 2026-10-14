package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BrandGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = BrandGreenContainer,
    onPrimaryContainer = BrandGreenContainerDark,
    secondary = BrandGreenHeader,
    onSecondary = Color.White,
    background = BrandBackground,
    onBackground = TextPrimary,
    surface = BrandSurface,
    onSurface = TextPrimary,
    surfaceVariant = BrandGreenContainer,
    onSurfaceVariant = TextSecondary,
    outline = BrandCardBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color(0xFF1B5E20),
    primaryContainer = Color(0xFF2E7D32),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFA5D6A7),
    onSecondary = Color(0xFF1B5E20),
    background = Color(0xFF121A14),
    onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF1A261D),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF233527),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF3B5740)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep branded showroom look
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
