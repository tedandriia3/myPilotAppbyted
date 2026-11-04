package com.pilotprep.app.ui.screens.search

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// Modèle pour les résultats de recherche
data class SearchResult(
    val id: String,
    val title: String,
    val excerpt: String,
    val content: String,
    val category: SearchCategory,
    val difficultyLevel: DifficultyLevel,
    val mediaType: MediaType,
    val lastUpdated: String,
    val relevanceScore: Float,
    val isFavorite: Boolean = false,
    val tags: List<String> = emptyList(),
    val author: String? = null,
    val source: String? = null
)

// Catégories de recherche
enum class SearchCategory(
    val displayName: String,
    val icon: ImageVector,
    val color: Color
) {
    METEOROLOGY(
        displayName = "Météorologie",
        icon = Icons.Default.Cloud,
        color = Color(0xFF2196F3)
    ),
    NAVIGATION(
        displayName = "Navigation",
        icon = Icons.Default.Navigation,
        color = Color(0xFF4CAF50)
    ),
    REGULATION(
        displayName = "Réglementation",
        icon = Icons.Default.Gavel,
        color = Color(0xFF9C27B0)
    ),
    PROCEDURES(
        displayName = "Procédures",
        icon = Icons.Default.List,
        color = Color(0xFFFF9800)
    ),
    AIRCRAFT_SYSTEMS(
        displayName = "Systèmes Avion",
        icon = Icons.Default.Settings,
        color = Color(0xFF607D8B)
    ),
    HUMAN_FACTORS(
        displayName = "Facteurs Humains",
        icon = Icons.Default.Person,
        color = Color(0xFFE91E63)
    ),
    AERODYNAMICS(
        displayName = "Aérodynamique",
        icon = Icons.Default.Flight,
        color = Color(0xFF795548)
    ),
    EMERGENCY(
        displayName = "Procédures d'Urgence",
        icon = Icons.Default.Warning,
        color = Color(0xFFF44336)
    ),
    THEORY(
        displayName = "Théorie",
        icon = Icons.Default.School,
        color = Color(0xFF3F51B5)
    ),
    EXAM(
        displayName = "Examens",
        icon = Icons.Default.Quiz,
        color = Color(0xFF00BCD4)
    ),
    HERITAGE(
        displayName = "Patrimoine du Ciel",
        icon = Icons.Default.Museum,
        color = Color(0xFF6D4C41)
    ),
    VOICES(
        displayName = "Voix du Ciel",
        icon = Icons.Default.Headphones,
        color = Color(0xFF00897B)
    )
}

// Niveau de difficulté
enum class DifficultyLevel {
    BEGINNER,      // Basique (élève)
    INTERMEDIATE,  // Intermédiaire (licence)
    ADVANCED       // Avancé (pilote expérimenté)
}

// Type de média
enum class MediaType {
    TEXT,   // Document texte
    VIDEO,  // Vidéo
    IMAGE,  // Image/Infographie
    QUIZ,   // Quiz interactif
    AUDIO   // Podcast/Audio
}

// Filtres de recherche
data class SearchFilter(
    val id: String,
    val displayName: String,
    val icon: ImageVector? = null,
    val filterType: FilterType
)

enum class FilterType {
    CATEGORY,
    DIFFICULTY,
    MEDIA_TYPE,
    DATE_RANGE,
    SOURCE
}

// État de l'écran de recherche
data class SearchUiState(
    val currentQuery: String = "",
    val isSearching: Boolean = false,
    val searchResults: List<SearchResult> = emptyList(),
    val suggestedSearches: List<String> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val availableFilters: List<SearchFilter> = emptyList(),
    val selectedFilters: Set<String> = emptySet(),
    val isVoiceSearching: Boolean = false,
    val errorMessage: String? = null,
    val searchHistory: List<String> = emptyList(),
    val favorites: List<String> = emptyList()
)

// Suggestion de recherche sémantique
data class SemanticSuggestion(
    val originalQuery: String,
    val suggestedQueries: List<String>,
    val relatedConcepts: List<String>,
    val synonyms: List<String>
)

// Résultat de recherche vocale
data class VoiceSearchResult(
    val recognizedText: String,
    val confidence: Float,
    val isProcessing: Boolean = false
)

// Configuration de recherche
data class SearchConfig(
    val enableSemanticSearch: Boolean = true,
    val enableVoiceSearch: Boolean = true,
    val enableHistory: Boolean = true,
    val enableFavorites: Boolean = true,
    val maxResults: Int = 50,
    val highlightKeywords: Boolean = true,
    val showRelevanceScore: Boolean = true
)
