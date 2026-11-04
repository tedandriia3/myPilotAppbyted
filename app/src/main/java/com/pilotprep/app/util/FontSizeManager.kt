package com.pilotprep.app.util

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

object FontSizeManager {
    private const val PREFS_NAME = "font_size_prefs"
    private const val KEY_FONT_SCALE = "font_scale"
    private const val DEFAULT_FONT_SCALE = 1.0f
    
    fun getFontScale(context: Context): Float {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getFloat(KEY_FONT_SCALE, DEFAULT_FONT_SCALE)
    }
    
    fun setFontScale(context: Context, scale: Float) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_FONT_SCALE, scale).apply()
    }
    
    fun resetFontScale(context: Context) {
        setFontScale(context, DEFAULT_FONT_SCALE)
    }
    
    @Composable
    fun rememberFontScale(): Float {
        val context = LocalContext.current
        return remember { getFontScale(context) }
    }
}

// Extension pour créer des styles de texte avec la taille dynamique
@Composable
fun androidx.compose.ui.text.TextStyle.withFontScale(
    baseStyle: androidx.compose.ui.text.TextStyle = androidx.compose.ui.text.TextStyle.Default,
    fontScale: Float = FontSizeManager.rememberFontScale()
): androidx.compose.ui.text.TextStyle {
    return baseStyle.copy(
        fontSize = baseStyle.fontSize * fontScale,
        lineHeight = baseStyle.lineHeight * fontScale
    )
}

