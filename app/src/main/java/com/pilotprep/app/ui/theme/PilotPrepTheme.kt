package com.pilotprep.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

/**
 * Thèmes personnalisés pour l'application
 */
object PilotPrepTheme {
    val LightColors = lightColorScheme(
        primary = Color(0xFF1976D2),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFBBDEFB),
        onPrimaryContainer = Color(0xFF0D47A1),
        secondary = Color(0xFF424242),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFE0E0E0),
        onSecondaryContainer = Color(0xFF212121),
        tertiary = Color(0xFF388E3C),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFC8E6C9),
        onTertiaryContainer = Color(0xFF1B5E20),
        error = Color(0xFFD32F2F),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFCDD2),
        onErrorContainer = Color(0xFFB71C1C),
        background = Color(0xFFFAFAFA),
        onBackground = Color(0xFF212121),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF212121),
        surfaceVariant = Color(0xFFF5F5F5),
        onSurfaceVariant = Color(0xFF424242),
        outline = Color(0xFF757575),
        outlineVariant = Color(0xFFBDBDBD),
        scrim = Color(0xFF000000),
        inverseSurface = Color(0xFF2C2C2C),
        inverseOnSurface = Color(0xFFF5F5F5),
        inversePrimary = Color(0xFF90CAF9),
        surfaceDim = Color(0xFFE0E0E0),
        surfaceBright = Color(0xFFFFFFFF),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFFAFAFA),
        surfaceContainer = Color(0xFFF5F5F5),
        surfaceContainerHigh = Color(0xFFEEEEEE),
        surfaceContainerHighest = Color(0xFFE8E8E8)
    )
    
    val DarkColors = darkColorScheme(
        primary = Color(0xFF90CAF9),
        onPrimary = Color(0xFF0D47A1),
        primaryContainer = Color(0xFF1565C0),
        onPrimaryContainer = Color(0xFFE3F2FD),
        secondary = Color(0xFFBDBDBD),
        onSecondary = Color(0xFF212121),
        secondaryContainer = Color(0xFF424242),
        onSecondaryContainer = Color(0xFFE0E0E0),
        tertiary = Color(0xFF81C784),
        onTertiary = Color(0xFF1B5E20),
        tertiaryContainer = Color(0xFF2E7D32),
        onTertiaryContainer = Color(0xFFC8E6C9),
        error = Color(0xFFEF5350),
        onError = Color(0xFFB71C1C),
        errorContainer = Color(0xFFD32F2F),
        onErrorContainer = Color(0xFFFFCDD2),
        background = Color(0xFF121212),
        onBackground = Color(0xFFE0E0E0),
        surface = Color(0xFF1E1E1E),
        onSurface = Color(0xFFE0E0E0),
        surfaceVariant = Color(0xFF2C2C2C),
        onSurfaceVariant = Color(0xFFBDBDBD),
        outline = Color(0xFF757575),
        outlineVariant = Color(0xFF424242),
        scrim = Color(0xFF000000),
        inverseSurface = Color(0xFFE0E0E0),
        inverseOnSurface = Color(0xFF212121),
        inversePrimary = Color(0xFF1976D2),
        surfaceDim = Color(0xFF0F0F0F),
        surfaceBright = Color(0xFF363636),
        surfaceContainerLowest = Color(0xFF0D0D0D),
        surfaceContainerLow = Color(0xFF1A1A1A),
        surfaceContainer = Color(0xFF1E1E1E),
        surfaceContainerHigh = Color(0xFF282828),
        surfaceContainerHighest = Color(0xFF333333)
    )
    
    // Thème Aviation (bleu ciel)
    val AviationLightColors = lightColorScheme(
        primary = Color(0xFF2196F3),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFBBDEFB),
        onPrimaryContainer = Color(0xFF0D47A1),
        secondary = Color(0xFF03A9F4),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFB3E5FC),
        onSecondaryContainer = Color(0xFF01579B),
        tertiary = Color(0xFF00BCD4),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFB2EBF2),
        onTertiaryContainer = Color(0xFF006064),
        background = Color(0xFFF8F9FA),
        onBackground = Color(0xFF1A1A1A),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF1A1A1A),
        surfaceVariant = Color(0xFFF1F3F4),
        onSurfaceVariant = Color(0xFF424242)
    )
    
    val AviationDarkColors = darkColorScheme(
        primary = Color(0xFF64B5F6),
        onPrimary = Color(0xFF0D47A1),
        primaryContainer = Color(0xFF1976D2),
        onPrimaryContainer = Color(0xFFE3F2FD),
        secondary = Color(0xFF4FC3F7),
        onSecondary = Color(0xFF01579B),
        secondaryContainer = Color(0xFF0277BD),
        onSecondaryContainer = Color(0xFFB3E5FC),
        tertiary = Color(0xFF4DD0E1),
        onTertiary = Color(0xFF006064),
        tertiaryContainer = Color(0xFF0097A7),
        onTertiaryContainer = Color(0xFFB2EBF2),
        background = Color(0xFF0A0E13),
        onBackground = Color(0xFFE1F5FE),
        surface = Color(0xFF111827),
        onSurface = Color(0xFFE1F5FE),
        surfaceVariant = Color(0xFF1F2937),
        onSurfaceVariant = Color(0xFFB0BEC5)
    )
    
    // Thème Cockpit (orange/rouge)
    val CockpitLightColors = lightColorScheme(
        primary = Color(0xFFFF9800),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFE0B2),
        onPrimaryContainer = Color(0xFFE65100),
        secondary = Color(0xFFFF5722),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFCCBC),
        onSecondaryContainer = Color(0xFFD84315),
        tertiary = Color(0xFFFFC107),
        onTertiary = Color(0xFF000000),
        tertiaryContainer = Color(0xFFFFF8E1),
        onTertiaryContainer = Color(0xFFF57F17),
        background = Color(0xFFFFFBF5),
        onBackground = Color(0xFF1A1A1A),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF1A1A1A),
        surfaceVariant = Color(0xFFFFF3E0),
        onSurfaceVariant = Color(0xFF424242)
    )
    
    val CockpitDarkColors = darkColorScheme(
        primary = Color(0xFFFFB74D),
        onPrimary = Color(0xFFE65100),
        primaryContainer = Color(0xFFFF9800),
        onPrimaryContainer = Color(0xFFFFE0B2),
        secondary = Color(0xFFFF8A65),
        onSecondary = Color(0xFFD84315),
        secondaryContainer = Color(0xFFFF5722),
        onSecondaryContainer = Color(0xFFFFCCBC),
        tertiary = Color(0xFFFFD54F),
        onTertiary = Color(0xFFF57F17),
        tertiaryContainer = Color(0xFFFFC107),
        onTertiaryContainer = Color(0xFFFFF8E1),
        background = Color(0xFF1A0F0A),
        onBackground = Color(0xFFFFF3E0),
        surface = Color(0xFF2C1810),
        onSurface = Color(0xFFFFF3E0),
        surfaceVariant = Color(0xFF3E2723),
        onSurfaceVariant = Color(0xFFBCAAA4)
    )
}

/**
 * Types de thèmes disponibles
 */
enum class ThemeType {
    SYSTEM,      // Suit le thème système
    LIGHT,       // Thème clair
    DARK,        // Thème sombre
    AVIATION,    // Thème aviation (bleu ciel)
    COCKPIT      // Thème cockpit (orange/rouge)
}

/**
 * Gestionnaire de thèmes
 */
@Composable
fun rememberThemeState(): MutableState<ThemeType> {
    return remember { mutableStateOf(ThemeType.SYSTEM) }
}

/**
 * Obtient le ColorScheme approprié selon le thème
 */
@Composable
fun getColorScheme(themeType: ThemeType, isSystemDark: Boolean): ColorScheme {
    return when (themeType) {
        ThemeType.SYSTEM -> if (isSystemDark) PilotPrepTheme.DarkColors else PilotPrepTheme.LightColors
        ThemeType.LIGHT -> PilotPrepTheme.LightColors
        ThemeType.DARK -> PilotPrepTheme.DarkColors
        ThemeType.AVIATION -> if (isSystemDark) PilotPrepTheme.AviationDarkColors else PilotPrepTheme.AviationLightColors
        ThemeType.COCKPIT -> if (isSystemDark) PilotPrepTheme.CockpitDarkColors else PilotPrepTheme.CockpitLightColors
    }
}

/**
 * Composable principal pour appliquer le thème
 */
@Composable
fun PilotPrepTheme(
    themeType: ThemeType = ThemeType.SYSTEM,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val colorScheme = getColorScheme(themeType, isSystemDark)
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

/**
 * Extension pour obtenir des couleurs personnalisées
 */
object CustomColors {
    val Success = Color(0xFF4CAF50)
    val Warning = Color(0xFFFF9800)
    val Info = Color(0xFF2196F3)
    val Error = Color(0xFFF44336)
    
    // Couleurs aviation
    val SkyBlue = Color(0xFF87CEEB)
    val AviationBlue = Color(0xFF1976D2)
    val RunwayGray = Color(0xFF757575)
    val CloudWhite = Color(0xFFF5F5F5)
    
    // Couleurs cockpit
    val InstrumentGreen = Color(0xFF4CAF50)
    val WarningRed = Color(0xFFF44336)
    val DisplayBlue = Color(0xFF2196F3)
    val ControlOrange = Color(0xFFFF9800)
}



