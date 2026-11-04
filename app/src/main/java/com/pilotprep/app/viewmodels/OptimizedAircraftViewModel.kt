package com.pilotprep.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pilotprep.app.data.model.Aircraft
import com.pilotprep.app.data.repository.OptimizedAircraftRepository
import com.pilotprep.app.util.MemoryListener
import com.pilotprep.app.util.MemoryLevel
import com.pilotprep.app.util.MemoryManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel optimisé pour les avions avec gestion intelligente du cache et de la mémoire
 */
@HiltViewModel
class OptimizedAircraftViewModel @Inject constructor(
    private val aircraftRepository: OptimizedAircraftRepository,
    private val memoryManager: MemoryManager,
) : ViewModel(), MemoryListener {

    private val _uiState = MutableStateFlow(AircraftUiState())
    val uiState: StateFlow<AircraftUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    init {
        // S'abonner aux changements de mémoire
        memoryManager.addMemoryListener(this)
    }

    /**
     * Charge tous les avions avec cache intelligent
     */
    fun loadAllAircraft(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val aircraft = aircraftRepository.getAllAircraft(forceRefresh)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    aircraft = aircraft,
                    filteredAircraft = aircraft
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Erreur lors du chargement des avions"
                )
            }
        }
    }

    /**
     * Recherche d'avions
     */
    fun searchAircraft(query: String) {
        _searchQuery.value = query
        
        val allAircraft = _uiState.value.aircraft
        val filteredAircraft = if (query.isBlank()) {
            allAircraft
        } else {
            allAircraft.filter { aircraft ->
                aircraft.name.lowercase().contains(query.lowercase()) ||
                aircraft.description.lowercase().contains(query.lowercase()) ||
                aircraft.engine.lowercase().contains(query.lowercase()) ||
                aircraft.year.lowercase().contains(query.lowercase()) ||
                aircraft.category.lowercase().contains(query.lowercase())
            }
        }
        
        _uiState.value = _uiState.value.copy(
            filteredAircraft = filteredAircraft
        )
    }

    /**
     * Filtre les avions par catégorie (filtrage côté client)
     */
    fun filterByCategory(category: String) {
        _selectedCategory.value = category
        
        val allAircraft = _uiState.value.aircraft
        val filteredAircraft = if (category.isBlank()) {
            allAircraft
        } else {
            allAircraft.filter { aircraft ->
                aircraft.category.lowercase().contains(category.lowercase())
            }
        }
        
        _uiState.value = _uiState.value.copy(filteredAircraft = filteredAircraft)
    }

    /**
     * Récupère un avion par ID
     */
    fun getAircraftById(id: String, onResult: (Aircraft?) -> Unit) {
        viewModelScope.launch {
            try {
                val aircraft = aircraftRepository.getAircraftById(id)
                onResult(aircraft)
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }

    /**
     * Rafraîchit les données
     */
    fun refresh() {
        loadAllAircraft(forceRefresh = true)
    }

    /**
     * Nettoie le cache
     */
    fun clearCache() {
        viewModelScope.launch {
            aircraftRepository.invalidateAllAircraftCache()
        }
    }

    /**
     * Gère les changements de niveau de mémoire
     */
    override fun onMemoryLevelChanged(level: MemoryLevel, usage: Float) {
        when (level) {
            MemoryLevel.LOW -> {
                // Réduire la taille de la liste affichée
                val currentAircraft = _uiState.value.aircraft
                if (currentAircraft.size > 20) {
                    _uiState.value = _uiState.value.copy(
                        aircraft = currentAircraft.take(20),
                        filteredAircraft = _uiState.value.filteredAircraft.take(20)
                    )
                }
            }
            MemoryLevel.CRITICAL -> {
                // Nettoyer le cache et réduire drastiquement la liste
                clearCache()
                val currentAircraft = _uiState.value.aircraft
                _uiState.value = _uiState.value.copy(
                    aircraft = currentAircraft.take(10),
                    filteredAircraft = _uiState.value.filteredAircraft.take(10)
                )
            }
            MemoryLevel.NORMAL -> {
                // Recharger les données complètes si nécessaire
                if (_uiState.value.aircraft.size < 50) {
                    loadAllAircraft()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        memoryManager.removeMemoryListener(this)
    }
}

/**
 * État de l'UI pour les avions
 */
data class AircraftUiState(
    val aircraft: List<Aircraft> = emptyList(),
    val filteredAircraft: List<Aircraft> = emptyList(),
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val error: String? = null,
    val memoryLevel: MemoryLevel = MemoryLevel.NORMAL
)
