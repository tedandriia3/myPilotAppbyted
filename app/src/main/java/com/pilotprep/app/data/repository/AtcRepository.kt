package com.pilotprep.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pilotprep.app.data.model.AtcExercise
import com.pilotprep.app.util.ErrorHandler
import com.pilotprep.app.util.ErrorSeverity
import com.pilotprep.app.util.NetworkRetry
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AtcRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val errorHandler: ErrorHandler
) {
    suspend fun getExercises(type: String): List<AtcExercise> {
        return try {
            val snapshot = NetworkRetry.retryWithBackoff(maxRetries = 3, initialDelay = 800L) {
                Result.success(
                    db.collection("atc_exercises")
                        .whereEqualTo("type", type)
                        .orderBy("orderIndex", Query.Direction.ASCENDING)
                        .get().await()
                )
            }.getOrThrow()
            snapshot.documents.mapNotNull { doc ->
                try { doc.toObject(AtcExercise::class.java)?.copy(id = doc.id) }
                catch (e: Exception) { errorHandler.logError(e, "AtcRepository#getExercises", ErrorSeverity.LOW); null }
            }
        } catch (e: Exception) {
            errorHandler.logError(e, "AtcRepository#getExercises", ErrorSeverity.MEDIUM)
            emptyList()
        }
    }
}