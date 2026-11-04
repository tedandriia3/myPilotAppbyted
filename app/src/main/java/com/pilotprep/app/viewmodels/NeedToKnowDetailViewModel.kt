package com.pilotprep.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pilotprep.app.data.model.NeedToKnowContent
import com.pilotprep.app.data.repository.NeedToKnowRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NeedToKnowDetailViewModel @Inject constructor(
    private val repository: NeedToKnowRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(NeedToKnowDetailUiState())
    val uiState: StateFlow<NeedToKnowDetailUiState> = _uiState.asStateFlow()
    
    fun loadContent(contentId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val content = repository.getContentById(contentId)
                _uiState.value = _uiState.value.copy(
                    content = content,
                    isLoading = false,
                    error = if (content == null) "Contenu non trouvé" else null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Erreur lors du chargement du contenu"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class NeedToKnowDetailUiState(
    val content: NeedToKnowContent? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)










