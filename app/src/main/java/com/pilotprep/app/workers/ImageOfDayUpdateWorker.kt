package com.pilotprep.app.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pilotprep.app.data.repository.ImageOfDayRepository
import com.pilotprep.app.util.PerformanceMonitor
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Worker optimisé pour la mise à jour quotidienne de l'image du jour
 */
@HiltWorker
class ImageOfDayUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val imageOfDayRepository: ImageOfDayRepository,
    private val performanceMonitor: PerformanceMonitor
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "ImageOfDayUpdateWorker"
        private const val MAX_RETRY_ATTEMPTS = 3
    }

    override suspend fun doWork(): Result {
        return performanceMonitor.measureOperation("ImageOfDayUpdateWorker") {
            try {
                Log.d(TAG, "🚀 Début de la mise à jour de l'image du jour")
                
                val today = LocalDate.now()
                val todayFormatted = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
                
                // Vérifier si l'image du jour est déjà à jour
                val currentImage = imageOfDayRepository.getCurrentImageOfDay()
                val needsUpdate = currentImage == null || 
                    !isImageFromToday(currentImage, todayFormatted)
                
                if (!needsUpdate) {
                    Log.d(TAG, "✅ Image du jour déjà à jour pour $todayFormatted")
                    return@measureOperation Result.success()
                }
                
                // Récupérer tous les avions disponibles
                val allAircraft = imageOfDayRepository.getAllAircraftForImageOfDay()
                
                if (allAircraft.isEmpty()) {
                    Log.w(TAG, "⚠️ Aucun avion disponible pour l'image du jour")
                    return@measureOperation Result.failure()
                }
                
                // Sélectionner un avion aléatoire
                val selectedAircraft = allAircraft.random()
                Log.d(TAG, "🎯 Avion sélectionné: ${selectedAircraft.name}")
                
                // Mettre à jour l'image du jour
                val updateResult = imageOfDayRepository.updateImageOfDay(selectedAircraft)
                
                updateResult.fold(
                    onSuccess = {
                        Log.i(TAG, "✅ Image du jour mise à jour avec succès: ${selectedAircraft.name}")
                        return@measureOperation Result.success()
                    },
                    onFailure = { error ->
                        Log.e(TAG, "❌ Échec de la mise à jour de l'image du jour: ${error.message}")
                        return@measureOperation Result.failure()
                    }
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "💥 Erreur lors de la mise à jour de l'image du jour: ${e.message}", e)
                
                // Retry logic pour les erreurs temporaires
                if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
                    Log.w(TAG, "🔄 Tentative de retry ${runAttemptCount + 1}/$MAX_RETRY_ATTEMPTS")
                    Result.retry()
                } else {
                    Log.e(TAG, "❌ Nombre maximum de tentatives atteint")
                    Result.failure()
                }
            }
        }
    }
    
    /**
     * Vérifie si l'image est de la date spécifiée
     */
    private fun isImageFromToday(image: Any, todayFormatted: String): Boolean {
        // Implémentation dépendante de votre modèle ImageOfDay
        // Ici on suppose qu'il y a un champ date ou timestamp
        return try {
            // Logique de vérification de date
            // À adapter selon votre modèle de données
            true // Placeholder
        } catch (e: Exception) {
            Log.w(TAG, "Erreur lors de la vérification de date: ${e.message}")
            false
        }
    }
}



















