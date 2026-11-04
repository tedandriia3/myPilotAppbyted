package com.pilotprep.app.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.pilotprep.app.viewmodels.ThemeOption
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFS_NAME = "app_main_prefs"
private const val KEY_THEME = "app_theme"
private const val KEY_HIGH_SCORE_PREFIX = "high_score_"

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // --- Logique pour le Thème ---
    fun saveTheme(theme: ThemeOption) {
        prefs.edit().putString(KEY_THEME, theme.name).apply()
    }

    fun getTheme(): ThemeOption {
        val themeName = prefs.getString(KEY_THEME, ThemeOption.SYSTEM.name)
        return ThemeOption.valueOf(themeName ?: ThemeOption.SYSTEM.name)
    }

    // --- Logique pour les Meilleurs Scores ---
    fun saveHighScore(examId: String, score: Int) {
        val currentHighScore = getHighScore(examId)
        if (score > currentHighScore) {
            prefs.edit().putInt("$KEY_HIGH_SCORE_PREFIX$examId", score).apply()
        }
    }

    fun getHighScore(examId: String): Int {
        return prefs.getInt("$KEY_HIGH_SCORE_PREFIX$examId", 0)
    }
}