package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppThemeMode(val label: String) {
    PREMIUM_PURPLE("Premium Purple"),
    AMOLED_BLACK("AMOLED Black"),
    MIDNIGHT_BLUE("Midnight Blue"),
    EMERALD("Emerald"),
    LIGHT_MINIMAL("Light Minimal"),
    SYSTEM("System Default")
}

fun getAccentColor(name: String): Color {
    return when (name.uppercase()) {
        "CYAN" -> CyanAccent
        "EMERALD" -> EmeraldAccent
        "AMBER" -> AmberAccent
        "ROSE" -> RoseAccent
        "GOLD" -> GoldAccent
        else -> VioletPrimary
    }
}

fun buildColorScheme(themeMode: String, accentName: String, isSystemDark: Boolean): ColorScheme {
    val primaryColor = getAccentColor(accentName)

    val actualMode = when (themeMode) {
        "SYSTEM" -> if (isSystemDark) "PREMIUM_PURPLE" else "LIGHT_MINIMAL"
        "LIGHT" -> "LIGHT_MINIMAL"
        "DARK" -> "PREMIUM_PURPLE"
        else -> themeMode
    }

    return when (actualMode) {
        "AMOLED_BLACK" -> darkColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryColor.copy(alpha = 0.25f),
            onPrimaryContainer = Color.White,
            secondary = CyanAccent,
            onSecondary = Color.Black,
            secondaryContainer = CyanContainer,
            onSecondaryContainer = Color(0xFFCFFAFE),
            tertiary = EmeraldAccent,
            onTertiary = Color.Black,
            background = AmoledBackground,
            onBackground = TextWhite,
            surface = AmoledSurface,
            onSurface = TextWhite,
            surfaceVariant = AmoledSurfaceVariant,
            onSurfaceVariant = TextGrayLight,
            outline = AmoledBorder,
            outlineVariant = Color(0xFF1E1E26),
            error = RoseAccent,
            onError = Color.White
        )
        "MIDNIGHT_BLUE" -> darkColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = Color(0xFF14244D),
            onPrimaryContainer = Color(0xFFD6E4FF),
            secondary = CyanAccent,
            onSecondary = Color.Black,
            secondaryContainer = CyanContainer,
            onSecondaryContainer = Color(0xFFCFFAFE),
            tertiary = EmeraldAccent,
            onTertiary = Color.Black,
            background = MidnightBackground,
            onBackground = TextWhite,
            surface = MidnightSurface,
            onSurface = TextWhite,
            surfaceVariant = MidnightSurfaceVariant,
            onSurfaceVariant = TextGrayLight,
            outline = MidnightBorder,
            outlineVariant = Color(0xFF16254F),
            error = RoseAccent,
            onError = Color.White
        )
        "EMERALD" -> darkColorScheme(
            primary = if (primaryColor == VioletPrimary) EmeraldAccent else primaryColor,
            onPrimary = Color.White,
            primaryContainer = EmeraldContainer,
            onPrimaryContainer = Color(0xFFD1FAE5),
            secondary = CyanAccent,
            onSecondary = Color.Black,
            secondaryContainer = CyanContainer,
            onSecondaryContainer = Color(0xFFCFFAFE),
            tertiary = AmberAccent,
            onTertiary = Color.Black,
            background = EmeraldThemeBackground,
            onBackground = TextWhite,
            surface = EmeraldThemeSurface,
            onSurface = TextWhite,
            surfaceVariant = EmeraldThemeSurfaceVariant,
            onSurfaceVariant = TextGrayLight,
            outline = EmeraldThemeBorder,
            outlineVariant = Color(0xFF153326),
            error = RoseAccent,
            onError = Color.White
        )
        "LIGHT_MINIMAL" -> lightColorScheme(
            primary = if (primaryColor == VioletPrimary) VioletDark else primaryColor,
            onPrimary = Color.White,
            primaryContainer = Color(0xFFF1EEFE),
            onPrimaryContainer = primaryColor,
            secondary = CyanDark,
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFE0F7FA),
            onSecondaryContainer = CyanDark,
            tertiary = EmeraldAccent,
            onTertiary = Color.White,
            background = LightBackground,
            onBackground = TextDark,
            surface = LightSurface,
            onSurface = TextDark,
            surfaceVariant = LightSurfaceVariant,
            onSurfaceVariant = TextDarkMuted,
            outline = LightBorder,
            outlineVariant = Color(0xFFE2E8F0),
            error = RoseAccent,
            onError = Color.White
        )
        else -> darkColorScheme( // PREMIUM_PURPLE
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = VioletContainer,
            onPrimaryContainer = Color(0xFFEDE9FE),
            secondary = CyanAccent,
            onSecondary = Color.Black,
            secondaryContainer = CyanContainer,
            onSecondaryContainer = Color(0xFFCFFAFE),
            tertiary = EmeraldAccent,
            onTertiary = Color.Black,
            background = DarkBackground,
            onBackground = TextWhite,
            surface = DarkSurface,
            onSurface = TextWhite,
            surfaceVariant = DarkSurfaceVariant,
            onSurfaceVariant = TextGrayLight,
            outline = DarkBorder,
            outlineVariant = Color(0xFF251E48),
            error = RoseAccent,
            onError = Color.White
        )
    }
}

@Composable
fun RebootTheme(
    themeMode: String = "PREMIUM_PURPLE",
    accentColor: String = "VIOLET",
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val colorScheme = buildColorScheme(themeMode, accentColor, isSystemDark)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
