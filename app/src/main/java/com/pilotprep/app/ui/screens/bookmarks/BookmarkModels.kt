package com.pilotprep.app.ui.screens.bookmarks

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.*

// Modèle pour un signet
data class Bookmark(
    val id: String,
    val lessonId: String,
    val title: String,
    val description: String? = null,
    val module: String,
    val chapter: String? = null,
    val category: BookmarkCategory,
    val route: String,
    val thumbnailUrl: String? = null,
    val progress: Float = 0f, // Progression dans la leçon (0.0 à 1.0)
    val lastPosition: Int = 0, // Position de défilement ou question actuelle
    val createdAt: Long = System.currentTimeMillis(),
    val lastAccessed: Long = System.currentTimeMillis(),
    val userId: String,
    val tags: List<String> = emptyList()
)

// Catégories de signets (pour l'instant limité à la Théorie du Vol)
enum class BookmarkCategory(
    val displayName: String,
    val icon: ImageVector,
    val color: Color,
    val description: String
) {
    AERODYNAMICS(
        displayName = "Aérodynamique",
        icon = Icons.Default.Flight,
        color = Color(0xFF2196F3),
        description = "Forces de vol, portance, traînée, stabilité"
    ),
    METEOROLOGY(
        displayName = "Météorologie",
        icon = Icons.Default.Cloud,
        color = Color(0xFF4CAF50),
        description = "Phénomènes météorologiques, prévisions"
    ),
    NAVIGATION(
        displayName = "Navigation",
        icon = Icons.Default.Navigation,
        color = Color(0xFF9C27B0),
        description = "Navigation VFR, IFR, instruments"
    ),
    REGULATION(
        displayName = "Réglementation",
        icon = Icons.Default.Gavel,
        color = Color(0xFFF44336),
        description = "Règles de l'air, procédures"
    ),
    HUMAN_FACTORS(
        displayName = "Facteurs Humains",
        icon = Icons.Default.Person,
        color = Color(0xFFE91E63),
        description = "Performance humaine, gestion du stress"
    ),
    AIRCRAFT_SYSTEMS(
        displayName = "Systèmes Avion",
        icon = Icons.Default.Settings,
        color = Color(0xFF607D8B),
        description = "Moteur, hydraulique, électrique"
    ),
    EMERGENCY_PROCEDURES(
        displayName = "Procédures d'Urgence",
        icon = Icons.Default.Warning,
        color = Color(0xFFFF9800),
        description = "Procédures d'urgence, gestion des pannes"
    ),
    PERFORMANCE(
        displayName = "Performance",
        icon = Icons.Default.Speed,
        color = Color(0xFF00BCD4),
        description = "Calculs de performance, masse et centrage"
    ),
    COMMUNICATIONS(
        displayName = "Communications",
        icon = Icons.Default.Call,
        color = Color(0xFF795548),
        description = "Phraséologie, communications radio"
    ),
    GENERAL_THEORY(
        displayName = "Théorie Générale",
        icon = Icons.Default.MenuBook,
        color = Color(0xFF3F51B5),
        description = "Concepts généraux de l'aviation"
    )
}

// État de l'écran des signets
data class BookmarkUiState(
    val bookmarks: List<Bookmark> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedBookmarks: Set<String> = emptySet(),
    val isSelectionMode: Boolean = false,
    val filterCategory: BookmarkCategory? = null,
    val searchQuery: String = "",
    val totalBookmarks: Int = 0,
    val recentlyAccessed: List<Bookmark> = emptyList()
)

// État du bouton signet dans la top bar
data class BookmarkButtonState(
    val isBookmarked: Boolean = false,
    val isLoading: Boolean = false,
    val canBookmark: Boolean = true,
    val bookmarkId: String? = null
)

// Filtres pour les signets
enum class BookmarkFilter(
    val displayName: String,
    val icon: ImageVector
) {
    ALL(
        displayName = "Tous",
        icon = Icons.Default.List
    ),
    RECENT(
        displayName = "Récents",
        icon = Icons.Default.Schedule
    ),
    MOST_ACCESSED(
        displayName = "Plus consultés",
        icon = Icons.Default.TrendingUp
    ),
    IN_PROGRESS(
        displayName = "En cours",
        icon = Icons.Default.PlayArrow
    ),
    COMPLETED(
        displayName = "Terminés",
        icon = Icons.Default.CheckCircle
    )
}

// Statistiques des signets
data class BookmarkStats(
    val totalBookmarks: Int,
    val bookmarksByCategory: Map<BookmarkCategory, Int>,
    val mostBookmarkedModule: String,
    val averageProgress: Float,
    val totalStudyTime: Long, // en minutes
    val lastBookmarkDate: Long
)

// Extension pour formater la date de création
fun Long.toBookmarkDateString(): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this

    val now = Calendar.getInstance()
    val diff = now.timeInMillis - this

    return when {
        diff < 3600_000 -> "Il y a ${diff / 60_000} min"
        diff < 86400_000 -> "Il y a ${diff / 3600_000}h"
        diff < 604800_000 -> "Il y a ${diff / 86400_000} jour${if (diff / 86400_000 > 1) "s" else ""}"
        else -> "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}"
    }
}

// Extension pour formater la date d'accès
fun Long.toLastAccessedString(): String {
    val diff = System.currentTimeMillis() - this

    return when {
        diff < 3600_000 -> "Il y a ${diff / 60_000} min"
        diff < 86400_000 -> "Il y a ${diff / 3600_000}h"
        diff < 604800_000 -> "Il y a ${diff / 86400_000} jour${if (diff / 86400_000 > 1) "s" else ""}"
        else -> "Il y a plus d'une semaine"
    }
}
