package com.pilotprep.app.data.repository

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pilotprep.app.data.model.WeeklyTip
import com.pilotprep.app.data.model.WeeklyTipConfig
import com.pilotprep.app.data.model.WeekUtils
import com.pilotprep.app.util.ErrorHandler
import com.pilotprep.app.util.ErrorSeverity
import com.pilotprep.app.util.NetworkRetry
import kotlinx.coroutines.tasks.await
import com.pilotprep.app.util.DataValidator
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

interface WeeklyTipRepository {
    suspend fun getCurrentWeeklyTip(): WeeklyTip?
    suspend fun getWeeklyTipForWeek(weekNumber: Int): WeeklyTip?
    suspend fun getWeeklyTipForDate(date: LocalDate): WeeklyTip?
    suspend fun getAllWeeklyTips(): List<WeeklyTip>
    suspend fun getWeeklyTipConfig(): WeeklyTipConfig
    suspend fun updateWeeklyTipConfig(config: WeeklyTipConfig): Result<Unit>
    suspend fun addWeeklyTip(tip: WeeklyTip): Result<Unit>
    suspend fun updateWeeklyTip(tip: WeeklyTip): Result<Unit>
    suspend fun deleteWeeklyTip(tipId: String): Result<Unit>
}

@Singleton
class WeeklyTipRepositoryImpl @Inject constructor(
    private val errorHandler: ErrorHandler
) : WeeklyTipRepository {

    private val db = Firebase.firestore
    private val CONFIG_DOC_ID = "weekly_tip_config"
    private val WEEKLY_TIPS_COLLECTION = "weekly_tips"

    override suspend fun getCurrentWeeklyTip(): WeeklyTip? {
        val currentWeek = WeekUtils.getCurrentWeekOfYear()
        println("DEBUG: Recherche astuce pour la semaine $currentWeek")
        return getWeeklyTipForWeek(currentWeek)
    }

    override suspend fun getWeeklyTipForWeek(weekNumber: Int): WeeklyTip? {
        if (!WeekUtils.isValidWeekNumber(weekNumber)) {
            println("ERROR: Numéro de semaine invalide: $weekNumber")
            return null
        }

        return try {
            println("DEBUG: Recherche dans Firebase pour semaine $weekNumber")
            val snapshot = NetworkRetry.retryWithBackoff(maxRetries = 3, initialDelay = 800L) {
                Result.success(
                    db.collection(WEEKLY_TIPS_COLLECTION)
                        .whereEqualTo("weekOfYear", weekNumber)
                        .whereEqualTo("isActive", true)
                        .limit(1)
                        .get()
                        .await()
                )
            }.getOrThrow()

            println("DEBUG: Firebase a retourné ${snapshot.documents.size} documents")
            
            if (snapshot.isEmpty) {
                println("DEBUG: Aucun document trouvé pour la semaine $weekNumber")
                // Essayer de récupérer n'importe quelle astuce active
                return getAnyActiveWeeklyTip()
            } else {
                val doc = snapshot.documents.first()
                val data = doc.data
                if (data != null) {
                    // Conversion sécurisée des types Firebase
                    val weekOfYear = when (val weekValue = data["weekOfYear"]) {
                        is Long -> weekValue.toInt()
                        is Int -> weekValue
                        is Number -> weekValue.toInt()
                        else -> null
                    }
                    
                    val isActive = when (val activeValue = data["isActive"]) {
                        is Boolean -> activeValue
                        is String -> activeValue.toBooleanStrictOrNull() ?: true
                        else -> true
                    }
                    
                    val tip = WeeklyTip(
                        id = doc.id,
                        weekOfYear = weekOfYear ?: 1,
                        title = data["title"]?.toString() ?: "",
                        introduction = data["introduction"]?.toString() ?: "",
                        fullContent = data["fullContent"]?.toString() ?: "",
                        category = data["category"]?.toString() ?: "",
                        imageUrl = data["imageUrl"]?.toString() ?: "",
                        createdAt = (data["createdAt"] as? Long) ?: System.currentTimeMillis(),
                        updatedAt = (data["updatedAt"] as? Long) ?: System.currentTimeMillis(),
                        isActive = isActive
                    )
                    
                    println("DEBUG: Astuce trouvée: ${tip.title} (semaine: ${tip.weekOfYear})")
                    tip
                } else {
                    println("DEBUG: Données nulles pour le document")
                    null
                }
            }
        } catch (e: Exception) {
            println("ERROR: Exception lors de la récupération de l'astuce: ${e.message}")
            errorHandler.logError(e, "WeeklyTipRepository#getWeeklyTipForWeek", ErrorSeverity.MEDIUM)
            null
        }
    }
    
    // Nouvelle méthode pour récupérer n'importe quelle astuce active
    private suspend fun getAnyActiveWeeklyTip(): WeeklyTip? {
        return try {
            println("DEBUG: Tentative de récupération d'une astuce active quelconque")
            val snapshot = NetworkRetry.retryWithBackoff(maxRetries = 3, initialDelay = 800L) {
                Result.success(
                    db.collection(WEEKLY_TIPS_COLLECTION)
                        .whereEqualTo("isActive", true)
                        .limit(1)
                        .get()
                        .await()
                )
            }.getOrThrow()
                
            if (snapshot.isEmpty) {
                println("DEBUG: Aucune astuce active trouvée dans Firebase")
                null
            } else {
                val doc = snapshot.documents.first()
                val data = doc.data
                if (data != null) {
                    // Conversion sécurisée des types Firebase
                    val weekOfYear = when (val weekValue = data["weekOfYear"]) {
                        is Long -> weekValue.toInt()
                        is Int -> weekValue
                        is Number -> weekValue.toInt()
                        else -> 1
                    }
                    
                    val isActive = when (val activeValue = data["isActive"]) {
                        is Boolean -> activeValue
                        is String -> activeValue.toBooleanStrictOrNull() ?: true
                        else -> true
                    }
                    
                    val tip = WeeklyTip(
                        id = doc.id,
                        weekOfYear = weekOfYear,
                        title = data["title"]?.toString() ?: "",
                        introduction = data["introduction"]?.toString() ?: "",
                        fullContent = data["fullContent"]?.toString() ?: "",
                        category = data["category"]?.toString() ?: "",
                        imageUrl = data["imageUrl"]?.toString() ?: "",
                        createdAt = (data["createdAt"] as? Long) ?: System.currentTimeMillis(),
                        updatedAt = (data["updatedAt"] as? Long) ?: System.currentTimeMillis(),
                        isActive = isActive
                    )
                    
                    println("DEBUG: Astuce de fallback trouvée: ${tip.title} (semaine: ${tip.weekOfYear})")
                    tip
                } else {
                    println("DEBUG: Données nulles pour l'astuce de fallback")
                    null
                }
            }
        } catch (e: Exception) {
            println("ERROR: Exception lors de la récupération de fallback: ${e.message}")
            errorHandler.logError(e, "WeeklyTipRepository#getAnyActiveWeeklyTip", ErrorSeverity.MEDIUM)
            null
        }
    }

    override suspend fun getWeeklyTipForDate(date: LocalDate): WeeklyTip? {
        val weekNumber = WeekUtils.getWeekOfYearForDate(date)
        return getWeeklyTipForWeek(weekNumber)
    }

    override suspend fun getAllWeeklyTips(): List<WeeklyTip> {
        return try {
            val snapshot = NetworkRetry.retryWithBackoff(maxRetries = 3, initialDelay = 800L) {
                Result.success(
                    db.collection(WEEKLY_TIPS_COLLECTION)
                        .whereEqualTo("isActive", true)
                        .orderBy("weekOfYear")
                        .get()
                        .await()
                )
            }.getOrThrow()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(WeeklyTip::class.java)?.copy(id = doc.id)
            }.filter { DataValidator.isValidWeeklyTip(it) }
        } catch (e: Exception) {
            errorHandler.logError(e, "WeeklyTipRepository#getAllWeeklyTips", ErrorSeverity.MEDIUM)
            emptyList()
        }
    }

    override suspend fun getWeeklyTipConfig(): WeeklyTipConfig {
        return try {
            val doc = NetworkRetry.retryWithBackoff(maxRetries = 3, initialDelay = 800L) {
                Result.success(db.collection("config").document(CONFIG_DOC_ID).get().await())
            }.getOrThrow()
            if (doc.exists()) {
                doc.toObject(WeeklyTipConfig::class.java) ?: WeeklyTipConfig()
            } else {
                WeeklyTipConfig()
            }
        } catch (e: Exception) {
            errorHandler.logError(e, "WeeklyTipRepository#getWeeklyTipConfig", ErrorSeverity.MEDIUM)
            WeeklyTipConfig()
        }
    }

    override suspend fun updateWeeklyTipConfig(config: WeeklyTipConfig): Result<Unit> {
        return try {
            NetworkRetry.retryWithBackoff(maxRetries = 3, initialDelay = 800L) {
                Result.success(db.collection("config").document(CONFIG_DOC_ID).set(config).await())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            errorHandler.logError(e, "WeeklyTipRepository#updateWeeklyTipConfig", ErrorSeverity.MEDIUM)
            Result.failure(e)
        }
    }

    override suspend fun addWeeklyTip(tip: WeeklyTip): Result<Unit> {
        return try {
            NetworkRetry.retryWithBackoff(maxRetries = 3, initialDelay = 800L) {
                Result.success(db.collection(WEEKLY_TIPS_COLLECTION).add(tip).await())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            errorHandler.logError(e, "WeeklyTipRepository#addWeeklyTip", ErrorSeverity.MEDIUM)
            Result.failure(e)
        }
    }

    override suspend fun updateWeeklyTip(tip: WeeklyTip): Result<Unit> {
        return try {
            if (tip.id.isNotEmpty()) {
                NetworkRetry.retryWithBackoff(maxRetries = 3, initialDelay = 800L) {
                    Result.success(
                        db.collection(WEEKLY_TIPS_COLLECTION)
                            .document(tip.id)
                            .set(tip)
                            .await()
                    )
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Tip ID is empty"))
            }
        } catch (e: Exception) {
            errorHandler.logError(e, "WeeklyTipRepository#updateWeeklyTip", ErrorSeverity.MEDIUM)
            Result.failure(e)
        }
    }

    override suspend fun deleteWeeklyTip(tipId: String): Result<Unit> {
        return try {
            NetworkRetry.retryWithBackoff(maxRetries = 3, initialDelay = 800L) {
                Result.success(db.collection(WEEKLY_TIPS_COLLECTION).document(tipId).delete().await())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            errorHandler.logError(e, "WeeklyTipRepository#deleteWeeklyTip", ErrorSeverity.MEDIUM)
            Result.failure(e)
        }
    }
}

