package com.pilotprep.app.util

import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Optimiseur de performances pour l'application Pilot Prep
 */
object PerformanceOptimizer {
    
    private const val TAG = "PerformanceOptimizer"
    private val cacheMap = mutableMapOf<String, CacheEntry<*>>()
    private const val CACHE_EXPIRY_MS = 5 * 60 * 1000L // 5 minutes
    
    /**
     * Récupère une valeur du cache ou exécute la fonction
     */
    suspend fun <T> getOrFetch(
        key: String,
        fetchFunction: suspend () -> T,
        expiryMs: Long = CACHE_EXPIRY_MS
    ): T = withContext(Dispatchers.IO) {
        val cached = cacheMap[key] as? CacheEntry<T>
        
        if (cached != null && !cached.isExpired()) {
            Log.d(TAG, "✅ Cache hit: $key")
            cached.value
        } else {
            Log.d(TAG, "🔄 Cache miss: $key - fetch from source")
            val value = fetchFunction()
            cacheMap[key] = CacheEntry(value, System.currentTimeMillis() + expiryMs)
            value
        }
    }
    
    /**
     * Invalide le cache pour une clé spécifique
     */
    fun invalidateCache(key: String) {
        cacheMap.remove(key)
        Log.d(TAG, "🗑️ Cache invalidé: $key")
    }
    
    /**
     * Invalide tout le cache
     */
    fun clearCache() {
        cacheMap.clear()
        Log.d(TAG, "🗑️ Cache vidé")
    }
    
    /**
     * Nettoyer les entrées expirées
     */
    fun cleanExpiredEntries() {
        val now = System.currentTimeMillis()
        val expiredKeys = cacheMap.entries.filter { it.value.expiryTime < now }.map { it.key }
        expiredKeys.forEach { cacheMap.remove(it) }
        if (expiredKeys.isNotEmpty()) {
            Log.d(TAG, "🧹 ${expiredKeys.size} entrées expirées supprimées")
        }
    }
    
    /**
     * Stocke une entrée de cache
     */
    private data class CacheEntry<T>(
        val value: T,
        val expiryTime: Long
    ) {
        fun isExpired() = System.currentTimeMillis() >= expiryTime
    }
}

/**
 * Utilitaire pour éviter les recompositions inutiles
 */
@Composable
fun <T> rememberOptimized(calculation: () -> T): T {
    return remember { calculation() }
}

/**
 * Hook pour gérer le cycle de vie et nettoyer les ressources
 */
@Composable
fun LifecycleCleanup(onDestroy: () -> Unit) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                onDestroy()
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}

/**
 * Récupère et mémorise de manière optimisée
 */
@Composable
fun <T> rememberCached(
    cacheKey: Any,
    calculation: suspend () -> T
): State<T?> {
    return remember(key1 = cacheKey) {
        mutableStateOf<T?>(null)
    }.apply {
        if (value == null) {
            LaunchedEffect(cacheKey) {
                value = withContext(Dispatchers.IO) {
                    PerformanceOptimizer.getOrFetch(
                        key = cacheKey.toString(),
                        fetchFunction = { calculation() }
                    )
                }
            }
        }
    }
}
