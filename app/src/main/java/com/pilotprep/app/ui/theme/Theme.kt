package com.pilotprep.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- NOUVELLE PALETTE POUR LE THÈME SOMBRE ---
private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,              // Bleu plus clair pour le mode sombre
    onPrimary = Color(0xFF000000),      // Texte noir sur le bleu clair
    primaryContainer = DarkPrimary.copy(alpha = 0.2f), // Conteneurs avec transparence
    onPrimaryContainer = DarkOnSurface, // Texte sur les conteneurs accentués

    secondary = DarkSecondary,          // Vert plus clair pour le mode sombre
    onSecondary = Color(0xFF000000),    // Texte noir sur le vert clair

    tertiary = DarkTertiary,            // Orange plus clair pour le mode sombre
    onTertiary = Color(0xFF000000),     // Texte noir sur l'orange clair

    background = DarkBackground,        // Arrière-plan principal de l'écran
    onBackground = DarkOnBackground,    // Texte sur l'arrière-plan principal

    surface = DarkSurface,              // Couleur des cartes, menus, etc.
    onSurface = DarkOnSurface,          // Texte sur les cartes

    surfaceVariant = DarkSurface.copy(alpha = 0.8f), // Pour les éléments légèrement différents
    onSurfaceVariant = DarkOnSurface    // Texte sur ces éléments
)

// --- NOUVELLE PALETTE POUR LE THÈME CLAIR ---
private val LightColorScheme = lightColorScheme(
    primary = BlueAircraftPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = BlueAircraftLight,
    onPrimaryContainer = LightOnBackground,

    secondary = BlueAircraftDark,
    onSecondary = LightOnPrimary,

    background = LightBackground,
    onBackground = LightOnBackground,

    surface = LightSurface,
    onSurface = LightOnSurface,

    surfaceVariant = LightBackground,
    onSurfaceVariant = LightOnBackground
)

@Composable
fun PilotPrepAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // On désactive la couleur dynamique pour imposer notre thème
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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Status Bar : même couleur pour les deux thèmes (couleur du thème clair)
            val statusBarColor = Color(0xFF031A4D).toArgb()
            window.statusBarColor = statusBarColor
            // Force la couleur même si elle était déjà définie
            window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = statusBarColor
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false // Texte de la barre de statut en blanc
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}