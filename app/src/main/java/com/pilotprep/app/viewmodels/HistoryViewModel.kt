package com.pilotprep.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pilotprep.app.ui.screens.history.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    // private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _selectedFilter = MutableStateFlow(HistoryFilter.ALL)
    val selectedFilter: StateFlow<HistoryFilter> = _selectedFilter.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Simuler le chargement depuis la base de données
                delay(1000)

                val mockHistory = generateMockHistory()
                val filteredHistory = filterHistory(mockHistory, _selectedFilter.value)

                _uiState.value = _uiState.value.copy(
                    entries = filteredHistory,
                    isLoading = false,
                    totalEntries = mockHistory.size
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erreur lors du chargement de l'historique: ${e.message}"
                )
            }
        }
    }

    fun updateFilter(filter: HistoryFilter) {
        _selectedFilter.value = filter
        val filteredHistory = filterHistory(_uiState.value.entries, filter)
        _uiState.value = _uiState.value.copy(entries = filteredHistory)
    }

    fun onEntryClick(entry: HistoryEntry, navController: androidx.navigation.NavController) {
        // Enregistrer la consultation
        recordHistoryEntry(entry)

        // Naviguer vers le contenu
        try {
            navController.navigate(entry.route)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Erreur de navigation: ${e.message}"
            )
        }
    }

    fun onEntryLongClick(entry: HistoryEntry) {
        // Activer le mode sélection et sélectionner l'entrée
        val updatedSelection = _uiState.value.selectedEntries.toMutableSet()
        updatedSelection.add(entry.id)

        _uiState.value = _uiState.value.copy(
            selectedEntries = updatedSelection,
            isSelectionMode = true
        )
    }

    fun toggleEntrySelection(entry: HistoryEntry) {
        val updatedSelection = _uiState.value.selectedEntries.toMutableSet()

        if (updatedSelection.contains(entry.id)) {
            updatedSelection.remove(entry.id)
        } else {
            updatedSelection.add(entry.id)
        }

        _uiState.value = _uiState.value.copy(selectedEntries = updatedSelection)
    }

    fun toggleSelectionMode() {
        _uiState.value = _uiState.value.copy(
            isSelectionMode = !_uiState.value.isSelectionMode,
            selectedEntries = emptySet()
        )
    }

    fun deleteEntry(entry: HistoryEntry) {
        viewModelScope.launch {
            try {
                // Supprimer de la base de données
                // historyRepository.deleteEntry(entry.id)

                // Mettre à jour l'état local
                val updatedEntries = _uiState.value.entries.filter { it.id != entry.id }
                val updatedSelection = _uiState.value.selectedEntries.toMutableSet()
                updatedSelection.remove(entry.id)

                _uiState.value = _uiState.value.copy(
                    entries = updatedEntries,
                    selectedEntries = updatedSelection
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Erreur lors de la suppression: ${e.message}"
                )
            }
        }
    }

    fun deleteSelectedEntries() {
        viewModelScope.launch {
            try {
                val selectedIds = _uiState.value.selectedEntries

                // Supprimer de la base de données
                // historyRepository.deleteEntries(selectedIds)

                // Mettre à jour l'état local
                val updatedEntries = _uiState.value.entries.filter { it.id !in selectedIds }

                _uiState.value = _uiState.value.copy(
                    entries = updatedEntries,
                    selectedEntries = emptySet(),
                    isSelectionMode = false
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Erreur lors de la suppression: ${e.message}"
                )
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            try {
                // Vider la base de données
                // historyRepository.clearAllHistory()

                _uiState.value = _uiState.value.copy(
                    entries = emptyList(),
                    selectedEntries = emptySet(),
                    isSelectionMode = false
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Erreur lors du nettoyage: ${e.message}"
                )
            }
        }
    }

    fun loadMoreEntries() {
        // Implémentation pour la pagination
        // historyRepository.loadMoreEntries()
    }

    fun recordHistoryEntry(entry: HistoryEntry) {
        viewModelScope.launch {
            try {
                // Enregistrer dans la base de données
                // historyRepository.saveEntry(entry)

                // Mettre à jour l'état local si nécessaire

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Erreur lors de l'enregistrement: ${e.message}"
                )
            }
        }
    }

    private fun filterHistory(
        entries: List<HistoryEntry>,
        filter: HistoryFilter
    ): List<HistoryEntry> {
        return when (filter) {
            HistoryFilter.ALL -> entries
            HistoryFilter.TODAY -> entries.filter { isToday(it.timestamp) }
            HistoryFilter.WEEK -> entries.filter { isThisWeek(it.timestamp) }
            HistoryFilter.MONTH -> entries.filter { isThisMonth(it.timestamp) }
            HistoryFilter.LESSONS -> entries.filter { it.contentType == ContentType.LESSON }
            HistoryFilter.QUIZZES -> entries.filter { it.contentType == ContentType.QUIZ || it.contentType == ContentType.EXAM }
            HistoryFilter.DOCUMENTS -> entries.filter { it.contentType == ContentType.DOCUMENT || it.contentType == ContentType.THEORY }
            HistoryFilter.SEARCHES -> entries.filter { it.contentType == ContentType.SEARCH }
        }
    }

    private fun isToday(timestamp: Long): Boolean {
        val today = System.currentTimeMillis()
        val startOfDay = today - (today % 86400000)
        return timestamp >= startOfDay
    }

    private fun isThisWeek(timestamp: Long): Boolean {
        val now = System.currentTimeMillis()
        val weekAgo = now - (7 * 24 * 60 * 60 * 1000)
        return timestamp >= weekAgo
    }

    private fun isThisMonth(timestamp: Long): Boolean {
        val now = System.currentTimeMillis()
        val monthAgo = now - (30 * 24 * 60 * 60 * 1000)
        return timestamp >= monthAgo
    }

    private fun generateMockHistory(): List<HistoryEntry> {
        val now = System.currentTimeMillis()

        return listOf(
            HistoryEntry(
                id = "1",
                title = "Procédures d'approche ILS",
                description = "Guide complet des procédures d'approche aux instruments",
                contentType = ContentType.LESSON,
                route = "lesson_detail/ils_approach",
                timestamp = now - 600000, // 10 minutes ago
                userId = "user123",
                progress = 0.75f,
                lastPosition = 1250
            ),
            HistoryEntry(
                id = "2",
                title = "Météorologie aéronautique",
                description = "Comprendre les phénomènes météorologiques",
                contentType = ContentType.DOCUMENT,
                route = "theory_selection/meteorology",
                timestamp = now - 3600000, // 1 hour ago
                userId = "user123",
                progress = 0.45f
            ),
            HistoryEntry(
                id = "3",
                title = "Quiz: Navigation VFR",
                description = "Test de connaissances sur la navigation à vue",
                contentType = ContentType.QUIZ,
                route = "quiz/vfr/navigation/practice",
                timestamp = now - 7200000, // 2 hours ago
                userId = "user123",
                progress = 0.90f
            ),
            HistoryEntry(
                id = "4",
                title = "Recherche: vent de travers",
                description = "Recherche sur les procédures de décrabage",
                contentType = ContentType.SEARCH,
                route = "search?q=vent+travers",
                timestamp = now - 86400000, // 1 day ago
                userId = "user123"
            ),
            HistoryEntry(
                id = "5",
                title = "Examen PPL - Météorologie",
                description = "Examen blanc pour la licence PPL",
                contentType = ContentType.EXAM,
                route = "quiz/ppl/meteorology/exam",
                timestamp = now - 172800000, // 2 days ago
                userId = "user123",
                progress = 0.60f
            ),
            HistoryEntry(
                id = "6",
                title = "Patrimoine aéronautique français",
                description = "Histoire de l'aviation française",
                contentType = ContentType.HERITAGE,
                route = "heritage",
                timestamp = now - 259200000, // 3 days ago
                userId = "user123",
                progress = 0.30f
            ),
            HistoryEntry(
                id = "7",
                title = "Podcast: Facteurs humains",
                description = "Épisode sur les facteurs humains en aviation",
                contentType = ContentType.AUDIO,
                route = "podcast_player/human_factors",
                timestamp = now - 345600000, // 4 days ago
                userId = "user123",
                progress = 0.85f
            ),
            HistoryEntry(
                id = "8",
                title = "Paramètres de l'application",
                description = "Configuration des préférences utilisateur",
                contentType = ContentType.SETTINGS,
                route = "settings",
                timestamp = now - 432000000, // 5 days ago
                userId = "user123"
            )
        ).sortedByDescending { it.timestamp }
    }
}
