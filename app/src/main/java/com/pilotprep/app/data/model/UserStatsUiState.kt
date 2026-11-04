package com.pilotprep.app.data.model

/**
 * État UI pour les statistiques utilisateur
 */
data class UserStatsUiState(
    val isLoading: Boolean = false,
    val userStats: UserStats? = null,
    val error: String? = null
)
