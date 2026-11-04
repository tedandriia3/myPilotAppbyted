package com.pilotprep.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pilotprep.app.data.repository.NeedToKnowDataSeeder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NeedToKnowManagementViewModel @Inject constructor(
    private val dataSeeder: NeedToKnowDataSeeder
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(NeedToKnowManagementUiState())
    val uiState: StateFlow<NeedToKnowManagementUiState> = _uiState.asStateFlow()
    
    fun seedSampleData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                dataSeeder.seedNeedToKnowData()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Données d'exemple ajoutées avec succès !"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Erreur lors de l'ajout des données"
                )
            }
        }
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null
        )
    }
}

data class NeedToKnowManagementUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)










