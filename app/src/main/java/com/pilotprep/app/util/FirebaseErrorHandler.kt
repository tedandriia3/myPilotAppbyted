package com.pilotprep.app.util

import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException

/**
 * Gestionnaire d'erreurs Firebase amélioré
 */
object FirebaseErrorHandler {
    
    private const val TAG = "FirebaseErrorHandler"
    
    sealed class ErrorType {
        object Network : ErrorType()
        object Authentication : ErrorType()
        object Permission : ErrorType()
        object NotFound : ErrorType()
        object RateLimit : ErrorType()
        object Unknown : ErrorType()
    }
    
    /**
     * Analyse une exception Firebase et retourne le type d'erreur
     */
    fun analyzeError(exception: Throwable): ErrorType {
        return when (exception) {
            is FirebaseAuthException -> {
                Log.e(TAG, "Erreur d'authentification: ${exception.errorCode}")
                ErrorType.Authentication
            }
            is FirebaseFirestoreException -> {
                when (exception.code) {
                    FirebaseFirestoreException.Code.NOT_FOUND -> {
                        Log.e(TAG, "Document non trouvé")
                        ErrorType.NotFound
                    }
                    FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                        Log.e(TAG, "Permission refusée")
                        ErrorType.Permission
                    }
                    FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED -> {
                        Log.e(TAG, "Limite de débit atteinte")
                        ErrorType.RateLimit
                    }
                    FirebaseFirestoreException.Code.UNAVAILABLE -> {
                        Log.e(TAG, "Service indisponible")
                        ErrorType.Network
                    }
                    else -> {
                        Log.e(TAG, "Erreur Firestore: ${exception.code}")
                        ErrorType.Unknown
                    }
                }
            }
            is java.net.UnknownHostException,
            is java.net.SocketTimeoutException,
            is java.io.IOException -> {
                Log.e(TAG, "Erreur réseau: ${exception.message}")
                ErrorType.Network
            }
            is FirebaseException -> {
                Log.e(TAG, "Erreur Firebase: ${exception.message}")
                ErrorType.Unknown
            }
            else -> {
                Log.e(TAG, "Erreur inconnue: ${exception.message}")
                ErrorType.Unknown
            }
        }
    }
    
    /**
     * Vérifie si les services Google Play sont disponibles
     */
    fun arePlayServicesAvailable(context: android.content.Context): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(context)
        
        return when (resultCode) {
            ConnectionResult.SUCCESS -> {
                Log.d(TAG, "✅ Google Play Services disponibles")
                true
            }
            ConnectionResult.SERVICE_UPDATING -> {
                Log.w(TAG, "⏳ Service Google Play en cours de mise à jour")
                false
            }
            ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> {
                Log.e(TAG, "❌ Google Play Services nécessite une mise à jour")
                false
            }
            else -> {
                Log.e(TAG, "❌ Google Play Services indisponible (code: $resultCode)")
                false
            }
        }
    }
    
    /**
     * Obtient un message d'erreur user-friendly
     */
    fun getUserFriendlyMessage(errorType: ErrorType): String {
        return when (errorType) {
            ErrorType.Network -> "Problème de connexion. Vérifiez votre connexion internet."
            ErrorType.Authentication -> "Erreur d'authentification. Réessayez."
            ErrorType.Permission -> "Vous n'avez pas la permission d'accéder à ces données."
            ErrorType.NotFound -> "Contenu non trouvé. Il a peut-être été supprimé."
            ErrorType.RateLimit -> "Trop de requêtes. Veuillez attendre un moment."
            ErrorType.Unknown -> "Une erreur s'est produite. Réessayez plus tard."
        }
    }
    
    /**
     * Logue les détails d'une erreur pour le débogage
     */
    fun logErrorDetails(exception: Throwable, context: String) {
        Log.e(TAG, "=== ERREUR FIREBASE ===")
        Log.e(TAG, "Contexte: $context")
        Log.e(TAG, "Type: ${exception.javaClass.simpleName}")
        Log.e(TAG, "Message: ${exception.message}")
        
        if (exception is FirebaseFirestoreException) {
            Log.e(TAG, "Code: ${exception.code}")
            Log.e(TAG, "Cause: ${exception.cause}")
        }
        
        if (exception is FirebaseAuthException) {
            Log.e(TAG, "Code erreur: ${exception.errorCode}")
        }
        
        Log.e(TAG, "====================")
    }
}




