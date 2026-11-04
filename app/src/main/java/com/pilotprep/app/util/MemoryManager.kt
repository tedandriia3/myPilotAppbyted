package com.pilotprep.app.util

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import androidx.annotation.WorkerThread
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestionnaire de mémoire pour optimiser les performances
 */
@Singleton
class MemoryManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val memoryInfo = ActivityManager.MemoryInfo()
    
    // Seuils de mémoire
    private val LOW_MEMORY_THRESHOLD = 0.8f // 80% de mémoire utilisée
    private val CRITICAL_MEMORY_THRESHOLD = 0.9f // 90% de mémoire utilisée
    
    // Coroutine scope pour les tâches de nettoyage
    private val cleanupScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Listeners pour les événements de mémoire
    private val memoryListeners = mutableListOf<MemoryListener>()
    
    init {
        startMemoryMonitoring()
    }

    /**
     * Démarre la surveillance de la mémoire
     */
    private fun startMemoryMonitoring() {
        cleanupScope.launch {
            while (isActive) {
                checkMemoryUsage()
                delay(30_000) // Vérifier toutes les 30 secondes
            }
        }
    }

    /**
     * Vérifie l'utilisation de la mémoire et déclenche des actions si nécessaire
     */
    @WorkerThread
    private fun checkMemoryUsage() {
        activityManager.getMemoryInfo(memoryInfo)
        val memoryUsage = 1.0f - (memoryInfo.availMem.toFloat() / memoryInfo.totalMem.toFloat())
        
        when {
            memoryUsage >= CRITICAL_MEMORY_THRESHOLD -> {
                // Mémoire critique - nettoyage agressif
                performCriticalMemoryCleanup()
                notifyMemoryListeners(MemoryLevel.CRITICAL, memoryUsage)
            }
            memoryUsage >= LOW_MEMORY_THRESHOLD -> {
                // Mémoire faible - nettoyage modéré
                performModerateMemoryCleanup()
                notifyMemoryListeners(MemoryLevel.LOW, memoryUsage)
            }
            else -> {
                // Mémoire normale
                notifyMemoryListeners(MemoryLevel.NORMAL, memoryUsage)
            }
        }
    }

    /**
     * Nettoyage modéré de la mémoire
     */
    private fun performModerateMemoryCleanup() {
        // Forcer le garbage collection
        System.gc()
        
        // Nettoyer les caches non critiques
        // (implémentation dépendante de vos caches)
    }

    /**
     * Nettoyage critique de la mémoire
     */
    private fun performCriticalMemoryCleanup() {
        // Nettoyage agressif
        System.gc()
        
        // Vider les caches
        // (implémentation dépendante de vos caches)
        
        // Libérer les ressources non essentielles
        cleanupNonEssentialResources()
    }

    /**
     * Nettoie les ressources non essentielles
     */
    private fun cleanupNonEssentialResources() {
        // Implémentation spécifique selon vos besoins
        // Par exemple : vider les caches d'images, libérer les bitmaps, etc.
    }

    /**
     * Obtient les informations de mémoire
     */
    fun getMemoryInfo(): MemoryInfo {
        activityManager.getMemoryInfo(memoryInfo)
        val runtime = Runtime.getRuntime()
        
        return MemoryInfo(
            totalMemory = memoryInfo.totalMem,
            availableMemory = memoryInfo.availMem,
            usedMemory = memoryInfo.totalMem - memoryInfo.availMem,
            memoryUsagePercentage = 1.0f - (memoryInfo.availMem.toFloat() / memoryInfo.totalMem.toFloat()),
            heapSize = runtime.totalMemory(),
            heapFree = runtime.freeMemory(),
            heapUsed = runtime.totalMemory() - runtime.freeMemory(),
            isLowMemory = memoryInfo.lowMemory
        )
    }

    /**
     * Vérifie si l'appareil a peu de mémoire
     */
    fun isLowMemory(): Boolean {
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.lowMemory
    }

    /**
     * Force un nettoyage de la mémoire
     */
    fun forceMemoryCleanup() {
        cleanupScope.launch {
            performModerateMemoryCleanup()
        }
    }

    /**
     * Ajoute un listener pour les événements de mémoire
     */
    fun addMemoryListener(listener: MemoryListener) {
        memoryListeners.add(listener)
    }

    /**
     * Supprime un listener
     */
    fun removeMemoryListener(listener: MemoryListener) {
        memoryListeners.remove(listener)
    }

    /**
     * Notifie tous les listeners
     */
    private fun notifyMemoryListeners(level: MemoryLevel, usage: Float) {
        memoryListeners.forEach { listener ->
            try {
                listener.onMemoryLevelChanged(level, usage)
            } catch (e: Exception) {
                // Ignorer les erreurs des listeners
            }
        }
    }

    /**
     * Nettoie les ressources
     */
    fun cleanup() {
        cleanupScope.cancel()
        memoryListeners.clear()
    }
}

/**
 * Informations de mémoire
 */
data class MemoryInfo(
    val totalMemory: Long,
    val availableMemory: Long,
    val usedMemory: Long,
    val memoryUsagePercentage: Float,
    val heapSize: Long,
    val heapFree: Long,
    val heapUsed: Long,
    val isLowMemory: Boolean
) {
    val memoryUsageFormatted: String
        get() = "${(memoryUsagePercentage * 100).toInt()}%"
    
    val totalMemoryFormatted: String
        get() = formatBytes(totalMemory)
    
    val availableMemoryFormatted: String
        get() = formatBytes(availableMemory)
    
    val usedMemoryFormatted: String
        get() = formatBytes(usedMemory)
    
    private fun formatBytes(bytes: Long): String {
        val kb = bytes / 1024
        val mb = kb / 1024
        val gb = mb / 1024
        
        return when {
            gb > 0 -> "${gb}GB ${mb % 1024}MB"
            mb > 0 -> "${mb}MB ${kb % 1024}KB"
            else -> "${kb}KB"
        }
    }
}

/**
 * Niveaux de mémoire
 */
enum class MemoryLevel {
    NORMAL,
    LOW,
    CRITICAL
}

/**
 * Interface pour écouter les changements de mémoire
 */
interface MemoryListener {
    fun onMemoryLevelChanged(level: MemoryLevel, usage: Float)
}

