package com.pilotprep.app.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestionnaire centralisé d'erreurs pour l'application
 */
@Singleton
class ErrorHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val structuredLogger: StructuredLogger
) {
    private val _errorEvents = MutableStateFlow<List<ErrorEvent>>(emptyList())
    val errorEvents = _errorEvents.asStateFlow()
    
    private val maxStoredErrors = 50
    
    /**
     * Log un message de debug
     */
    fun logDebug(context: String, message: String) {
        structuredLogger.debug(context, message)
    }
    
    /**
     * Log un message d'info
     */
    fun logInfo(context: String, message: String) {
        structuredLogger.info(context, message)
    }
    
    /**
     * Enregistre une erreur avec contexte
     */
    fun logError(
        error: Throwable,
        context: String = "",
        severity: ErrorSeverity = ErrorSeverity.MEDIUM,
        userAction: String? = null
    ) {
        val errorEvent = ErrorEvent(
            error = error,
            context = context,
            severity = severity,
            userAction = userAction,
            timestamp = System.currentTimeMillis()
        )
        
        // Structured log
        val fields = mapOf(
            "severity" to severity.name,
            "user_action" to userAction,
            "exception" to (error.javaClass.simpleName + ": " + (error.message ?: ""))
        )
        when (severity) {
            ErrorSeverity.LOW -> structuredLogger.debug(context, "error", fields)
            ErrorSeverity.MEDIUM -> structuredLogger.warn(context, "error", fields)
            ErrorSeverity.HIGH -> structuredLogger.error(context, "error", fields, error)
            ErrorSeverity.CRITICAL -> structuredLogger.wtf(context, "critical_error", fields, error)
        }
        
        // Ajouter à la liste des erreurs
        val currentErrors = _errorEvents.value.toMutableList()
        currentErrors.add(errorEvent)
        
        // Garder seulement les erreurs récentes
        if (currentErrors.size > maxStoredErrors) {
            currentErrors.removeAt(0)
        }
        
        _errorEvents.value = currentErrors
        
        // Actions spécifiques selon la sévérité
        when (severity) {
            ErrorSeverity.CRITICAL -> handleCriticalError(errorEvent)
            ErrorSeverity.HIGH -> handleHighSeverityError(errorEvent)
            else -> { /* Pas d'action spéciale */ }
        }
    }
    
    /**
     * Gère les erreurs critiques
     */
    private fun handleCriticalError(errorEvent: ErrorEvent) {
        // Actions pour les erreurs critiques
        // Par exemple : envoyer un rapport de crash, redémarrer l'app, etc.
        structuredLogger.wtf("ErrorHandler", "critical_error_detected", mapOf("message" to (errorEvent.error.message ?: "")), errorEvent.error)
    }
    
    /**
     * Gère les erreurs de haute sévérité
     */
    private fun handleHighSeverityError(errorEvent: ErrorEvent) {
        // Actions pour les erreurs importantes
        structuredLogger.error("ErrorHandler", "high_severity_error", mapOf("message" to (errorEvent.error.message ?: "")), errorEvent.error)
    }
    
    /**
     * Obtient les erreurs récentes
     */
    fun getRecentErrors(limit: Int = 10): List<ErrorEvent> {
        return _errorEvents.value.takeLast(limit)
    }
    
    /**
     * Obtient les erreurs par sévérité
     */
    fun getErrorsBySeverity(severity: ErrorSeverity): List<ErrorEvent> {
        return _errorEvents.value.filter { it.severity == severity }
    }
    
    /**
     * Nettoie les erreurs anciennes
     */
    fun cleanupOldErrors() {
        val currentTime = System.currentTimeMillis()
        val oneHourAgo = currentTime - (60 * 60 * 1000L)
        
        val filteredErrors = _errorEvents.value.filter { it.timestamp > oneHourAgo }
        _errorEvents.value = filteredErrors
    }
    
    /**
     * Nettoie toutes les erreurs
     */
    fun clearAllErrors() {
        _errorEvents.value = emptyList()
    }
    
    /**
     * Obtient un rapport d'erreurs
     */
    fun getErrorReport(): ErrorReport {
        val errors = _errorEvents.value
        val criticalErrors = errors.count { it.severity == ErrorSeverity.CRITICAL }
        val highErrors = errors.count { it.severity == ErrorSeverity.HIGH }
        val mediumErrors = errors.count { it.severity == ErrorSeverity.MEDIUM }
        val lowErrors = errors.count { it.severity == ErrorSeverity.LOW }
        
        return ErrorReport(
            totalErrors = errors.size,
            criticalErrors = criticalErrors,
            highErrors = highErrors,
            mediumErrors = mediumErrors,
            lowErrors = lowErrors,
            recentErrors = errors.takeLast(5)
        )
    }
}

/**
 * Événement d'erreur
 */
data class ErrorEvent(
    val error: Throwable,
    val context: String,
    val severity: ErrorSeverity,
    val userAction: String?,
    val timestamp: Long
)

/**
 * Niveaux de sévérité des erreurs
 */
enum class ErrorSeverity {
    LOW,       // Erreurs mineures, pas d'impact utilisateur
    MEDIUM,    // Erreurs modérées, impact limité
    HIGH,      // Erreurs importantes, impact significatif
    CRITICAL   // Erreurs critiques, application peut crasher
}

/**
 * Rapport d'erreurs
 */
data class ErrorReport(
    val totalErrors: Int,
    val criticalErrors: Int,
    val highErrors: Int,
    val mediumErrors: Int,
    val lowErrors: Int,
    val recentErrors: List<ErrorEvent>
)

/**
 * Extensions pour faciliter l'utilisation
 */
fun ErrorHandler.logNetworkError(error: Throwable, operation: String) {
    logError(error, "Network: $operation", ErrorSeverity.MEDIUM)
}

fun ErrorHandler.logDatabaseError(error: Throwable, operation: String) {
    logError(error, "Database: $operation", ErrorSeverity.HIGH)
}

fun ErrorHandler.logFirebaseError(error: Throwable, operation: String) {
    logError(error, "Firebase: $operation", ErrorSeverity.MEDIUM)
}

fun ErrorHandler.logUIError(error: Throwable, screen: String) {
    logError(error, "UI: $screen", ErrorSeverity.LOW)
}
