package com.pilotprep.app.services

import com.pilotprep.app.data.model.*
import com.pilotprep.app.data.repository.UserStatsRepository
import com.pilotprep.app.data.model.UserStatsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service partagé pour les statistiques utilisateur
 * Utilisé pour synchroniser les données entre les différents écrans
 */
@Singleton
class UserStatsService @Inject constructor(
    private val statsRepository: UserStatsRepository
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

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
     * Rafraîchit les statistiques d'un utilisateur
     */
    fun refreshStats(userId: String) {
        loadUserStats(userId)
    }

    /**
     * Charge les statistiques d'un utilisateur
     */
    fun loadUserStats(userId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        coroutineScope.launch {
            try {
                val baseStats = statsRepository.getUserStats(userId)
                    ?: statsRepository.createInitialUserStats(userId)

                val aggregated = statsRepository.calculateAggregatedStats(userId)
                val mergedStats = aggregated?.let { agg ->
                    baseStats.copy(
                        totalQuizzesCompleted = agg.totalQuizzesCompleted,
                        totalExamsCompleted = agg.totalExamsCompleted,
                        totalStudyTimeMinutes = agg.totalStudyTimeMinutes,
                        averageScore = agg.averageScore,
                        currentStreak = agg.currentStreak,
                        longestStreak = maxOf(baseStats.longestStreak, agg.currentStreak),
                        lastStudyDate = agg.lastStudyDate ?: baseStats.lastStudyDate,
                        updatedAt = agg.updatedAt
                    )
                } ?: baseStats

                val subjectStats = statsRepository.getSubjectStats(userId)
                val weeklyProgress = statsRepository.getWeeklyProgress(userId)
                val goals = statsRepository.getUserGoals(userId)
                val achievements = statsRepository.getUserAchievements(userId)

                statsRepository.updateUserStats(mergedStats)

                _uiState.value = _uiState.value.copy(
                    userStats = mergedStats,
                    isLoading = false,
                    error = null
                )

                _subjectStats.value = subjectStats
                _weeklyProgress.value = weeklyProgress
                _userGoals.value = goals
                _achievements.value = achievements
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erreur de chargement des statistiques: ${e.localizedMessage}"
                )
            }
        }
    }

    /**
     * Enregistre une session d'étude et met à jour les statistiques
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
        coroutineScope.launch {
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
                
                // Recharger les données pour mise à jour de l'UI
                loadUserStats(userId)
                
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

}
