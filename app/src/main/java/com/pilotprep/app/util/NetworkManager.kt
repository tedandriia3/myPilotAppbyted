package com.pilotprep.app.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkManager constructor(
    @ApplicationContext private val context: Context
) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    /**
     * Vérifie si l'appareil est connecté à Internet
     */
    fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
    
    /**
     * Vérifie si la connexion est stable (WiFi ou Ethernet)
     */
    fun isStableConnection(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || 
               capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }
    
    /**
     * Surveille les changements de connectivité
     */
    fun observeNetworkStatus(): Flow<Boolean> = flow {
        while (true) {
            emit(isNetworkAvailable())
            delay(1000) // Vérifie toutes les secondes
        }
    }
}

/**
 * Gestionnaire d'erreurs réseau avec retry automatique
 */
@Singleton
class NetworkErrorHandler @Inject constructor(
    private val networkManager: NetworkManager
) {
    companion object {
        private const val TAG = "NetworkErrorHandler"
        private const val MAX_RETRIES = 3
        private const val RETRY_DELAY_MS = 1000L
    }
    
    /**
     * Exécute une opération réseau avec retry automatique
     */
    suspend fun <T> executeWithRetry(
        operation: suspend () -> T,
        onError: (NetworkError) -> Unit = {}
    ): Result<T> {
        var lastException: Exception? = null
        
        repeat(MAX_RETRIES) { attempt ->
            try {
                if (!networkManager.isNetworkAvailable()) {
                    throw IOException("Aucune connexion Internet disponible")
                }
                
                val result = operation()
                return Result.success(result)
                
            } catch (e: Exception) {
                lastException = e
                val networkError = mapExceptionToNetworkError(e)
                
                Log.w(TAG, "Tentative ${attempt + 1}/$MAX_RETRIES échouée: ${e.message}")
                
                if (attempt < MAX_RETRIES - 1) {
                    delay(RETRY_DELAY_MS * (attempt + 1)) // Backoff exponentiel
                } else {
                    onError(networkError)
                }
            }
        }
        
        return Result.failure(lastException ?: Exception("Erreur réseau inconnue"))
    }
    
    /**
     * Mappe les exceptions vers des erreurs réseau typées
     */
    private fun mapExceptionToNetworkError(exception: Exception): NetworkError {
        return when (exception) {
            is UnknownHostException -> NetworkError.NoConnection("Impossible de résoudre l'adresse")
            is IOException -> NetworkError.ConnectionTimeout("Timeout de connexion")
            is SecurityException -> NetworkError.SecurityError("Erreur de sécurité")
            else -> NetworkError.Unknown(exception.message ?: "Erreur inconnue")
        }
    }
}

/**
 * Types d'erreurs réseau
 */
sealed class NetworkError(val message: String) {
    data class NoConnection(val customMessage: String = "Aucune connexion Internet") : NetworkError(customMessage)
    data class ConnectionTimeout(val customMessage: String = "Timeout de connexion") : NetworkError(customMessage)
    data class SecurityError(val customMessage: String = "Erreur de sécurité") : NetworkError(customMessage)
    data class ServerError(val customMessage: String = "Erreur du serveur") : NetworkError(customMessage)
    data class Unknown(val customMessage: String) : NetworkError(customMessage)
}

/**
 * Extension pour les opérations réseau avec gestion d'erreurs
 */
suspend fun <T> safeNetworkCall(
    networkErrorHandler: NetworkErrorHandler,
    operation: suspend () -> T
): Result<T> {
    return networkErrorHandler.executeWithRetry(operation)
}
