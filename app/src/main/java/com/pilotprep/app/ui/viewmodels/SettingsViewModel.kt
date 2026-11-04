package com.pilotprep.app.viewmodels

import androidx.lifecycle.ViewModel
import com.pilotprep.app.data.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

enum class ThemeOption { SYSTEM, LIGHT, DARK }

data class SettingsUiState(
    val selectedTheme: ThemeOption = ThemeOption.SYSTEM,
    val isDataSaverEnabled: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appPrefs: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // On charge le thème sauvegardé au démarrage
        _uiState.update { it.copy(selectedTheme = appPrefs.getTheme()) }
    }

    fun onThemeSelected(themeOption: ThemeOption) {
        appPrefs.saveTheme(themeOption) // On sauvegarde le choix
        _uiState.update { it.copy(selectedTheme = themeOption) }
    }

    fun onDataSaverToggled(isEnabled: Boolean) {
        _uiState.update { it.copy(isDataSaverEnabled = isEnabled) }
    }
}