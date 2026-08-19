package com.example.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CyberBlue,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF003854),
    onPrimaryContainer = Color(0xFFB8EEFF),
    secondary = CyberCrimson,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF520014),
    onSecondaryContainer = Color(0xFFFFD9DF),
    tertiary = TitaniumGold,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF473300),
    onTertiaryContainer = Color(0xFFFFE08A),
    background = ObsidianBlack,
    onBackground = TextPrimary,
    surface = StealthSurface,
    onSurface = TextPrimary,
    surfaceVariant = StealthCard,
    onSurfaceVariant = TextSecondary,
    outline = StealthBorder
)

private val LightColorScheme = lightColorScheme(
    primary = CyberCobalt,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E4FF),
    onPrimaryContainer = Color(0xFF001B3D),
    secondary = CyberCrimson,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDADF),
    onSecondaryContainer = Color(0xFF40000C),
    tertiary = Color(0xFF8A5A00),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDF9E),
    onTertiaryContainer = Color(0xFF2C1A00),
    background = Color(0xFFF6F8FC),
    onBackground = Color(0xFF10141D),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF10141D),
    surfaceVariant = Color(0xFFE2E7F0),
    onSurfaceVariant = Color(0xFF424B5C),
    outline = Color(0xFFB5C0D3)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to aggressive stealth dark theme for creator dominance
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
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
