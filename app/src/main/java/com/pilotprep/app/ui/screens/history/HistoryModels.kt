package com.pilotprep.app.ui.screens.history

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.*

// Modèle pour une entrée d'historique
data class HistoryEntry(
    val id: String,
    val title: String,
    val description: String? = null,
    val contentType: ContentType,
    val route: String,
    val timestamp: Long,
    val userId: String,
    val metadata: Map<String, Any> = emptyMap(),
    val thumbnailUrl: String? = null,
    val progress: Float = 0f, // Pourcentage de progression (0.0 à 1.0)
    val lastPosition: Int = 0 // Position de défilement ou question actuelle
)

// Types de contenu pour l'historique
enum class ContentType(
    val displayName: String,
    val icon: ImageVector,
    val color: Color,
    val emoji: String
) {
    LESSON(
        displayName = "Leçon",
        icon = Icons.Default.School,
        color = Color(0xFF2196F3),
        emoji = "📖"
    ),
    DOCUMENT(
        displayName = "Document",
        icon = Icons.Default.Article,
        color = Color(0xFF4CAF50),
        emoji = "📄"
    ),
    QUIZ(
        displayName = "Quiz",
        icon = Icons.Default.Quiz,
        color = Color(0xFFFF9800),
        emoji = "❓"
    ),
    EXAM(
        displayName = "Examen",
        icon = Icons.Default.Assignment,
        color = Color(0xFFF44336),
        emoji = "📝"
    ),
    SEARCH(
        displayName = "Recherche",
        icon = Icons.Default.Search,
        color = Color(0xFF9C27B0),
        emoji = "🔍"
    ),
    SIMULATOR(
        displayName = "Simulateur",
        icon = Icons.Default.Flight,
        color = Color(0xFF795548),
        emoji = "✈️"
    ),
    VIDEO(
        displayName = "Vidéo",
        icon = Icons.Default.PlayArrow,
        color = Color(0xFFE91E63),
        emoji = "🎥"
    ),
    AUDIO(
        displayName = "Audio",
        icon = Icons.Default.Headphones,
        color = Color(0xFF00BCD4),
        emoji = "🎧"
    ),
    THEORY(
        displayName = "Théorie",
        icon = Icons.Default.MenuBook,
        color = Color(0xFF3F51B5),
        emoji = "📚"
    ),
    HERITAGE(
        displayName = "Patrimoine",
        icon = Icons.Default.Museum,
        color = Color(0xFF607D8B),
        emoji = "🏛️"
    ),
    SETTINGS(
        displayName = "Paramètres",
        icon = Icons.Default.Settings,
        color = Color(0xFF9E9E9E),
        emoji = "⚙️"
    ),
    OTHER(
        displayName = "Autre",
        icon = Icons.Default.MoreHoriz,
        color = Color(0xFF757575),
        emoji = "📋"
    )
}

// État de l'écran d'historique
data class HistoryUiState(
    val entries: List<HistoryEntry> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedEntries: Set<String> = emptySet(),
    val isSelectionMode: Boolean = false,
    val totalEntries: Int = 0,
    val hasMoreEntries: Boolean = false
)

// Filtres pour l'historique
enum class HistoryFilter(
    val displayName: String,
    val icon: ImageVector
) {
    ALL(
        displayName = "Tout",
        icon = Icons.Default.List
    ),
    TODAY(
        displayName = "Aujourd'hui",
        icon = Icons.Default.Today
    ),
    WEEK(
        displayName = "Cette semaine",
        icon = Icons.Default.DateRange
    ),
    MONTH(
        displayName = "Ce mois",
        icon = Icons.Default.CalendarMonth
    ),
    LESSONS(
        displayName = "Leçons",
        icon = Icons.Default.School
    ),
    QUIZZES(
        displayName = "Quiz",
        icon = Icons.Default.Quiz
    ),
    DOCUMENTS(
        displayName = "Documents",
        icon = Icons.Default.Article
    ),
    SEARCHES(
        displayName = "Recherches",
        icon = Icons.Default.Search
    )
}

// Configuration de l'historique
data class HistoryConfig(
    val maxEntries: Int = 100,
    val enableAutoTracking: Boolean = true,
    val enableProgressTracking: Boolean = true,
    val enablePositionTracking: Boolean = true,
    val autoCleanupDays: Int = 90 // Nettoyage automatique après 90 jours
)

// Statistiques de l'historique
data class HistoryStats(
    val totalEntries: Int,
    val entriesByType: Map<ContentType, Int>,
    val mostAccessedContent: List<HistoryEntry>,
    val totalStudyTime: Long, // en minutes
    val averageSessionDuration: Long, // en minutes
    val lastActivityDate: Long
)

// Extension pour formater la date relative
fun Long.toRelativeTimeString(): String {
    val now = System.currentTimeMillis()
    val diff = now - this

    return when {
        diff < 60_000 -> "À l'instant"
        diff < 3600_000 -> "${diff / 60_000} min"
        diff < 86400_000 -> "${diff / 3600_000}h"
        diff < 604800_000 -> "${diff / 86400_000} jour${if (diff / 86400_000 > 1) "s" else ""}"
        diff < 2592000_000 -> "${diff / 604800_000} semaine${if (diff / 604800_000 > 1) "s" else ""}"
        else -> "${diff / 2592000_000} mois"
    }
}

// Extension pour formater la date complète
fun Long.toFullDateString(): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this

    return String.format(
        "%02d/%02d/%d à %02d:%02d",
        calendar.get(Calendar.DAY_OF_MONTH),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE)
    )
}
