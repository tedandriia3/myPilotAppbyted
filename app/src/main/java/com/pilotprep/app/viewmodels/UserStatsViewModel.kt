package com.pilotprep.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pilotprep.app.data.model.*
import com.pilotprep.app.data.repository.UserStatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * ViewModel pour la gestion des statistiques utilisateur
 */
@HiltViewModel
class UserStatsViewModel @Inject constructor(
    private val statsRepository: UserStatsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserStatsUiState())
    val uiState: StateFlow<UserStatsUiState> = _uiState.asStateFlow()

    private val _subjectStats = MutableStateFlow<List<SubjectStats>>(emptyList())
    val subjectStats: StateFlow<List<SubjectStats>> = _subjectStats.asStateFlow()

    private val _weeklyProgress = MutableStateFlow<List<WeeklyProgress>>(emptyList())
    val weeklyProgress: StateFlow<List<WeeklyProgress>> = _weeklyProgress.asStateFlow()

    private val _userGoals = MutableStateFlow<List<UserGoal>>(emptyList())
    val userGoals: StateFlow<List<UserGoal>> = _userGoals.asStateFlow()

    private val _achievements = MutableStateFlow<List<UserAchievement>>(emptyList())
    val achievements: StateFlow<List<UserAchievement>> = _achievements.asStateFlow()

    /**
     * Charge les statistiques d'un utilisateur
     */
    fun loadUserStats(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Charger les statistiques générales
                val userStats = statsRepository.getUserStats(userId)
                    ?: statsRepository.createInitialUserStats(userId)
                
                // Charger les statistiques par matière
                val subjectStatsList = statsRepository.getSubjectStats(userId)
                
                // Charger la progression hebdomadaire
                val weeklyProgressList = statsRepository.getWeeklyProgress(userId)
                
                // Charger les objectifs
                val goalsList = statsRepository.getUserGoals(userId)
                
                // Charger les récompenses
                val achievementsList = statsRepository.getUserAchievements(userId)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    userStats = userStats,
                    error = null
                )
                
                _subjectStats.value = subjectStatsList
                _weeklyProgress.value = weeklyProgressList
                _userGoals.value = goalsList
                _achievements.value = achievementsList
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erreur de chargement des statistiques: ${e.localizedMessage}"
                )
            }
        }
    }

    /**
     * Enregistre une session d'étude
     */
    fun recordStudySession(
        userId: String,
        subject: String,
        license: String,
        sessionType: SessionType,
        durationMinutes: Long,
        questionsAnswered: Int,
        correctAnswers: Int
    ) {
        viewModelScope.launch {
            try {
                val sessionId = UUID.randomUUID().toString()
                val score = if (questionsAnswered > 0) {
                    (correctAnswers.toFloat() / questionsAnswered * 100)
                } else 0f
                
                val session = StudySession(
                    sessionId = sessionId,
                    userId = userId,
                    subject = subject,
                    license = license,
                    sessionType = sessionType,
                    startTime = Date(),
                    endTime = Date(),
                    durationMinutes = durationMinutes,
                    questionsAnswered = questionsAnswered,
                    correctAnswers = correctAnswers,
                    score = score,
                    isCompleted = true
                )
                
                // Enregistrer la session
                statsRepository.recordStudySession(session)
                
                // Mettre à jour les statistiques de la matière
                updateSubjectStats(userId, subject, license, sessionType, score, durationMinutes)
                
                // Mettre à jour les statistiques générales
                updateGeneralStats(userId, sessionType, score, durationMinutes)
                
                // Vérifier les objectifs et récompenses
                checkGoalsAndAchievements(userId)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Erreur d'enregistrement de la session: ${e.localizedMessage}"
                )
            }
        }
    }

    /**
     * Met à jour les statistiques d'une matière
     */
    private suspend fun updateSubjectStats(
        userId: String,
        subject: String,
        license: String,
        sessionType: SessionType,
        score: Float,
        durationMinutes: Long
    ) {
        val existingStats = statsRepository.getSubjectStats(userId, license)
            .find { it.subject == subject }
        
        val updatedStats = if (existingStats != null) {
            val newQuizzesCompleted = if (sessionType == SessionType.QUIZ) {
                existingStats.quizzesCompleted + 1
            } else existingStats.quizzesCompleted
            
            val newExamsCompleted = if (sessionType == SessionType.EXAM) {
                existingStats.examsCompleted + 1
            } else existingStats.examsCompleted
            
            val totalSessions = newQuizzesCompleted + newExamsCompleted
            val newAverageScore = if (totalSessions > 0) {
                ((existingStats.averageScore * (totalSessions - 1)) + score) / totalSessions
            } else score
            
            existingStats.copy(
                quizzesCompleted = newQuizzesCompleted,
                examsCompleted = newExamsCompleted,
                averageScore = newAverageScore,
                bestScore = maxOf(existingStats.bestScore, score.toInt()),
                worstScore = minOf(existingStats.worstScore, score.toInt()),
                totalTimeMinutes = existingStats.totalTimeMinutes + durationMinutes,
                lastActivity = Date()
            )
        } else {
            SubjectStats(
                userId = userId,
                subject = subject,
                license = license,
                quizzesCompleted = if (sessionType == SessionType.QUIZ) 1 else 0,
                examsCompleted = if (sessionType == SessionType.EXAM) 1 else 0,
                averageScore = score,
                bestScore = score.toInt(),
                worstScore = score.toInt(),
                totalTimeMinutes = durationMinutes,
                lastActivity = Date()
            )
        }
        
        statsRepository.updateSubjectStats(updatedStats)
    }

    /**
     * Met à jour les statistiques générales
     */
    private suspend fun updateGeneralStats(
        userId: String,
        sessionType: SessionType,
        score: Float,
        durationMinutes: Long
    ) {
        val currentStats = statsRepository.getUserStats(userId) ?: return
        
        val newQuizzesCompleted = if (sessionType == SessionType.QUIZ) {
            currentStats.totalQuizzesCompleted + 1
        } else currentStats.totalQuizzesCompleted
        
        val newExamsCompleted = if (sessionType == SessionType.EXAM) {
            currentStats.totalExamsCompleted + 1
        } else currentStats.totalExamsCompleted
        
        val totalSessions = newQuizzesCompleted + newExamsCompleted
        val newAverageScore = if (totalSessions > 0) {
            ((currentStats.averageScore * (totalSessions - 1)) + score) / totalSessions
        } else score
        
        val updatedStats = currentStats.copy(
            totalQuizzesCompleted = newQuizzesCompleted,
            totalExamsCompleted = newExamsCompleted,
            totalStudyTimeMinutes = currentStats.totalStudyTimeMinutes + durationMinutes,
            averageScore = newAverageScore,
            lastStudyDate = Date()
        )
        
        statsRepository.updateUserStats(updatedStats)
    }

    /**
     * Vérifie et met à jour les objectifs et récompenses
     */
    private suspend fun checkGoalsAndAchievements(userId: String) {
        val currentStats = statsRepository.getUserStats(userId) ?: return
        val goals = statsRepository.getUserGoals(userId)
        
        // Vérifier les objectifs
        goals.forEach { goal ->
            val currentValue = when (goal.goalType) {
                GoalType.STUDY_TIME -> currentStats.totalStudyTimeMinutes.toInt()
                GoalType.QUIZ_COUNT -> currentStats.totalQuizzesCompleted
                GoalType.SCORE_IMPROVEMENT -> currentStats.averageScore.toInt()
                GoalType.STREAK_DAYS -> currentStats.currentStreak
                GoalType.SUBJECT_MASTERY -> 0 // À implémenter selon la logique métier
            }
            
            if (currentValue >= goal.targetValue && !goal.isCompleted) {
                statsRepository.updateUserGoal(goal.goalId, mapOf("isCompleted" to true))
            }
        }
        
        // Vérifier les récompenses automatiques
        checkAutomaticAchievements(userId, currentStats)
    }

    /**
     * Vérifie les récompenses automatiques
     */
    private suspend fun checkAutomaticAchievements(userId: String, stats: UserStats) {
        val achievements = statsRepository.getUserAchievements(userId)
        
        // Récompense pour le premier quiz
        if (stats.totalQuizzesCompleted >= 1) {
            val firstQuizAchievement = achievements.find { 
                it.title == "Premier Quiz" && !it.isUnlocked 
            }
            if (firstQuizAchievement != null) {
                statsRepository.unlockAchievement(firstQuizAchievement)
            }
        }
        
        // Récompense pour 10 quiz
        if (stats.totalQuizzesCompleted >= 10) {
            val tenQuizAchievement = achievements.find { 
                it.title == "Dix Quiz" && !it.isUnlocked 
            }
            if (tenQuizAchievement != null) {
                statsRepository.unlockAchievement(tenQuizAchievement)
            }
        }
        
        // Récompense pour la série de 7 jours
        if (stats.currentStreak >= 7) {
            val streakAchievement = achievements.find { 
                it.title == "Série de 7 Jours" && !it.isUnlocked 
            }
            if (streakAchievement != null) {
                statsRepository.unlockAchievement(streakAchievement)
            }
        }
    }

    /**
     * Crée un nouvel objectif
     */
    fun createGoal(
        userId: String,
        title: String,
        description: String,
        targetValue: Int,
        goalType: GoalType,
        targetDate: Date? = null
    ) {
        viewModelScope.launch {
            try {
                val goalId = UUID.randomUUID().toString()
                val goal = UserGoal(
                    goalId = goalId,
                    userId = userId,
                    title = title,
                    description = description,
                    targetValue = targetValue,
                    goalType = goalType,
                    targetDate = targetDate
                )
                
                statsRepository.createUserGoal(goal)
                loadUserStats(userId) // Recharger pour mettre à jour l'UI
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Erreur de création de l'objectif: ${e.localizedMessage}"
                )
            }
        }
    }

    /**
     * Rafraîchit les données
     */
    fun refreshStats(userId: String) {
        loadUserStats(userId)
    }
}

