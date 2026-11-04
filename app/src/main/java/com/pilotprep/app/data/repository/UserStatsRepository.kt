package com.pilotprep.app.data.repository

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pilotprep.app.data.model.*
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import com.pilotprep.app.util.ErrorHandler
import com.pilotprep.app.util.ErrorSeverity
import com.pilotprep.app.util.NetworkRetry

/**
 * Repository pour la gestion des statistiques utilisateur
 */
@Singleton
class UserStatsRepository @Inject constructor(
    private val errorHandler: ErrorHandler
) {
    private val db = Firebase.firestore

    // === STATISTIQUES GÉNÉRALES ===

    /**
     * Récupère les statistiques générales d'un utilisateur
     */
    suspend fun getUserStats(userId: String): UserStats? {
        return try {
            val doc = NetworkRetry.retryWithBackoff(maxRetries = 3, initialDelay = 800L) {
                Result.success(db.collection("user_stats").document(userId).get().await())
            }.getOrThrow()
            doc.toObject(UserStats::class.java)?.copy(userId = userId)
        } catch (e: Exception) {
            errorHandler.logError(e, "UserStatsRepository#getUserStats", ErrorSeverity.MEDIUM)
            null
        }
    }

    /**
     * Met à jour les statistiques générales d'un utilisateur
     */
    suspend fun updateUserStats(userStats: UserStats): Boolean {
        return try {
            val updatedStats = userStats.copy(updatedAt = Date())
            NetworkRetry.retryWithBackoff(maxRetries = 3, initialDelay = 800L) {
                Result.success(db.collection("user_stats").document(userStats.userId).set(updatedStats).await())
            }
            true
        } catch (e: Exception) {
            errorHandler.logError(e, "UserStatsRepository#updateUserStats", ErrorSeverity.MEDIUM)
            false
        }
    }

    /**
     * Crée les statistiques initiales pour un nouvel utilisateur
     */
    suspend fun createInitialUserStats(userId: String): UserStats {
        val initialStats = UserStats(
            userId = userId,
            createdAt = Date(),
            updatedAt = Date()
        )
        updateUserStats(initialStats)
        return initialStats
    }

    // === STATISTIQUES PAR MATIÈRE ===

    /**
     * Récupère les statistiques par matière pour un utilisateur
     */
    suspend fun getSubjectStats(userId: String, license: String? = null): List<SubjectStats> {
        return try {
            var query = db.collection("subject_stats").whereEqualTo("userId", userId)
            if (license != null) query = query.whereEqualTo("license", license)
            val snapshot = NetworkRetry.retryWithBackoff(maxRetries = 3, initialDelay = 800L) {
                Result.success(query.get().await())
            }.getOrThrow()
            snapshot.documents.mapNotNull { doc ->
                try { doc.toObject(SubjectStats::class.java) } catch (e: Exception) { errorHandler.logError(e, "UserStatsRepository#getSubjectStats", ErrorSeverity.LOW); null }
            }
        } catch (e: Exception) {
            errorHandler.logError(e, "UserStatsRepository#getSubjectStats", ErrorSeverity.MEDIUM)
            emptyList()
        }
    }

    /**
     * Met à jour les statistiques d'une matière spécifique
     */
    suspend fun updateSubjectStats(subjectStats: SubjectStats): Boolean {
        return try {
            val updatedStats = subjectStats.copy(updatedAt = Date())
            val docId = "${subjectStats.userId}_${subjectStats.license}_${subjectStats.subject}"
            NetworkRetry.retryWithBackoff(maxRetries = 3, initialDelay = 800L) {
                Result.success(db.collection("subject_stats").document(docId).set(updatedStats).await())
            }
            true
        } catch (e: Exception) {
            errorHandler.logError(e, "UserStatsRepository#updateSubjectStats", ErrorSeverity.MEDIUM)
            false
        }
    }

    // === SESSIONS D'ÉTUDE ===

    /**
     * Enregistre une nouvelle session d'étude
     */
    suspend fun recordStudySession(session: StudySession): Boolean {
        return try {
            NetworkRetry.retryWithBackoff(maxRetries = 3, initialDelay = 800L) {
                Result.success(db.collection("study_sessions").document(session.sessionId).set(session).await())
            }
            true
        } catch (e: Exception) {
            errorHandler.logError(e, "UserStatsRepository#recordStudySession", ErrorSeverity.MEDIUM)
            false
        }
    }

    /**
     * Met à jour une session d'étude existante
     */
    suspend fun updateStudySession(sessionId: String, updates: Map<String, Any>): Boolean {
        return try {
            val updateMap = updates.toMutableMap()
            updateMap["updatedAt"] = Date()
            
            db.collection("study_sessions").document(sessionId)
                .update(updateMap).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Récupère l'historique des sessions d'étude
     */
    suspend fun getStudySessionsHistory(
        userId: String, 
        limit: Int = 50,
        subject: String? = null
    ): List<StudySession> {
        return try {
            var query = db.collection("study_sessions")
                .whereEqualTo("userId", userId)
                .orderBy("startTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
            
            if (subject != null) {
                query = query.whereEqualTo("subject", subject)
            }
            
            val snapshot = query.get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(StudySession::class.java)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // === OBJECTIFS ===

    /**
     * Récupère les objectifs d'un utilisateur
     */
    suspend fun getUserGoals(userId: String): List<UserGoal> {
        return try {
            val snapshot = db.collection("user_goals")
                .whereEqualTo("userId", userId)
                .get().await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(UserGoal::class.java)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Crée un nouvel objectif
     */
    suspend fun createUserGoal(goal: UserGoal): Boolean {
        return try {
            db.collection("user_goals").document(goal.goalId)
                .set(goal).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Met à jour un objectif
     */
    suspend fun updateUserGoal(goalId: String, updates: Map<String, Any>): Boolean {
        return try {
            val updateMap = updates.toMutableMap()
            updateMap["updatedAt"] = Date()
            
            db.collection("user_goals").document(goalId)
                .update(updateMap).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // === RÉCOMPENSES ===

    /**
     * Récupère les récompenses d'un utilisateur
     */
    suspend fun getUserAchievements(userId: String): List<UserAchievement> {
        return try {
            val snapshot = db.collection("user_achievements")
                .whereEqualTo("userId", userId)
                .get().await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(UserAchievement::class.java)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Débloque une récompense
     */
    suspend fun unlockAchievement(achievement: UserAchievement): Boolean {
        return try {
            val unlockedAchievement = achievement.copy(
                isUnlocked = true,
                unlockedAt = Date()
            )
            db.collection("user_achievements").document(achievement.achievementId)
                .set(unlockedAchievement).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // === PROGRESSION HEBDOMADAIRE ===

    /**
     * Récupère la progression hebdomadaire
     */
    suspend fun getWeeklyProgress(userId: String, weeks: Int = 4): List<WeeklyProgress> {
        return try {
            val snapshot = db.collection("weekly_progress")
                .whereEqualTo("userId", userId)
                .orderBy("weekStartDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(weeks.toLong())
                .get().await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(WeeklyProgress::class.java)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Met à jour la progression hebdomadaire
     */
    suspend fun updateWeeklyProgress(progress: WeeklyProgress): Boolean {
        return try {
            db.collection("weekly_progress").document(progress.weekId)
                .set(progress).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // === MÉTHODES UTILITAIRES ===

    /**
     * Calcule les statistiques agrégées
     */
    suspend fun calculateAggregatedStats(userId: String): UserStats? {
        val sessions = getStudySessionsHistory(userId, 1000) // Récupère toutes les sessions
        val subjectStats = getSubjectStats(userId)
        
        if (sessions.isEmpty()) return null
        
        val totalQuizzes = sessions.count { it.sessionType == SessionType.QUIZ }
        val totalExams = sessions.count { it.sessionType == SessionType.EXAM }
        val totalStudyTime = sessions.sumOf { it.durationMinutes }
        val averageScore = sessions.map { it.score }.average().toFloat()
        
        // Calcul de la série actuelle (simplifié)
        val currentStreak = calculateCurrentStreak(sessions)
        
        return UserStats(
            userId = userId,
            totalQuizzesCompleted = totalQuizzes,
            totalExamsCompleted = totalExams,
            totalStudyTimeMinutes = totalStudyTime,
            averageScore = averageScore,
            currentStreak = currentStreak,
            longestStreak = currentStreak, // À améliorer avec un calcul plus sophistiqué
            lastStudyDate = sessions.maxByOrNull { it.startTime }?.startTime,
            updatedAt = Date()
        )
    }

    /**
     * Calcule la série actuelle de jours consécutifs
     */
    private fun calculateCurrentStreak(sessions: List<StudySession>): Int {
        if (sessions.isEmpty()) return 0
        
        val sortedSessions = sessions.sortedByDescending { it.startTime }
        val calendar = Calendar.getInstance()
        var streak = 0
        
        for (session in sortedSessions) {
            val sessionDate = Calendar.getInstance().apply { time = session.startTime }
            val today = Calendar.getInstance()
            
            val daysDiff = ((today.timeInMillis - sessionDate.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
            
            if (daysDiff == streak) {
                streak++
            } else {
                break
            }
        }
        
        return streak
    }
}

