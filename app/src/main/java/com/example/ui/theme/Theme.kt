package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val IsayaDarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = DeepBlack,
    primaryContainer = GoldContainer,
    onPrimaryContainer = GoldLight,
    secondary = SushiRed,
    onSecondary = TextPrimary,
    secondaryContainer = SushiRedContainer,
    onSecondaryContainer = SushiRedLight,
    background = DeepBlack,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    outlineVariant = GoldDark
)

@Composable
fun IsayaSushiTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = IsayaDarkColorScheme,
        typography = Typography,
        content = content
    )
}

// Keep alias for template compatibility if needed
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    IsayaSushiTheme(content = content)
}

