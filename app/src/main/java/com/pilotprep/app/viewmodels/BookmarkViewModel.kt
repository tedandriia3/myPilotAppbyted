package com.pilotprep.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pilotprep.app.ui.screens.bookmarks.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class BookmarkViewModel @Inject constructor(
    // private val bookmarkRepository: BookmarkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookmarkUiState())
    val uiState: StateFlow<BookmarkUiState> = _uiState.asStateFlow()

    init {
        loadBookmarks()
    }

    private fun loadBookmarks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Simuler le chargement depuis la base de données
                delay(1000)

                val mockBookmarks = generateMockBookmarks()
                val filteredBookmarks = filterBookmarks(mockBookmarks)

                _uiState.value = _uiState.value.copy(
                    bookmarks = filteredBookmarks,
                    totalBookmarks = mockBookmarks.size,
                    isLoading = false
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erreur lors du chargement des signets: ${e.message}"
                )
            }
        }
    }

    fun updateFilterCategory(category: BookmarkCategory?) {
        _uiState.value = _uiState.value.copy(filterCategory = category)
        applyFilters()
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }

    private fun applyFilters() {
        val allBookmarks = generateMockBookmarks()
        val filteredBookmarks = filterBookmarks(allBookmarks)
        _uiState.value = _uiState.value.copy(bookmarks = filteredBookmarks)
    }

    private fun filterBookmarks(bookmarks: List<Bookmark>): List<Bookmark> {
        var filtered = bookmarks

        // Filtrer par catégorie
        _uiState.value.filterCategory?.let { category ->
            filtered = filtered.filter { it.category == category }
        }

        // Filtrer par recherche
        if (_uiState.value.searchQuery.isNotEmpty()) {
            val query = _uiState.value.searchQuery.lowercase()
            filtered = filtered.filter {
                it.title.lowercase().contains(query) ||
                        it.description?.lowercase()?.contains(query) == true ||
                        it.module.lowercase().contains(query)
            }
        }

        return filtered.sortedByDescending { it.lastAccessed }
    }

    fun onBookmarkClick(bookmark: Bookmark, navController: androidx.navigation.NavController) {
        // Mettre à jour la date d'accès
        updateLastAccessed(bookmark)

        // Naviguer vers la leçon
        try {
            navController.navigate(bookmark.route)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Erreur de navigation: ${e.message}"
            )
        }
    }

    fun onBookmarkLongClick(bookmark: Bookmark) {
        // Activer le mode sélection et sélectionner le signet
        val updatedSelection = _uiState.value.selectedBookmarks.toMutableSet()
        updatedSelection.add(bookmark.id)

        _uiState.value = _uiState.value.copy(
            selectedBookmarks = updatedSelection,
            isSelectionMode = true
        )
    }

    fun toggleBookmarkSelection(bookmark: Bookmark) {
        val updatedSelection = _uiState.value.selectedBookmarks.toMutableSet()

        if (updatedSelection.contains(bookmark.id)) {
            updatedSelection.remove(bookmark.id)
        } else {
            updatedSelection.add(bookmark.id)
        }

        _uiState.value = _uiState.value.copy(selectedBookmarks = updatedSelection)
    }

    fun toggleSelectionMode() {
        _uiState.value = _uiState.value.copy(
            isSelectionMode = !_uiState.value.isSelectionMode,
            selectedBookmarks = emptySet()
        )
    }

    fun deleteBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            try {
                // Supprimer de la base de données
                // bookmarkRepository.deleteBookmark(bookmark.id)

                // Mettre à jour l'état local
                val updatedBookmarks = _uiState.value.bookmarks.filter { it.id != bookmark.id }
                val updatedSelection = _uiState.value.selectedBookmarks.toMutableSet()
                updatedSelection.remove(bookmark.id)

                _uiState.value = _uiState.value.copy(
                    bookmarks = updatedBookmarks,
                    selectedBookmarks = updatedSelection,
                    totalBookmarks = updatedBookmarks.size
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Erreur lors de la suppression: ${e.message}"
                )
            }
        }
    }

    fun deleteSelectedBookmarks() {
        viewModelScope.launch {
            try {
                val selectedIds = _uiState.value.selectedBookmarks

                // Supprimer de la base de données
                // bookmarkRepository.deleteBookmarks(selectedIds)

                // Mettre à jour l'état local
                val updatedBookmarks = _uiState.value.bookmarks.filter { it.id !in selectedIds }

                _uiState.value = _uiState.value.copy(
                    bookmarks = updatedBookmarks,
                    selectedBookmarks = emptySet(),
                    isSelectionMode = false,
                    totalBookmarks = updatedBookmarks.size
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Erreur lors de la suppression: ${e.message}"
                )
            }
        }
    }

    fun clearAllBookmarks() {
        viewModelScope.launch {
            try {
                // Vider la base de données
                // bookmarkRepository.clearAllBookmarks()

                _uiState.value = _uiState.value.copy(
                    bookmarks = emptyList(),
                    selectedBookmarks = emptySet(),
                    isSelectionMode = false,
                    totalBookmarks = 0
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Erreur lors du nettoyage: ${e.message}"
                )
            }
        }
    }

    fun addBookmark(lessonId: String, title: String, module: String, route: String) {
        viewModelScope.launch {
            try {
                val bookmark = Bookmark(
                    id = "bookmark_${System.currentTimeMillis()}",
                    lessonId = lessonId,
                    title = title,
                    module = module,
                    category = determineCategory(module),
                    route = route,
                    userId = "user123" // À remplacer par l'ID utilisateur réel
                )

                // Sauvegarder dans la base de données
                // bookmarkRepository.saveBookmark(bookmark)

                // Mettre à jour l'état local
                val updatedBookmarks = _uiState.value.bookmarks + bookmark
                _uiState.value = _uiState.value.copy(
                    bookmarks = updatedBookmarks,
                    totalBookmarks = updatedBookmarks.size
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Erreur lors de l'ajout: ${e.message}"
                )
            }
        }
    }

    fun removeBookmark(lessonId: String) {
        viewModelScope.launch {
            try {
                val bookmarkToRemove = _uiState.value.bookmarks.find { it.lessonId == lessonId }
                bookmarkToRemove?.let { bookmark ->
                    deleteBookmark(bookmark)
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Erreur lors de la suppression: ${e.message}"
                )
            }
        }
    }

    fun isBookmarked(lessonId: String): Boolean {
        return _uiState.value.bookmarks.any { it.lessonId == lessonId }
    }

    private fun updateLastAccessed(bookmark: Bookmark) {
        val updatedBookmark = bookmark.copy(lastAccessed = System.currentTimeMillis())
        val updatedBookmarks = _uiState.value.bookmarks.map {
            if (it.id == bookmark.id) updatedBookmark else it
        }

        _uiState.value = _uiState.value.copy(bookmarks = updatedBookmarks)
    }

    private fun determineCategory(module: String): BookmarkCategory {
        return when {
            module.contains("aérodynamique", ignoreCase = true) ||
                    module.contains("aerodynamique", ignoreCase = true) -> BookmarkCategory.AERODYNAMICS
            module.contains("météorologie", ignoreCase = true) ||
                    module.contains("meteorologie", ignoreCase = true) -> BookmarkCategory.METEOROLOGY
            module.contains("navigation", ignoreCase = true) -> BookmarkCategory.NAVIGATION
            module.contains("réglementation", ignoreCase = true) ||
                    module.contains("reglementation", ignoreCase = true) -> BookmarkCategory.REGULATION
            module.contains("facteurs humains", ignoreCase = true) -> BookmarkCategory.HUMAN_FACTORS
            module.contains("systèmes", ignoreCase = true) -> BookmarkCategory.AIRCRAFT_SYSTEMS
            module.contains("urgence", ignoreCase = true) -> BookmarkCategory.EMERGENCY_PROCEDURES
            module.contains("performance", ignoreCase = true) -> BookmarkCategory.PERFORMANCE
            module.contains("communication", ignoreCase = true) -> BookmarkCategory.COMMUNICATIONS
            else -> BookmarkCategory.GENERAL_THEORY
        }
    }

    private fun generateMockBookmarks(): List<Bookmark> {
        val now = System.currentTimeMillis()

        return listOf(
            Bookmark(
                id = "1",
                lessonId = "aerodynamics_lift",
                title = "Forces de Vol - Portance et Traînée",
                description = "Comprendre les forces fondamentales qui permettent le vol",
                module = "Module 2 : Aérodynamique",
                chapter = "Chapitre 1",
                category = BookmarkCategory.AERODYNAMICS,
                route = "lesson_detail/aerodynamics_lift",
                progress = 0.75f,
                lastPosition = 1250,
                createdAt = now - 86400000, // 1 day ago
                lastAccessed = now - 3600000, // 1 hour ago
                userId = "user123"
            ),
            Bookmark(
                id = "2",
                lessonId = "weather_fronts",
                title = "Fronts Météorologiques",
                description = "Identification et caractéristiques des fronts chauds et froids",
                module = "Module 3 : Météorologie",
                chapter = "Chapitre 2",
                category = BookmarkCategory.METEOROLOGY,
                route = "lesson_detail/weather_fronts",
                progress = 0.45f,
                lastPosition = 800,
                createdAt = now - 172800000, // 2 days ago
                lastAccessed = now - 7200000, // 2 hours ago
                userId = "user123"
            ),
            Bookmark(
                id = "3",
                lessonId = "vfr_navigation",
                title = "Navigation VFR - Lecture de Carte",
                description = "Techniques de navigation à vue et utilisation des cartes aéronautiques",
                module = "Module 4 : Navigation",
                chapter = "Chapitre 1",
                category = BookmarkCategory.NAVIGATION,
                route = "lesson_detail/vfr_navigation",
                progress = 0.90f,
                lastPosition = 2000,
                createdAt = now - 259200000, // 3 days ago
                lastAccessed = now - 86400000, // 1 day ago
                userId = "user123"
            ),
            Bookmark(
                id = "4",
                lessonId = "aircraft_stability",
                title = "Stabilité de l'Avion",
                description = "Stabilité longitudinale, latérale et directionnelle",
                module = "Module 2 : Aérodynamique",
                chapter = "Chapitre 3",
                category = BookmarkCategory.AERODYNAMICS,
                route = "lesson_detail/aircraft_stability",
                progress = 0.30f,
                lastPosition = 500,
                createdAt = now - 345600000, // 4 days ago
                lastAccessed = now - 172800000, // 2 days ago
                userId = "user123"
            ),
            Bookmark(
                id = "5",
                lessonId = "emergency_procedures",
                title = "Procédures d'Urgence - Panne Moteur",
                description = "Procédures à suivre en cas de panne moteur en vol",
                module = "Module 6 : Procédures d'Urgence",
                chapter = "Chapitre 1",
                category = BookmarkCategory.EMERGENCY_PROCEDURES,
                route = "lesson_detail/emergency_procedures",
                progress = 0.60f,
                lastPosition = 1000,
                createdAt = now - 432000000, // 5 days ago
                lastAccessed = now - 259200000, // 3 days ago
                userId = "user123"
            ),
            Bookmark(
                id = "6",
                lessonId = "human_factors_stress",
                title = "Gestion du Stress en Cockpit",
                description = "Identification et gestion du stress en situation de vol",
                module = "Module 5 : Facteurs Humains",
                chapter = "Chapitre 2",
                category = BookmarkCategory.HUMAN_FACTORS,
                route = "lesson_detail/human_factors_stress",
                progress = 0.85f,
                lastPosition = 1500,
                createdAt = now - 518400000, // 6 days ago
                lastAccessed = now - 345600000, // 4 days ago
                userId = "user123"
            )
        ).sortedByDescending { it.lastAccessed }
    }
}
