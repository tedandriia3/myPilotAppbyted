package com.pilotprep.app.data.model

import java.util.Date

/**
 * Modèle de données pour les statistiques utilisateur
 */
data class UserStats(
    val userId: String = "",
    val totalQuizzesCompleted: Int = 0,
    val totalExamsCompleted: Int = 0,
    val totalStudyTimeMinutes: Long = 0,
    val averageScore: Float = 0f,
    val currentStreak: Int = 0, // Série de jours consécutifs
    val longestStreak: Int = 0,
    val lastStudyDate: Date? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

/**
 * Statistiques par matière
 */
data class SubjectStats(
    val userId: String = "",
    val subject: String = "",
    val license: String = "", // PPL, CPL, ATPL
    val quizzesCompleted: Int = 0,
    val examsCompleted: Int = 0,
    val averageScore: Float = 0f,
    val bestScore: Int = 0,
    val worstScore: Int = 0,
    val totalTimeMinutes: Long = 0,
    val lastActivity: Date? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

/**
 * Session d'étude individuelle
 */
data class StudySession(
    val sessionId: String = "",
    val userId: String = "",
    val subject: String = "",
    val license: String = "",
    val sessionType: SessionType = SessionType.QUIZ,
    val startTime: Date = Date(),
    val endTime: Date? = null,
    val durationMinutes: Long = 0,
    val questionsAnswered: Int = 0,
    val correctAnswers: Int = 0,
    val score: Float = 0f,
    val isCompleted: Boolean = false
)

/**
 * Type de session d'étude
 */
enum class SessionType {
    QUIZ,           // Quiz de pratique
    EXAM,           // Examen simulé
    THEORY,         // Lecture de théorie
    REVIEW          // Révision
}

/**
 * Objectif utilisateur
 */
data class UserGoal(
    val goalId: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val targetValue: Int = 0,
    val currentValue: Int = 0,
    val goalType: GoalType = GoalType.STUDY_TIME,
    val targetDate: Date? = null,
    val isCompleted: Boolean = false,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

/**
 * Type d'objectif
 */
enum class GoalType {
    STUDY_TIME,         // Temps d'étude (en minutes)
    QUIZ_COUNT,         // Nombre de quiz
    SCORE_IMPROVEMENT,  // Amélioration du score
    STREAK_DAYS,        // Série de jours
    SUBJECT_MASTERY     // Maîtrise d'une matière
}

/**
 * Récompense utilisateur
 */
data class UserAchievement(
    val achievementId: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val iconUrl: String = "",
    val points: Int = 0,
    val unlockedAt: Date? = null,
    val isUnlocked: Boolean = false,
    val createdAt: Date = Date()
)

/**
 * Progression hebdomadaire
 */
data class WeeklyProgress(
    val weekId: String = "",
    val userId: String = "",
    val weekStartDate: Date = Date(),
    val weekEndDate: Date = Date(),
    val studyTimeMinutes: Long = 0,
    val quizzesCompleted: Int = 0,
    val averageScore: Float = 0f,
    val subjectsStudied: List<String> = emptyList(),
    val dailyActivity: Map<String, Long> = emptyMap() // Jour -> minutes d'étude
)

