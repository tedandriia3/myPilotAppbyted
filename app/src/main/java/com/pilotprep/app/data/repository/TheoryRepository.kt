package com.pilotprep.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.pilotprep.app.data.model.Lesson
import com.pilotprep.app.util.ErrorHandler
import com.pilotprep.app.util.ErrorSeverity
import com.pilotprep.app.util.NetworkRetry
import com.pilotprep.app.util.DataValidator
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TheoryRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val errorHandler: ErrorHandler
) {
    suspend fun getAllLessonsForDebugging(): List<Lesson> {
        return try {
            val snapshot = NetworkRetry.retryWithBackoff(maxRetries = 3, initialDelay = 800L) {
                Result.success(db.collection("lessons").get().await())
            }.getOrThrow()
            val lessons = snapshot.documents.mapNotNull { doc ->
                try { doc.toObject<Lesson>()?.copy(id = doc.id) }
                catch (e: Exception) { errorHandler.logError(e, "TheoryRepository#getAllLessonsForDebugging", ErrorSeverity.LOW); null }
            }.filter { DataValidator.isValidLesson(it) }
            println("DEBUG: Toutes les leçons dans Firestore:")
            lessons.forEach { lesson ->
                println("  - ID: ${lesson.id}, Title: ${lesson.title}, Subject: ${lesson.subject}, License: ${lesson.category}")
            }
            lessons
        } catch (e: Exception) {
            errorHandler.logError(e, "TheoryRepository#getAllLessonsForDebugging", ErrorSeverity.MEDIUM)
            emptyList()
        }
    }

    suspend fun getLessonById(lessonId: String): Lesson? {
        return try {
            val doc = NetworkRetry.retryWithBackoff(maxRetries = 3, initialDelay = 800L) {
                Result.success(db.collection("lessons").document(lessonId).get().await())
            }.getOrThrow()
            if (doc.exists()) {
                val lesson = doc.toObject<Lesson>()?.copy(id = doc.id)
                println("DEBUG: Leçon trouvée par ID '$lessonId': ${lesson?.title}")
                lesson
            } else {
                println("DEBUG: Aucune leçon trouvée avec l'ID: '$lessonId'")
                null
            }
        } catch (e: Exception) {
            errorHandler.logError(e, "TheoryRepository#getLessonById", ErrorSeverity.MEDIUM)
            println("DEBUG: Erreur lors de la recherche par ID '$lessonId': ${e.message}")
            null
        }
    }

    suspend fun getLessonsForSubject(license: String, subject: String): List<Lesson> {
        return try {
            var query: Query = db.collection("lessons")
                .whereEqualTo("license", license)

            if (subject != "all") {
                query = query.whereEqualTo("subject", subject)
            }

            val snapshot = NetworkRetry.retryWithBackoff(maxRetries = 3, initialDelay = 800L) {
                Result.success(query.orderBy("orderIndex", Query.Direction.ASCENDING).get().await())
            }.getOrThrow()

            snapshot.documents.mapNotNull { doc ->
                try { doc.toObject<Lesson>()?.copy(id = doc.id) }
                catch (e: Exception) { errorHandler.logError(e, "TheoryRepository#getLessonsForSubject", ErrorSeverity.LOW); null }
            }.filter { DataValidator.isValidLesson(it) }
        } catch (e: Exception) {
            errorHandler.logError(e, "TheoryRepository#getLessonsForSubject", ErrorSeverity.MEDIUM)
            emptyList()
        }
    }

    suspend fun getLessonsForTheoryCategory(category: String, subject: String): List<Lesson> {
        return try {
            // D'abord, récupérer toutes les leçons avec la bonne license
            val snapshot = NetworkRetry.retryWithBackoff(maxRetries = 3, initialDelay = 800L) {
                Result.success(
                    db.collection("lessons")
                .whereEqualTo("license", category)
                .get()
                .await()
                )
            }.getOrThrow()

            val lessons = snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: emptyMap()
                    val docSubject = data["subject"] as? String ?: ""
                    
                    // Filtrer par sujet côté client si nécessaire
                    if (subject != "all" && docSubject != subject) {
                        return@mapNotNull null
                    }
                    
                    doc.toObject<Lesson>()?.copy(id = doc.id)
                } catch (e: Exception) {
                    errorHandler.logError(e, "TheoryRepository#getLessonsForTheoryCategory", ErrorSeverity.LOW)
                    null
                }
            }.filter { DataValidator.isValidLesson(it) }.sortedBy { it.orderIndex }

            lessons
        } catch (e: Exception) {
            errorHandler.logError(e, "TheoryRepository#getLessonsForTheoryCategory", ErrorSeverity.MEDIUM)
            println("DEBUG: Erreur lors de la recherche pour catégorie '$category': ${e.message}")
            emptyList()
        }
    }
}