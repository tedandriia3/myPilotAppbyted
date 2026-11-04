package com.pilotprep.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pilotprep.app.data.model.Aircraft
import com.pilotprep.app.data.repository.AircraftRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HeritageUiState {
    object Loading : HeritageUiState()
    data class Success(val aircraftList: List<Aircraft>) : HeritageUiState()
    data class Error(val message: String) : HeritageUiState()
}

@HiltViewModel
class HeritageViewModel @Inject constructor(
    private val repository: AircraftRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HeritageUiState>(HeritageUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _selectedAircraft = MutableStateFlow<Aircraft?>(null)
    val selectedAircraft = _selectedAircraft.asStateFlow()
    
    private val _isLoadingAircraft = MutableStateFlow(false)
    val isLoadingAircraft = _isLoadingAircraft.asStateFlow()

    private var aircraftListCache: List<Aircraft> = emptyList()

    init {
        fetchAircraft()
    }

    // --- MISE À JOUR DE LA FONCTION ---
    fun fetchAircraft(forceRefresh: Boolean = false) {
        // Si on ne force pas le rafraîchissement et que le cache n'est pas vide, on ne fait rien
        if (!forceRefresh && aircraftListCache.isNotEmpty()) {
            _uiState.value = HeritageUiState.Success(aircraftListCache)
            return
        }

        viewModelScope.launch {
            _uiState.value = HeritageUiState.Loading
            try {
                val aircraft = repository.getAllAircraft()
                aircraftListCache = aircraft
                _uiState.value = HeritageUiState.Success(aircraft)
            } catch (e: Exception) {
                _uiState.value = HeritageUiState.Error(e.message ?: "Une erreur est survenue")
            }
        }
    }

    fun findAircraftById(id: String) {
        _selectedAircraft.value = aircraftListCache.find { it.id == id }
    }
    
    /**
     * Charge un avion par son ID depuis Firebase
     */
    fun loadAircraftById(id: String) {
        viewModelScope.launch {
            _isLoadingAircraft.value = true
            try {
                val aircraft = repository.getAircraftById(id)
                _selectedAircraft.value = aircraft
            } catch (e: Exception) {
                _selectedAircraft.value = null
            } finally {
                _isLoadingAircraft.value = false
            }
        }
    }
}