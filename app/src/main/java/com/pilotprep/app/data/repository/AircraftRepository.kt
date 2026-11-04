package com.pilotprep.app.data.repository

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pilotprep.app.data.model.Aircraft
import com.pilotprep.app.util.ErrorHandler
import com.pilotprep.app.util.ErrorSeverity
import com.pilotprep.app.util.NetworkRetry
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AircraftRepository @Inject constructor(
    private val errorHandler: ErrorHandler
) {
    private val db = Firebase.firestore

    suspend fun getAllAircraft(): List<Aircraft> {
        return try {
            val snapshot = NetworkRetry.retryWithBackoff(maxRetries = 3, initialDelay = 800L) {
                Result.success(db.collection("aircraft").get().await())
            }.getOrThrow()
            snapshot.documents.mapNotNull { doc ->
                try {
                    val aircraft = doc.toObject(Aircraft::class.java)
                    aircraft?.copy(id = doc.id)
                } catch (e: Exception) {
                    errorHandler.logError(e, "AircraftRepository#getAllAircraft", ErrorSeverity.LOW)
                    null
                }
            }
        } catch (e: Exception) {
            errorHandler.logError(e, "AircraftRepository#getAllAircraft", ErrorSeverity.MEDIUM)
            emptyList()
        }
    }

    suspend fun getAircraftById(id: String): Aircraft? {
        return try {
            val doc = NetworkRetry.retryWithBackoff(maxRetries = 3, initialDelay = 800L) {
                Result.success(db.collection("aircraft").document(id).get().await())
            }.getOrThrow()
            if (doc.exists()) doc.toObject(Aircraft::class.java)?.copy(id = doc.id) else null
        } catch (e: Exception) {
            errorHandler.logError(e, "AircraftRepository#getAircraftById", ErrorSeverity.MEDIUM)
            null
        }
    }
}