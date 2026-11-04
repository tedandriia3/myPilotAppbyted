package com.pilotprep.app.data.repository

import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pilotprep.app.data.model.Question
import com.pilotprep.app.util.ErrorHandler
import com.pilotprep.app.util.ErrorSeverity
import com.pilotprep.app.util.NetworkRetry
import com.pilotprep.app.util.PerformanceMonitor
import com.pilotprep.app.util.ValidationUtils
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository optimisé pour la gestion des quiz avec gestion d'erreurs améliorée
 */
@Singleton
class QuizRepository @Inject constructor(
    private val performanceMonitor: PerformanceMonitor,
    private val errorHandler: ErrorHandler
) {
    private val db = Firebase.firestore

    /**
     * Récupère les questions pour la pratique avec gestion d'erreurs robuste
     */
    suspend fun getQuestionsForPractice(license: String, subject: String, limit: Int = 10): Result<List<Question>> {
        return performanceMonitor.measureOperation("getQuestionsForPractice") {
            errorHandler.logDebug("getQuestionsForPractice", "Chargement des questions pour $license - $subject")
            
            // Utiliser retry logic pour les erreurs réseau
            NetworkRetry.retryWithBackoff(maxRetries = 3, initialDelay = 1000L) {
                try {
                    // Validation des paramètres
                    if (license.isBlank() || subject.isBlank()) {
                        return@retryWithBackoff Result.failure(QuizException.InvalidParameters("Licence ou sujet vide"))
                    }
                    
                    if (limit <= 0 || limit > 50) {
                        return@retryWithBackoff Result.failure(QuizException.InvalidParameters("Limite invalide: $limit"))
                    }
                    
                    val snapshot = db.collection("questions")
                        .whereEqualTo("license", license)
                        .whereEqualTo("subject", subject)
                        .limit(limit.toLong())
                        .get()
                        .await()

                    val questions = snapshot.documents.mapNotNull { doc ->
                        try {
                            val question = doc.toObject(Question::class.java)?.copy(id = doc.id)
                            // Validation de la question
                            if (question != null) {
                                val validation = ValidationUtils.validateQuestion(question)
                                if (validation is ValidationUtils.ValidationResult.Invalid) {
                                    errorHandler.logError(
                                        Exception("Question invalide: ${validation.errors.joinToString()}"),
                                        "getQuestionsForPractice",
                                        ErrorSeverity.LOW
                                    )
                                    null
                                } else {
                                    question
                                }
                            } else {
                                null
                            }
                        } catch (e: Exception) {
                            errorHandler.logError(e, "getQuestionsForPractice (parsing)", ErrorSeverity.LOW)
                            null
                        }
                    }
                    
                    if (questions.isEmpty()) {
                        errorHandler.logError(
                            QuizException.NoQuestionsFound("Aucune question trouvée pour $license - $subject"),
                            "getQuestionsForPractice",
                            ErrorSeverity.MEDIUM
                        )
                        Result.failure(QuizException.NoQuestionsFound("Aucune question trouvée pour $license - $subject"))
                    } else {
                        errorHandler.logInfo("getQuestionsForPractice", "${questions.size} questions chargées avec succès")
                        Result.success(questions.shuffled())
                    }
                } catch (e: FirebaseFirestoreException) {
                    when (e.code) {
                        FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                            errorHandler.logError(e, "getQuestionsForPractice", ErrorSeverity.HIGH, "Permission refusée")
                            Result.failure(QuizException.PermissionDenied("Accès refusé aux questions"))
                        }
                        FirebaseFirestoreException.Code.UNAVAILABLE -> {
                            errorHandler.logError(e, "getQuestionsForPractice", ErrorSeverity.MEDIUM, "Service indisponible")
                            Result.failure(QuizException.NetworkError("Service indisponible"))
                        }
                        else -> {
                            errorHandler.logError(e, "getQuestionsForPractice", ErrorSeverity.HIGH, "Erreur Firebase")
                            Result.failure(QuizException.FirebaseError("Erreur Firebase: ${e.message}"))
                        }
                    }
                } catch (e: Exception) {
                    errorHandler.logError(e, "getQuestionsForPractice", ErrorSeverity.HIGH, "Erreur inattendue")
                    Result.failure(QuizException.UnknownError("Erreur inattendue: ${e.message}"))
                }
            }
        }
    }

    /**
     * Récupère les questions pour l'examen avec validation
     */
    suspend fun getQuestionsForExam(license: String, subject: String, limit: Int = 10): Result<List<Question>> {
        return performanceMonitor.measureOperation("getQuestionsForExam") {
            try {
                // Validation des paramètres
                if (license.isBlank() || subject.isBlank()) {
                    return@measureOperation Result.failure(QuizException.InvalidParameters("Licence ou sujet vide"))
                }
                
                if (limit <= 0 || limit > 50) {
                    return@measureOperation Result.failure(QuizException.InvalidParameters("Limite invalide: $limit"))
                }

                val snapshot = db.collection("questions")
                    .whereEqualTo("license", license)
                    .whereEqualTo("subject", subject)
                    .limit(limit.toLong())
                    .get()
                    .await()

                val questions = snapshot.documents.mapNotNull { doc ->
                    try {
                        val question = doc.toObject(Question::class.java)?.copy(id = doc.id)
                        // Validation de la question
                        if (question != null && isValidQuestion(question)) {
                            question
                        } else {
                            android.util.Log.w("QuizRepository", "Question invalide: ${doc.id}")
                            null
                        }
                    } catch (e: Exception) {
                        android.util.Log.w("QuizRepository", "Erreur parsing question ${doc.id}: ${e.message}")
                        null
                    }
                }
                
                if (questions.isEmpty()) {
                    Result.failure(QuizException.NoQuestionsFound("Aucune question valide trouvée pour $license - $subject"))
                } else {
                    Result.success(questions.shuffled())
                }
            } catch (e: FirebaseFirestoreException) {
                when (e.code) {
                    FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                        Result.failure(QuizException.PermissionDenied("Accès refusé aux questions d'examen"))
                    }
                    FirebaseFirestoreException.Code.UNAVAILABLE -> {
                        Result.failure(QuizException.NetworkError("Service indisponible"))
                    }
                    else -> {
                        Result.failure(QuizException.FirebaseError("Erreur Firebase: ${e.message}"))
                    }
                }
            } catch (e: Exception) {
                Result.failure(QuizException.UnknownError("Erreur inattendue: ${e.message}"))
            }
        }
    }

    /**
     * Valide qu'une question est complète et valide
     */
    private fun isValidQuestion(question: Question): Boolean {
        return question.questionText.isNotBlank() &&
                question.options.isNotEmpty() &&
                question.correctOptionIndex >= 0 &&
                question.correctOptionIndex < question.options.size
    }

    /**
     * Récupère le nombre total de questions disponibles
     */
    suspend fun getQuestionCount(license: String, subject: String): Result<Int> {
        return performanceMonitor.measureOperation("getQuestionCount") {
            try {
                val snapshot = db.collection("questions")
                    .whereEqualTo("license", license)
                    .whereEqualTo("subject", subject)
                    .get()
                    .await()
                
                Result.success(snapshot.size())
            } catch (e: Exception) {
                Result.failure(QuizException.UnknownError("Erreur lors du comptage: ${e.message}"))
            }
        }
    }
}

/**
 * Exceptions spécifiques pour les quiz
 */
sealed class QuizException(message: String) : Exception(message) {
    class NoQuestionsFound(message: String) : QuizException(message)
    class PermissionDenied(message: String) : QuizException(message)
    class NetworkError(message: String) : QuizException(message)
    class FirebaseError(message: String) : QuizException(message)
    class InvalidParameters(message: String) : QuizException(message)
    class UnknownError(message: String) : QuizException(message)
}