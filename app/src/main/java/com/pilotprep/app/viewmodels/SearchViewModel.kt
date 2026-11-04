package com.pilotprep.app.viewmodels

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pilotprep.app.ui.screens.search.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: com.pilotprep.app.data.repository.SearchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val allowedSections = setOf(
        SearchCategory.METEOROLOGY,
        SearchCategory.NAVIGATION,
        SearchCategory.REGULATION,
        SearchCategory.PROCEDURES,
        SearchCategory.AIRCRAFT_SYSTEMS,
        SearchCategory.HUMAN_FACTORS,
        SearchCategory.AERODYNAMICS,
        SearchCategory.EMERGENCY,
        SearchCategory.THEORY,
        SearchCategory.EXAM,
        SearchCategory.HERITAGE,
        SearchCategory.VOICES
    )

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // Charger l'historique récent (simulé)
            val recentSearches = listOf(
                "Approche non-précision",
                "Météorologie",
                "Procédures d'urgence"
            )

            // Charger les filtres disponibles
            val availableFilters = createAvailableFilters()

            _uiState.value = _uiState.value.copy(
                suggestedSearches = emptyList(),
                recentSearches = recentSearches,
                availableFilters = availableFilters
            )
        }
    }

    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(currentQuery = query)
    }

    fun performSearch() {
        val query = _uiState.value.currentQuery
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, errorMessage = null)

            try {
                // Rechercher dans Firestore (lessons, podcasts, aircraft)
                val results = searchRepository.searchInAllCollections(query)
                    .filter { allowedSections.contains(it.category) }
                val filteredResults = applyFilters(results, _uiState.value.selectedFilters)

                // Ajouter à l'historique
                val updatedHistory = (_uiState.value.recentSearches + query)
                    .distinct()
                    .take(10)

                _uiState.value = _uiState.value.copy(
                    searchResults = filteredResults,
                    isSearching = false,
                    recentSearches = updatedHistory
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    errorMessage = "Erreur lors de la recherche: ${e.message}"
                )
            }
        }
    }

    fun performSuggestedSearch(suggestion: String) {
        _uiState.value = _uiState.value.copy(currentQuery = suggestion)
        performSearch()
    }

    fun performRecentSearch(query: String) {
        _uiState.value = _uiState.value.copy(currentQuery = query)
        performSearch()
    }

    fun updateFilters(filterId: String, isSelected: Boolean) {
        val currentFilters = _uiState.value.selectedFilters.toMutableSet()

        if (isSelected) {
            currentFilters.add(filterId)
        } else {
            currentFilters.remove(filterId)
        }

        _uiState.value = _uiState.value.copy(selectedFilters = currentFilters)

        // Re-lancer la recherche avec les nouveaux filtres
        if (_uiState.value.currentQuery.isNotEmpty()) {
            performSearch()
        }
    }

    fun toggleVoiceSearch() {
        val currentState = _uiState.value.isVoiceSearching
        _uiState.value = _uiState.value.copy(isVoiceSearching = !currentState)

        if (!currentState) {
            // Démarrer la reconnaissance vocale
            startVoiceRecognition()
        }
    }

    private fun startVoiceRecognition() {
        viewModelScope.launch {
            // Simuler la reconnaissance vocale
            delay(2000)

            // Simuler un résultat de reconnaissance
            val recognizedText = "Procédure d'approche ILS"

            _uiState.value = _uiState.value.copy(
                isVoiceSearching = false,
                currentQuery = recognizedText
            )

            // Lancer automatiquement la recherche
            performSearch()
        }
    }

    fun clearSearchHistory() {
        _uiState.value = _uiState.value.copy(recentSearches = emptyList())
    }

    fun onResultClick(result: SearchResult) {
        // Logique pour naviguer vers le contenu
        // ou ajouter aux favoris
    }


    private fun createAvailableFilters(): List<SearchFilter> {
        return listOf(
            SearchFilter(
                id = "domain_theory",
                displayName = "Théorie du vol",
                icon = Icons.Default.School,
                filterType = FilterType.CATEGORY
            ),
            SearchFilter(
                id = "domain_heritage",
                displayName = "Patrimoine du Ciel",
                icon = Icons.Default.Museum,
                filterType = FilterType.CATEGORY
            ),
            SearchFilter(
                id = "domain_voices",
                displayName = "Voix du Ciel",
                icon = Icons.Default.Headphones,
                filterType = FilterType.CATEGORY
            ),
            SearchFilter(
                id = "difficulty_beginner",
                displayName = "Niveau Élève",
                filterType = FilterType.DIFFICULTY
            ),
            SearchFilter(
                id = "difficulty_intermediate",
                displayName = "Niveau Licence",
                filterType = FilterType.DIFFICULTY
            ),
            SearchFilter(
                id = "difficulty_advanced",
                displayName = "Niveau Instructeur",
                filterType = FilterType.DIFFICULTY
            ),
            SearchFilter(
                id = "media_text",
                displayName = "Contenu Théorique",
                icon = Icons.Default.Article,
                filterType = FilterType.MEDIA_TYPE
            ),
            SearchFilter(
                id = "media_audio",
                displayName = "Contenu Audio",
                icon = Icons.Default.Headphones,
                filterType = FilterType.MEDIA_TYPE
            )
        )
    }

    private fun applyFilters(
        results: List<SearchResult>,
        activeFilters: Set<String>
    ): List<SearchResult> {
        if (activeFilters.isEmpty()) return results

        var filtered = results

        val domainFilters = activeFilters.filter { it.startsWith("domain_") }.toSet()
        if (domainFilters.isNotEmpty()) {
            val theoryCategories = setOf(
                SearchCategory.METEOROLOGY,
                SearchCategory.NAVIGATION,
                SearchCategory.REGULATION,
                SearchCategory.PROCEDURES,
                SearchCategory.AIRCRAFT_SYSTEMS,
                SearchCategory.HUMAN_FACTORS,
                SearchCategory.AERODYNAMICS,
                SearchCategory.EMERGENCY,
                SearchCategory.THEORY,
                SearchCategory.EXAM
            )
            filtered = filtered.filter { result ->
                when {
                    domainFilters.contains("domain_theory") && theoryCategories.contains(result.category) -> true
                    domainFilters.contains("domain_heritage") && result.category == SearchCategory.HERITAGE -> true
                    domainFilters.contains("domain_voices") && result.category == SearchCategory.VOICES -> true
                    else -> domainFilters.isEmpty()
                }
            }
        }

        val difficultyFilters = activeFilters.filter { it.startsWith("difficulty_") }.toSet()
        if (difficultyFilters.isNotEmpty()) {
            val allowedDifficulties = difficultyFilters.mapNotNull {
                when (it) {
                    "difficulty_beginner" -> DifficultyLevel.BEGINNER
                    "difficulty_intermediate" -> DifficultyLevel.INTERMEDIATE
                    "difficulty_advanced" -> DifficultyLevel.ADVANCED
                    else -> null
                }
            }.toSet()
            filtered = filtered.filter { allowedDifficulties.contains(it.difficultyLevel) }
        }

        val mediaFilters = activeFilters.filter { it.startsWith("media_") }.toSet()
        if (mediaFilters.isNotEmpty()) {
            val allowedMedia = mediaFilters.mapNotNull {
                when (it) {
                    "media_text" -> MediaType.TEXT
                    "media_audio" -> MediaType.AUDIO
                    else -> null
                }
            }.toSet()
            filtered = filtered.filter { allowedMedia.contains(it.mediaType) }
        }

        return filtered
    }
}
