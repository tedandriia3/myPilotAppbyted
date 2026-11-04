package com.pilotprep.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pilotprep.app.data.model.*
import com.pilotprep.app.services.UserStatsService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgressDashboardViewModel @Inject constructor(
    private val userStatsService: UserStatsService
) : ViewModel() {

    val uiState: StateFlow<UserStatsUiState> = userStatsService.uiState
    val subjectStats: StateFlow<List<SubjectStats>> = userStatsService.subjectStats
    val weeklyProgress: StateFlow<List<WeeklyProgress>> = userStatsService.weeklyProgress
    val userGoals: StateFlow<List<UserGoal>> = userStatsService.userGoals
    val achievements: StateFlow<List<UserAchievement>> = userStatsService.achievements

    fun loadUserStats(userId: String) {
        viewModelScope.launch {
            userStatsService.loadUserStats(userId)
        }
    }

    fun refreshStats(userId: String) {
        viewModelScope.launch {
            userStatsService.refreshStats(userId)
        }
    }
}
