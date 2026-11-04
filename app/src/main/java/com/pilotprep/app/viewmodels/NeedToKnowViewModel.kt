package com.pilotprep.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pilotprep.app.data.model.NeedToKnowContent
import com.pilotprep.app.data.model.NeedToKnowUiState
import com.pilotprep.app.data.repository.NeedToKnowRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NeedToKnowViewModel @Inject constructor(
    private val repository: NeedToKnowRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(NeedToKnowUiState())
    val uiState: StateFlow<NeedToKnowUiState> = _uiState.asStateFlow()
    
    init {
        loadAllContents()
    }
    
    /**
     * Charge tous les contenus
     */
    fun loadAllContents() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val contents = repository.getAllContents()
                _uiState.value = _uiState.value.copy(
                    contents = contents,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Erreur lors du chargement des contenus"
                )
            }
        }
    }
    
    
    /**
     * Récupère un contenu par son ID
     */
    fun getContentById(contentId: String): NeedToKnowContent? {
        return _uiState.value.contents.find { it.id == contentId }
    }
    
    /**
     * Efface l'erreur
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

