package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Isaya Sushi Vibrant Luxury Palette (Lively Japanese Slate, Crimson, Imperial Gold & Emerald)
val DeepBlack = Color(0xFF10131A) // Rich deep midnight obsidian (not dull pitch black)
val DarkSurface = Color(0xFF161A26) // Deep navy slate surface
val DarkCard = Color(0xFF1E2333) // Vibrant dark card background
val DarkCardElevated = Color(0xFF282E44) // Luminous elevated card
val DarkBorder = Color(0xFF3B4460) // High-contrast subtle border
val DarkCardBorder = DarkBorder

// Radiant Gold Accents
val GoldPrimary = Color(0xFFFFC72C) // Radiant Vibrant Sun Gold
val GoldLight = Color(0xFFFFF1BD) // Soft luminous gold
val GoldDark = Color(0xFFB8860B) // Rich deep gold
val GoldContainer = Color(0xFF382D0F) // Warm gold container

// Radiant Crimson Sushi Red
val SushiRed = Color(0xFFFF2A4D) // Bright vibrant Crimson Coral
val SushiRedLight = Color(0xFFFF6B85) // Energetic lively pink-red
val SushiRedDark = Color(0xFFB30C28) // Deep royal crimson
val SushiRedContainer = Color(0xFF4D101C) // Rich crimson container

// Status Colors - Ultra Vivid & High Contrast
val StatusGreen = Color(0xFF00E676) // Radiant lively emerald
val SuccessGreen = StatusGreen
val StatusGreenContainer = Color(0xFF0F3D24)
val StatusOrange = Color(0xFFFF9500) // Electric amber orange
val StatusOrangeContainer = Color(0xFF472800)
val StatusBlue = Color(0xFF00B4D8) // Radiant electric cyan azure
val StatusBlueContainer = Color(0xFF072D42)
val StatusPurple = Color(0xFFA855F7) // Vibrant royal violet

// Vivid Neutrals & Typography
val TextPrimary = Color(0xFFFFFFFF) // Crisp bright white
val TextSecondary = Color(0xFFCBD5E1) // Crisp silver slate
val TextMuted = Color(0xFF94A3B8) // Clear legible slate

// Vivid Multi-Color Gradients
val GradientGoldCrimson = Brush.horizontalGradient(
    listOf(GoldPrimary, SushiRed)
)
val GradientSushiHeader = Brush.linearGradient(
    listOf(Color(0xFF2A1520), Color(0xFF161A26), Color(0xFF1B2335))
)
val GradientKitchenActive = Brush.horizontalGradient(
    listOf(SushiRed, Color(0xFFFF6B4A))
)
val GradientDispatched = Brush.horizontalGradient(
    listOf(Color(0xFF00B4D8), StatusGreen)
)
val GradientGoldBanner = Brush.horizontalGradient(
    listOf(GoldDark, GoldPrimary, GoldLight)
)
val GradientCardSubtle = Brush.verticalGradient(
    listOf(Color(0xFF242A3E), Color(0xFF1A1F2E))
)

// --- iOS Clean Light Theme Palette (App Clientes) ---
val IosLightBg = Color(0xFFF8F9FB)
val IosLightSurface = Color(0xFFFFFFFF)
val IosLightCard = Color(0xFFFFFFFF)
val IosLightCardSecondary = Color(0xFFF0F3F8)
val IosLightBorder = Color(0xFFE2E8F0)
val IosLightBorderSubtle = Color(0xFFEDF2F7)

val IosTextPrimary = Color(0xFF0F172A)
val IosTextSecondary = Color(0xFF334155)
val IosTextMuted = Color(0xFF64748B)

val IosGoldPrimary = Color(0xFFD97706)
val IosGoldLight = Color(0xFF92400E)
val IosGoldContainer = Color(0xFFFEF3C7)
val IosGoldBorder = Color(0xFFFDE68A)

val IosRedPrimary = Color(0xFFEF4444)
val IosRedLight = Color(0xFFDC2626)
val IosRedContainer = Color(0xFFFEE2E2)
val IosRedBorder = Color(0xFFFECACA)

val IosGreenPrimary = Color(0xFF10B981)
val IosGreenContainer = Color(0xFFD1FAE5)
val IosGreenBorder = Color(0xFFA7F3D0)

val IosInputBackground = Color(0xFFF1F5F9)
val IosInputBorder = Color(0xFFCBD5E1)
val IosInputFocusedBorder = Color(0xFFD97706)

// Dark Theme Map Palette for GPS Selector
val IosDarkMapBg = Color(0xFF10131A)
val IosDarkMapSurface = Color(0xFF1A1F2D)
val IosDarkMapCard = Color(0xFF252C3E)
val IosDarkMapBorder = Color(0xFF3B4460)




