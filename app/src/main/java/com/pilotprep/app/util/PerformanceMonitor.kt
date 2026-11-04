package com.pilotprep.app.util

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Moniteur de performance optimisé pour l'application
 */
@Singleton
class PerformanceMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val performanceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val performanceMetrics = mutableMapOf<String, PerformanceMetric>()
    
    // Seuils de performance optimisés
    private val SLOW_OPERATION_THRESHOLD = 500L // 500ms
    private val VERY_SLOW_OPERATION_THRESHOLD = 2000L // 2 secondes
    private val CRITICAL_OPERATION_THRESHOLD = 5000L // 5 secondes
    
    // Cache pour éviter les recalculs
    private val metricCache = mutableMapOf<String, CachedMetric>()
    
    init {
        startPerformanceMonitoring()
    }

    /**
     * Démarre la surveillance des performances avec optimisations
     */
    private fun startPerformanceMonitoring() {
        performanceScope.launch {
            while (isActive) {
                try {
                    logPerformanceMetrics()
                    cleanupOldMetrics()
                    delay(60_000) // Log toutes les minutes
                } catch (e: Exception) {
                    Log.e("PerformanceMonitor", "Erreur dans le monitoring: ${e.message}")
                }
            }
        }
    }

    /**
     * Mesure le temps d'exécution d'une opération avec cache intelligent
     */
    suspend fun <T> measureOperation(
        operationName: String,
        operation: suspend () -> T
    ): T {
        val startTime = System.currentTimeMillis()
        var success = true
        
        return try {
            val result = operation()
            result
        } catch (e: Exception) {
            success = false
            Log.e("PerformanceMonitor", "Erreur dans $operationName: ${e.message}")
            throw e
        } finally {
            val duration = System.currentTimeMillis() - startTime
            recordOperation(operationName, duration, success)
            
            // Alerte immédiate pour les opérations critiques
            if (duration > CRITICAL_OPERATION_THRESHOLD) {
                Log.wtf("Performance", "🚨 OPÉRATION CRITIQUE: $operationName - ${duration}ms")
            }
        }
    }

    /**
     * Enregistre une opération avec optimisations
     */
    private fun recordOperation(operationName: String, duration: Long, success: Boolean) {
        val metric = performanceMetrics.getOrPut(operationName) { 
            PerformanceMetric(operationName) 
        }
        metric.addMeasurement(duration, success)
        
        // Mise à jour du cache
        metricCache[operationName] = CachedMetric(
            averageDuration = metric.averageDuration,
            failureRate = metric.failureRate,
            lastUpdate = System.currentTimeMillis()
        )
        
        // Alerter si l'opération est lente
        when {
            duration > CRITICAL_OPERATION_THRESHOLD -> {
                Log.wtf("Performance", "🚨 Opération critique: $operationName - ${duration}ms")
            }
            duration > VERY_SLOW_OPERATION_THRESHOLD -> {
                Log.w("Performance", "⚠️ Opération très lente: $operationName - ${duration}ms")
            }
            duration > SLOW_OPERATION_THRESHOLD -> {
                Log.i("Performance", "🐌 Opération lente: $operationName - ${duration}ms")
            }
        }
    }

    /**
     * Obtient les métriques de performance avec cache
     */
    fun getPerformanceMetrics(): Map<String, PerformanceMetric> {
        return performanceMetrics.toMap()
    }

    /**
     * Obtient un rapport de performance optimisé
     */
    fun getPerformanceReport(): PerformanceReport {
        val slowOperations = performanceMetrics.values.filter { 
            it.averageDuration > SLOW_OPERATION_THRESHOLD 
        }
        
        val failedOperations = performanceMetrics.values.filter { 
            it.failureRate > 0.05f // Plus de 5% d'échec
        }
        
        val criticalOperations = performanceMetrics.values.filter {
            it.averageDuration > CRITICAL_OPERATION_THRESHOLD
        }
        
        return PerformanceReport(
            slowOperations = slowOperations,
            failedOperations = failedOperations,
            criticalOperations = criticalOperations,
            totalOperations = performanceMetrics.size,
            averageResponseTime = performanceMetrics.values.map { it.averageDuration }.average().toLong()
        )
    }

    /**
     * Log les métriques de performance avec formatage amélioré
     */
    private fun logPerformanceMetrics() {
        val report = getPerformanceReport()
        
        Log.d("Performance", "📊 === RAPPORT DE PERFORMANCE ===")
        Log.d("Performance", "📈 Opérations totales: ${report.totalOperations}")
        Log.d("Performance", "🐌 Opérations lentes: ${report.slowOperations.size}")
        Log.d("Performance", "❌ Opérations en échec: ${report.failedOperations.size}")
        Log.d("Performance", "🚨 Opérations critiques: ${report.criticalOperations.size}")
        Log.d("Performance", "⏱️ Temps de réponse moyen: ${report.averageResponseTime}ms")
        
        // Log des opérations les plus problématiques
        report.criticalOperations.sortedByDescending { it.averageDuration }
            .take(3)
            .forEach { metric ->
                Log.w("Performance", "🚨 CRITIQUE: ${metric.operationName} - ${metric.averageDuration}ms (${metric.callCount} appels, ${(metric.failureRate * 100).toInt()}% échec)")
            }
            
        report.slowOperations.sortedByDescending { it.averageDuration }
            .take(5)
            .forEach { metric ->
                Log.d("Performance", "🐌 Lent: ${metric.operationName} - ${metric.averageDuration}ms (${metric.callCount} appels)")
            }
    }

    /**
     * Nettoie les métriques anciennes avec optimisations
     */
    fun cleanupOldMetrics() {
        val currentTime = System.currentTimeMillis()
        val metricsToRemove = performanceMetrics.filter { 
            currentTime - it.value.lastCallTime > 24 * 60 * 60 * 1000L // 24h
        }.keys
        
        metricsToRemove.forEach { 
            performanceMetrics.remove(it)
            metricCache.remove(it)
        }
        
        // Nettoyer le cache aussi
        val cacheToRemove = metricCache.filter {
            currentTime - it.value.lastUpdate > 60 * 60 * 1000L // 1h
        }.keys
        
        cacheToRemove.forEach { metricCache.remove(it) }
    }

    /**
     * Obtient les métriques en temps réel pour une opération
     */
    fun getRealtimeMetrics(operationName: String): CachedMetric? {
        return metricCache[operationName]
    }

    /**
     * Force un nettoyage complet
     */
    fun forceCleanup() {
        performanceMetrics.clear()
        metricCache.clear()
        System.gc()
    }

    /**
     * Nettoie les ressources
     */
    fun cleanup() {
        performanceScope.cancel()
        performanceMetrics.clear()
        metricCache.clear()
    }
}

/**
 * Métrique de performance optimisée pour une opération
 */
data class PerformanceMetric(
    val operationName: String,
    private val measurements: MutableList<Measurement> = mutableListOf()
) {
    var lastCallTime: Long = 0L
        private set
    
    val callCount: Int get() = measurements.size
    val averageDuration: Long get() = if (measurements.isEmpty()) 0L else measurements.map { it.duration }.average().toLong()
    val failureRate: Float get() = if (measurements.isEmpty()) 0f else measurements.count { !it.success }.toFloat() / measurements.size
    val minDuration: Long get() = if (measurements.isEmpty()) 0L else measurements.minOf { it.duration }
    val maxDuration: Long get() = if (measurements.isEmpty()) 0L else measurements.maxOf { it.duration }
    
    fun addMeasurement(duration: Long, success: Boolean) {
        measurements.add(Measurement(duration, success))
        lastCallTime = System.currentTimeMillis()
        
        // Garder seulement les 200 dernières mesures pour plus de précision
        if (measurements.size > 200) {
            measurements.removeAt(0)
        }
    }
}

/**
 * Mesure individuelle
 */
data class Measurement(
    val duration: Long,
    val success: Boolean
)

/**
 * Rapport de performance amélioré
 */
data class PerformanceReport(
    val slowOperations: List<PerformanceMetric>,
    val failedOperations: List<PerformanceMetric>,
    val criticalOperations: List<PerformanceMetric>,
    val totalOperations: Int,
    val averageResponseTime: Long
)

/**
 * Métrique mise en cache pour les accès rapides
 */
data class CachedMetric(
    val averageDuration: Long,
    val failureRate: Float,
    val lastUpdate: Long
)