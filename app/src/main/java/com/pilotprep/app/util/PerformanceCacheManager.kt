package com.pilotprep.app.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestionnaire de cache simple pour optimiser les performances
 */
@Singleton
class PerformanceCacheManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Cache simple en mémoire
    private val cache = mutableMapOf<String, Any>()

    fun <T> get(key: String, cacheType: CacheType = CacheType.GENERIC): T? {
        @Suppress("UNCHECKED_CAST")
        return cache[key] as? T
    }

    fun <T> put(key: String, value: T, cacheType: CacheType = CacheType.GENERIC) {
        cache[key] = value as Any
    }

    fun remove(key: String, cacheType: CacheType = CacheType.GENERIC) {
        cache.remove(key)
    }

    fun clear(cacheType: CacheType = CacheType.GENERIC) {
        when (cacheType) {
            CacheType.AIRCRAFT, CacheType.IMAGE, CacheType.GENERIC, CacheType.ALL -> {
                cache.clear()
            }
        }
    }
}

enum class CacheType {
    AIRCRAFT, IMAGE, GENERIC, ALL
}