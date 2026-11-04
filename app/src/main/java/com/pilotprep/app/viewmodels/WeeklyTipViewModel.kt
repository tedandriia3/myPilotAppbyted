package com.pilotprep.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pilotprep.app.data.model.WeeklyTip
import com.pilotprep.app.data.model.WeeklyTipInfo
import com.pilotprep.app.data.model.toWeeklyTipInfo
import com.pilotprep.app.data.repository.WeeklyTipRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed class WeeklyTipUiState {
    object Loading : WeeklyTipUiState()
    data class Success(val tipInfo: WeeklyTipInfo) : WeeklyTipUiState()
    data class Error(val message: String) : WeeklyTipUiState()
    object Empty : WeeklyTipUiState()
}

sealed class WeeklyTipDetailUiState {
    object Loading : WeeklyTipDetailUiState()
    data class Success(val tip: WeeklyTip) : WeeklyTipDetailUiState()
    data class Error(val message: String) : WeeklyTipDetailUiState()
    object Empty : WeeklyTipDetailUiState()
}

@HiltViewModel
class WeeklyTipViewModel @Inject constructor(
    private val weeklyTipRepository: WeeklyTipRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeeklyTipUiState>(WeeklyTipUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _detailUiState = MutableStateFlow<WeeklyTipDetailUiState>(WeeklyTipDetailUiState.Loading)
    val detailUiState = _detailUiState.asStateFlow()

    private val _lastRefreshDate = MutableStateFlow<String?>(null)
    val lastRefreshDate = _lastRefreshDate.asStateFlow()

    init {
        loadCurrentWeeklyTip()
    }

    fun loadCurrentWeeklyTip(forceRefresh: Boolean = false) {
        val today = LocalDate.now().toString()
        
        // Debug: Afficher les informations de calcul de semaine
        val debugInfo = com.pilotprep.app.data.model.WeekUtils.debugWeekCalculation()
        println("DEBUG WeeklyTipViewModel: $debugInfo")
        
        // Si on ne force pas le rafraîchissement et qu'on a déjà chargé l'astuce d'aujourd'hui
        if (!forceRefresh && _lastRefreshDate.value == today) {
            println("DEBUG: Utilisation du cache pour $today")
            return
        }

        viewModelScope.launch {
            _uiState.value = WeeklyTipUiState.Loading
            try {
                println("DEBUG: Chargement de l'astuce de la semaine...")
                val tip = weeklyTipRepository.getCurrentWeeklyTip()
                if (tip != null) {
                    val tipInfo = tip.toWeeklyTipInfo()
                    println("DEBUG: Astuce chargée avec succès: ${tipInfo.title}")
                    _uiState.value = WeeklyTipUiState.Success(tipInfo)
                    _lastRefreshDate.value = today
                } else {
                    println("DEBUG: Aucune astuce trouvée")
                    _uiState.value = WeeklyTipUiState.Empty
                }
            } catch (e: Exception) {
                println("ERROR WeeklyTipViewModel: ${e.message}")
                _uiState.value = WeeklyTipUiState.Error(e.message ?: "Erreur lors du chargement de l'astuce de la semaine")
            }
        }
    }

    fun refreshWeeklyTip() {
        loadCurrentWeeklyTip(forceRefresh = true)
    }

    fun getWeeklyTipForWeek(weekNumber: Int) {
        viewModelScope.launch {
            _uiState.value = WeeklyTipUiState.Loading
            try {
                val tip = weeklyTipRepository.getWeeklyTipForWeek(weekNumber)
                if (tip != null) {
                    val tipInfo = tip.toWeeklyTipInfo()
                    _uiState.value = WeeklyTipUiState.Success(tipInfo)
                } else {
                    _uiState.value = WeeklyTipUiState.Empty
                }
            } catch (e: Exception) {
                _uiState.value = WeeklyTipUiState.Error(e.message ?: "Erreur lors du chargement de l'astuce")
            }
        }
    }

    fun getWeeklyTipForDate(date: LocalDate) {
        viewModelScope.launch {
            _uiState.value = WeeklyTipUiState.Loading
            try {
                val tip = weeklyTipRepository.getWeeklyTipForDate(date)
                if (tip != null) {
                    val tipInfo = tip.toWeeklyTipInfo()
                    _uiState.value = WeeklyTipUiState.Success(tipInfo)
                } else {
                    _uiState.value = WeeklyTipUiState.Empty
                }
            } catch (e: Exception) {
                _uiState.value = WeeklyTipUiState.Error(e.message ?: "Erreur lors du chargement de l'astuce")
            }
        }
    }

    fun loadTipDetail(tipId: String) {
        viewModelScope.launch {
            _detailUiState.value = WeeklyTipDetailUiState.Loading
            try {
                println("DEBUG: Chargement des détails pour tipId: $tipId")
                
                // Pour l'instant, on utilise l'astuce actuelle
                // Dans une vraie implémentation, on aurait une méthode getTipById
                val tip = weeklyTipRepository.getCurrentWeeklyTip()
                
                if (tip != null) {
                    println("DEBUG: Détails chargés avec succès: ${tip.title}")
                    _detailUiState.value = WeeklyTipDetailUiState.Success(tip)
                } else {
                    println("DEBUG: Aucune astuce trouvée pour les détails")
                    _detailUiState.value = WeeklyTipDetailUiState.Empty
                }
            } catch (e: Exception) {
                println("ERROR WeeklyTipViewModel loadTipDetail: ${e.message}")
                _detailUiState.value = WeeklyTipDetailUiState.Error(e.message ?: "Erreur lors du chargement des détails")
            }
        }
    }

    fun getAllWeeklyTips() {
        viewModelScope.launch {
            try {
                val tips = weeklyTipRepository.getAllWeeklyTips()
                // Ici on pourrait gérer une liste d'astuces si nécessaire
            } catch (e: Exception) {
                // Gérer l'erreur
            }
        }
    }
}

