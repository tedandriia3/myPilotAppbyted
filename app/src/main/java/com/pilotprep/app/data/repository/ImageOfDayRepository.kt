package com.pilotprep.app.data.repository

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pilotprep.app.data.model.Aircraft
import com.pilotprep.app.data.model.DailyImageConfig
import com.pilotprep.app.data.model.ImageOfDay
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

interface ImageOfDayRepository {
    suspend fun getCurrentImageOfDay(): Aircraft?
    suspend fun getImageOfDayForDate(date: LocalDate): Aircraft?
    suspend fun updateImageOfDay(aircraft: Aircraft): Result<Unit>
    suspend fun getDailyImageConfig(): DailyImageConfig
    suspend fun updateDailyImageConfig(config: DailyImageConfig): Result<Unit>
    suspend fun getAllAircraftForImageOfDay(): List<Aircraft>
    suspend fun getImageOfDayHistory(): List<ImageOfDay>
    suspend fun addImageOfDayEntry(entry: ImageOfDay): Result<Unit>
}

@Singleton
class ImageOfDayRepositoryImpl @Inject constructor(
    private val aircraftRepository: AircraftRepository
) : ImageOfDayRepository {

    private val db = Firebase.firestore
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val CONFIG_DOC_ID = "daily_image_config"
    private val IMAGE_OF_DAY_COLLECTION = "image_of_day"

    override suspend fun getCurrentImageOfDay(): Aircraft? {
        val today = LocalDate.now()
        return getImageOfDayForDate(today)
    }

    override suspend fun getImageOfDayForDate(date: LocalDate): Aircraft? {
        val config = getDailyImageConfig()
        val dateString = date.format(dateFormatter)
        
        // Si la configuration n'est pas à jour pour aujourd'hui, on sélectionne une nouvelle image
        if (config.lastUpdated != dateString) {
            val newAircraft = selectNewImageOfDay()
            if (newAircraft != null) {
                updateDailyImageConfig(
                    config.copy(
                        currentImageId = newAircraft.id,
                        lastUpdated = dateString
                    )
                )
                return newAircraft
            }
        }
        
        // Retourner l'avion actuel si disponible
        return aircraftRepository.getAircraftById(config.currentImageId)
    }

    override suspend fun updateImageOfDay(aircraft: Aircraft): Result<Unit> {
        return try {
            val today = LocalDate.now()
            val config = DailyImageConfig(
                currentImageId = aircraft.id,
                lastUpdated = today.format(dateFormatter)
            )
            updateDailyImageConfig(config)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDailyImageConfig(): DailyImageConfig {
        return try {
            val doc = db.collection("config").document(CONFIG_DOC_ID).get().await()
            if (doc.exists()) {
                doc.toObject(DailyImageConfig::class.java) ?: DailyImageConfig()
            } else {
                DailyImageConfig()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            DailyImageConfig()
        }
    }

    override suspend fun updateDailyImageConfig(config: DailyImageConfig): Result<Unit> {
        return try {
            db.collection("config").document(CONFIG_DOC_ID).set(config).await()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getAllAircraftForImageOfDay(): List<Aircraft> {
        return aircraftRepository.getAllAircraft()
    }

    override suspend fun getImageOfDayHistory(): List<ImageOfDay> {
        return try {
            val snapshot = db.collection(IMAGE_OF_DAY_COLLECTION)
                .orderBy("displayDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(30) // Limiter à 30 entrées récentes
                .get().await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(ImageOfDay::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun addImageOfDayEntry(entry: ImageOfDay): Result<Unit> {
        return try {
            db.collection(IMAGE_OF_DAY_COLLECTION).add(entry).await()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private suspend fun selectNewImageOfDay(): Aircraft? {
        val allAircraft = getAllAircraftForImageOfDay()
        if (allAircraft.isEmpty()) return null
        
        // Stratégie simple : sélection aléatoire
        // Dans une vraie implémentation, on pourrait éviter de répéter les images récentes
        return allAircraft.random()
    }
}
