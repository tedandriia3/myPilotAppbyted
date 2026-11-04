package com.pilotprep.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pilotprep.app.data.model.Aircraft
import com.pilotprep.app.data.model.ImageOfDayInfo
import com.pilotprep.app.data.model.toImageOfDayInfo
import com.pilotprep.app.data.repository.ImageOfDayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed class ImageOfDayUiState {
    object Loading : ImageOfDayUiState()
    data class Success(val imageInfo: ImageOfDayInfo) : ImageOfDayUiState()
    data class Error(val message: String) : ImageOfDayUiState()
    object Empty : ImageOfDayUiState()
}

@HiltViewModel
class ImageOfDayViewModel @Inject constructor(
    private val imageOfDayRepository: ImageOfDayRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ImageOfDayUiState>(ImageOfDayUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _lastRefreshDate = MutableStateFlow<String?>(null)
    val lastRefreshDate = _lastRefreshDate.asStateFlow()

    init {
        loadImageOfDay()
    }

    fun loadImageOfDay(forceRefresh: Boolean = false) {
        val today = LocalDate.now().toString()
        
        // Si on ne force pas le rafraîchissement et qu'on a déjà chargé l'image d'aujourd'hui
        if (!forceRefresh && _lastRefreshDate.value == today) {
            return
        }

        viewModelScope.launch {
            _uiState.value = ImageOfDayUiState.Loading
            try {
                val aircraft = imageOfDayRepository.getCurrentImageOfDay()
                if (aircraft != null) {
                    val imageInfo = aircraft.toImageOfDayInfo()
                    _uiState.value = ImageOfDayUiState.Success(imageInfo)
                    _lastRefreshDate.value = today
                } else {
                    _uiState.value = ImageOfDayUiState.Empty
                }
            } catch (e: Exception) {
                _uiState.value = ImageOfDayUiState.Error(e.message ?: "Erreur lors du chargement de l'image du jour")
            }
        }
    }

    fun refreshImageOfDay() {
        loadImageOfDay(forceRefresh = true)
    }

    fun getImageOfDayForDate(date: LocalDate) {
        viewModelScope.launch {
            _uiState.value = ImageOfDayUiState.Loading
            try {
                val aircraft = imageOfDayRepository.getImageOfDayForDate(date)
                if (aircraft != null) {
                    val imageInfo = aircraft.toImageOfDayInfo()
                    _uiState.value = ImageOfDayUiState.Success(imageInfo)
                } else {
                    _uiState.value = ImageOfDayUiState.Empty
                }
            } catch (e: Exception) {
                _uiState.value = ImageOfDayUiState.Error(e.message ?: "Erreur lors du chargement de l'image")
            }
        }
    }
}
