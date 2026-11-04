package com.pilotprep.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pilotprep.app.data.model.NeedToKnowContent
import com.pilotprep.app.data.repository.NeedToKnowRepository
import com.pilotprep.app.services.ContentManagementService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContentManagementUiState(
    val contents: List<NeedToKnowContent> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class ContentManagementViewModel @Inject constructor(
    private val contentManagementService: ContentManagementService,
    private val needToKnowRepository: NeedToKnowRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ContentManagementUiState())
    val uiState: StateFlow<ContentManagementUiState> = _uiState.asStateFlow()
    
    init {
        loadContents()
    }
    
    fun loadContents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val contents = needToKnowRepository.getAllContents()
                _uiState.update { 
                    it.copy(
                        contents = contents,
                        isLoading = false,
                        error = null
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Erreur lors du chargement"
                    ) 
                }
            }
        }
    }
    
    fun addSimpleContent(title: String, text: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                val contentId = contentManagementService.addSimpleContent(title, text)
                if (contentId != null) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            successMessage = "Contenu ajouté avec succès !"
                        ) 
                    }
                    // Recharger les contenus
                    loadContents()
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Erreur lors de l'ajout du contenu"
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Erreur lors de l'ajout"
                    ) 
                }
            }
        }
    }
    
    fun addContentWithBlocks(title: String, blocks: List<Map<String, Any>>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                val contentId = contentManagementService.addContentWithBlocks(title, blocks)
                if (contentId != null) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            successMessage = "Contenu ajouté avec succès !"
                        ) 
                    }
                    // Recharger les contenus
                    loadContents()
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Erreur lors de l'ajout du contenu"
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Erreur lors de l'ajout"
                    ) 
                }
            }
        }
    }
    
    fun addTestData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                val addedIds = contentManagementService.addTestData()
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        successMessage = "${addedIds.size} contenus de test ajoutés !"
                    ) 
                }
                // Recharger les contenus
                loadContents()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Erreur lors de l'ajout des données de test"
                    ) 
                }
            }
        }
    }
    
    fun deleteContent(contentId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                val success = contentManagementService.deleteContent(contentId)
                if (success) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            successMessage = "Contenu supprimé avec succès !"
                        ) 
                    }
                    // Recharger les contenus
                    loadContents()
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Erreur lors de la suppression"
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Erreur lors de la suppression"
                    ) 
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
}
