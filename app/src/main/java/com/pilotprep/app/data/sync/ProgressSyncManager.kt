package com.pilotprep.app.data.sync

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import com.pilotprep.app.data.local.OfflineQuizResult
import com.pilotprep.app.util.NetworkManager
import com.pilotprep.app.util.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressSyncManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val networkManager: NetworkManager,
    private val sessionManager: SessionManager
) {
    
    /**
     * Synchronise les progrès utilisateur avec Firebase
     */
    suspend fun syncUserProgress(): Result<SyncResult> {
        return try {
            if (!networkManager.isNetworkAvailable()) {
                return Result.failure(Exception("Aucune connexion Internet"))
            }
            
            val session = sessionManager.sessionState.first()
            val userId = session.userId
            
            if (userId.isEmpty()) {
                return Result.failure(Exception("Utilisateur non connecté"))
            }
            
            // Synchroniser les données de session
            val sessionData = mapOf(
                "userId" to userId,
                "lastLogin" to session.lastLogin,
                "currentWeek" to session.currentWeek,
                "totalStudyTime" to session.totalStudyTime,
                "lastQuizScore" to session.lastQuizScore,
                "preferredLanguage" to session.preferredLanguage,
                "darkModeEnabled" to session.darkModeEnabled,
                "notificationSettings" to session.notificationSettings,
                "lastSync" to System.currentTimeMillis()
            )
            
            firestore.collection("user_progress")
                .document(userId)
                .set(sessionData)
                .await()
            
            Result.success(SyncResult(
                syncedItems = 1,
                totalItems = 1,
                syncTime = System.currentTimeMillis()
            ))
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Synchronise les résultats de quiz
     */
    suspend fun syncQuizResults(results: List<OfflineQuizResult>): Result<SyncResult> {
        return try {
            if (!networkManager.isNetworkAvailable()) {
                return Result.failure(Exception("Aucune connexion Internet"))
            }
            
            val batch = firestore.batch()
            var syncedCount = 0
            
            for (result in results) {
                val resultData = mapOf(
                    "quizId" to result.quizId,
                    "userId" to result.userId,
                    "score" to result.score,
                    "totalQuestions" to result.totalQuestions,
                    "answers" to result.answers,
                    "completedAt" to result.completedAt,
                    "syncedAt" to System.currentTimeMillis()
                )
                
                batch.set(
                    firestore.collection("quiz_results").document(result.id),
                    resultData
                )
                syncedCount++
            }
            
            batch.commit().await()
            
            Result.success(SyncResult(
                syncedItems = syncedCount,
                totalItems = results.size,
                syncTime = System.currentTimeMillis()
            ))
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Récupère les progrès depuis Firebase
     */
    suspend fun fetchUserProgress(userId: String): Result<UserProgressData> {
        return try {
            if (!networkManager.isNetworkAvailable()) {
                return Result.failure(Exception("Aucune connexion Internet"))
            }
            
            val document = firestore.collection("user_progress")
                .document(userId)
                .get()
                .await()
            
            if (!document.exists()) {
                return Result.failure(Exception("Aucun progrès trouvé"))
            }
            
            val data = document.data ?: return Result.failure(Exception("Données vides"))
            
            val progressData = UserProgressData(
                userId = data["userId"]?.toString() ?: userId,
                lastLogin = (data["lastLogin"] as? Long) ?: 0L,
                currentWeek = (data["currentWeek"] as? Long)?.toInt() ?: 1,
                totalStudyTime = (data["totalStudyTime"] as? Long) ?: 0L,
                lastQuizScore = (data["lastQuizScore"] as? Long)?.toInt() ?: 0,
                preferredLanguage = data["preferredLanguage"]?.toString() ?: "fr",
                darkModeEnabled = (data["darkModeEnabled"] as? Boolean) ?: false,
                notificationSettings = data["notificationSettings"]?.toString() ?: "all",
                lastSync = (data["lastSync"] as? Long) ?: 0L
            )
            
            Result.success(progressData)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Surveille les changements de progrès en temps réel
     */
    fun observeProgressChanges(userId: String): Flow<UserProgressData?> = flow {
        if (!networkManager.isNetworkAvailable()) {
            emit(null)
            return@flow
        }
        
        try {
            val document = firestore.collection("user_progress")
                .document(userId)
                .get()
                .await()
            
            if (document.exists()) {
                val data = document.data ?: return@flow
                
                val progressData = UserProgressData(
                    userId = data["userId"]?.toString() ?: userId,
                    lastLogin = (data["lastLogin"] as? Long) ?: 0L,
                    currentWeek = (data["currentWeek"] as? Long)?.toInt() ?: 1,
                    totalStudyTime = (data["totalStudyTime"] as? Long) ?: 0L,
                    lastQuizScore = (data["lastQuizScore"] as? Long)?.toInt() ?: 0,
                    preferredLanguage = data["preferredLanguage"]?.toString() ?: "fr",
                    darkModeEnabled = (data["darkModeEnabled"] as? Boolean) ?: false,
                    notificationSettings = data["notificationSettings"]?.toString() ?: "all",
                    lastSync = (data["lastSync"] as? Long) ?: 0L
                )
                
                emit(progressData)
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            emit(null)
        }
    }
    
    /**
     * Synchronise automatiquement en arrière-plan
     */
    suspend fun autoSync(): Result<SyncResult> {
        return try {
            val session = sessionManager.sessionState.first()
            
            // Synchroniser les progrès de session
            val sessionResult = syncUserProgress()
            if (sessionResult.isFailure) {
                return sessionResult
            }
            
            // Synchroniser les résultats de quiz non synchronisés
            // (Cette partie nécessiterait l'accès au repository des résultats)
            
            Result.success(SyncResult(
                syncedItems = 1,
                totalItems = 1,
                syncTime = System.currentTimeMillis()
            ))
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Résout les conflits de synchronisation
     */
    suspend fun resolveSyncConflicts(
        localData: UserProgressData,
        remoteData: UserProgressData
    ): UserProgressData {
        return UserProgressData(
            userId = localData.userId,
            lastLogin = maxOf(localData.lastLogin, remoteData.lastLogin),
            currentWeek = maxOf(localData.currentWeek, remoteData.currentWeek),
            totalStudyTime = maxOf(localData.totalStudyTime, remoteData.totalStudyTime),
            lastQuizScore = maxOf(localData.lastQuizScore, remoteData.lastQuizScore),
            preferredLanguage = remoteData.preferredLanguage, // Préférer les paramètres distants
            darkModeEnabled = remoteData.darkModeEnabled,
            notificationSettings = remoteData.notificationSettings,
            lastSync = System.currentTimeMillis()
        )
    }
}

data class SyncResult(
    val syncedItems: Int,
    val totalItems: Int,
    val syncTime: Long
) {
    val isComplete: Boolean get() = syncedItems == totalItems
    val syncPercentage: Float get() = if (totalItems > 0) syncedItems.toFloat() / totalItems else 0f
}

data class UserProgressData(
    val userId: String,
    val lastLogin: Long,
    val currentWeek: Int,
    val totalStudyTime: Long,
    val lastQuizScore: Int,
    val preferredLanguage: String,
    val darkModeEnabled: Boolean,
    val notificationSettings: String,
    val lastSync: Long
)
