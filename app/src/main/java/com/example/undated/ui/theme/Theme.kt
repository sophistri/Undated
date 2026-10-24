package com.example.undated.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.material3.ColorProviders

val LightColorScheme = lightColorScheme(
    primary = AppleBlossom,
    onPrimary = Cream,
    primaryContainer = DarkPeach,
    onPrimaryContainer = LunarGreen,
    secondary = LunarGreen,
    onSecondary = Cream,
    tertiary = DarkPeach,
    onTertiary = LunarGreen,
    background = Cream,
    onBackground = LunarGreen,
    surface = Cream,
    onSurface = LunarGreen,
    surfaceVariant = Butter,
    onSurfaceVariant = LunarGreen,
    outline = LunarGreen
)

val DarkColorScheme = darkColorScheme(
    primary = DarkPeach,
    onPrimary = LunarGreen,
    primaryContainer = AppleBlossom,
    onPrimaryContainer = Cream,
    secondary = Butter,
    onSecondary = Night,
    tertiary = AppleBlossom,
    onTertiary = Cream,
    background = Night,
    onBackground = Cream,
    surface = Night,
    onSurface = Cream,
    surfaceVariant = NightSurface,
    onSurfaceVariant = Cream,
    outline = DarkPeach
)

val UndatedWidgetColors = ColorProviders(light = LightColorScheme, dark = DarkColorScheme)

private val RetroShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(6.dp)
)

@Composable
fun UndatedTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = RetroShapes,
        content = content
    )
}