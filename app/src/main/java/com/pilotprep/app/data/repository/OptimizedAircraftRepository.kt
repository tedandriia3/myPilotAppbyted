package com.pilotprep.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.pilotprep.app.data.model.Aircraft
import com.pilotprep.app.util.CacheType
import com.pilotprep.app.util.PerformanceCacheManager
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository optimisé pour les avions avec cache intelligent
 */
@Singleton
class OptimizedAircraftRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val cacheManager: PerformanceCacheManager
) {
    companion object {
        private const val AIRCRAFT_COLLECTION = "aircraft"
        private const val CACHE_KEY_ALL_AIRCRAFT = "all_aircraft"
    }

    /**
     * Récupère tous les avions avec cache intelligent
     */
    suspend fun getAllAircraft(forceRefresh: Boolean = false): List<Aircraft> {
        if (!forceRefresh) {
            val cachedData: List<Aircraft>? = cacheManager.get(CACHE_KEY_ALL_AIRCRAFT, CacheType.AIRCRAFT)
            if (cachedData != null) {
                return cachedData
            }
        }

        return try {
            val snapshot = db.collection(AIRCRAFT_COLLECTION).get().await()
            val aircraftList = snapshot.documents.mapNotNull { doc ->
                doc.toObject<Aircraft>()?.copy(id = doc.id)
            }
            cacheManager.put(CACHE_KEY_ALL_AIRCRAFT, aircraftList, CacheType.AIRCRAFT)
            aircraftList
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Récupère un avion par ID avec cache
     */
    suspend fun getAircraftById(id: String, forceRefresh: Boolean = false): Aircraft? {
        if (!forceRefresh) {
            val cachedData: Aircraft? = cacheManager.get(id, CacheType.AIRCRAFT)
            if (cachedData != null) {
                return cachedData
            }
        }

        return try {
            val doc = db.collection(AIRCRAFT_COLLECTION).document(id).get().await()
            if (doc.exists()) {
                val aircraft = doc.toObject<Aircraft>()?.copy(id = doc.id)
                if (aircraft != null) {
                    cacheManager.put(id, aircraft, CacheType.AIRCRAFT)
                }
                aircraft
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Invalide tout le cache des avions
     */
    fun invalidateAllAircraftCache() {
        cacheManager.clear(CacheType.AIRCRAFT)
    }
}
