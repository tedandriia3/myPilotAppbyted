package com.pilotprep.app.util

import android.util.Log
import kotlinx.coroutines.delay

/**
 * Utility pour implémenter une logique de retry avec backoff exponentiel
 * pour les opérations réseau qui peuvent échouer
 */
object NetworkRetry {
    
    private const val TAG = "NetworkRetry"
    
    /**
     * Exécute une opération avec retry et backoff exponentiel
     * 
     * @param maxRetries Nombre maximum de tentatives (par défaut: 3)
     * @param initialDelay Délai initial en ms (par défaut: 1000ms)
     * @param operation Opération à retry
     * @return Résultat de l'opération ou échec après toutes les tentatives
     */
    suspend fun <T> retryWithBackoff(
        maxRetries: Int = 3,
        initialDelay: Long = 1000L,
        operation: suspend () -> Result<T>
    ): Result<T> {
        var lastException: Exception? = null
        var attempt = 0
        
        repeat(maxRetries) {
            attempt = it + 1
            val result = try {
                operation()
            } catch (e: Exception) {
                lastException = e
                Log.w(TAG, "Tentative $attempt/$maxRetries échouée: ${e.message}")
                Result.failure(e)
            }
            
            if (result.isSuccess) {
                Log.d(TAG, "✅ Opération réussie après $attempt tentative(s)")
                return result
            }
            
            // Retry avec backoff exponentiel
            if (attempt < maxRetries) {
                val delayMs = initialDelay * (1 shl attempt) // Backoff exponentiel: 1s, 2s, 4s
                Log.d(TAG, "⏳ Nouvelle tentative dans ${delayMs}ms...")
                delay(delayMs)
            }
        }
        
        Log.e(TAG, "❌ Échec après $maxRetries tentatives")
        return lastException?.let { Result.failure(it) } 
            ?: Result.failure(Exception("Operation failed after $maxRetries retries"))
    }
    
    /**
     * Exécute une opération avec retry et backoff exponentiel, avec fonction de prédicat
     * 
     * @param maxRetries Nombre maximum de tentatives
     * @param initialDelay Délai initial en ms
     * @param retryIf Prédicat pour déterminer si on doit retry (par défaut: retry toujours)
     * @param operation Opération à retry
     * @return Résultat de l'opération ou échec après toutes les tentatives
     */
    suspend fun <T> retryWithBackoffConditional(
        maxRetries: Int = 3,
        initialDelay: Long = 1000L,
        retryIf: (Throwable) -> Boolean = { true },
        operation: suspend () -> Result<T>
    ): Result<T> {
        var lastException: Exception? = null
        var attempt = 0
        
        repeat(maxRetries) {
            attempt = it + 1
            val result = try {
                operation()
            } catch (e: Exception) {
                lastException = e
                
                // Vérifier si on doit retry cette erreur
                if (!retryIf(e)) {
                    Log.d(TAG, "❌ Erreur non retryable: ${e.message}")
                    return Result.failure(e)
                }
                
                Log.w(TAG, "Tentative $attempt/$maxRetries échouée: ${e.message}")
                Result.failure(e)
            }
            
            if (result.isSuccess) {
                Log.d(TAG, "✅ Opération réussie après $attempt tentative(s)")
                return result
            }
            
            // Retry avec backoff exponentiel
            if (attempt < maxRetries) {
                val delayMs = initialDelay * (1 shl attempt)
                Log.d(TAG, "⏳ Nouvelle tentative dans ${delayMs}ms...")
                delay(delayMs)
            }
        }
        
        Log.e(TAG, "❌ Échec après $maxRetries tentatives")
        return lastException?.let { Result.failure(it) } 
            ?: Result.failure(Exception("Operation failed after $maxRetries retries"))
    }
    
    /**
     * Retry seulement pour les erreurs réseau
     */
    fun isNetworkError(throwable: Throwable): Boolean {
        return when (throwable) {
            is java.net.UnknownHostException,
            is java.net.SocketTimeoutException,
            is java.io.IOException -> true
            else -> false
        }
    }
    
    /**
     * Retry seulement pour les erreurs Firebase
     */
    fun isFirebaseRetryableError(throwable: Throwable): Boolean {
        if (throwable is com.google.firebase.firestore.FirebaseFirestoreException) {
            return when (throwable.code) {
                com.google.firebase.firestore.FirebaseFirestoreException.Code.UNAVAILABLE,
                com.google.firebase.firestore.FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED,
                com.google.firebase.firestore.FirebaseFirestoreException.Code.DEADLINE_EXCEEDED,
                com.google.firebase.firestore.FirebaseFirestoreException.Code.INTERNAL,
                com.google.firebase.firestore.FirebaseFirestoreException.Code.ABORTED -> true
                else -> false
            }
        }
        return false
    }
}


