package com.pilotprep.app.workers

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageOfDayWorkerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val workManager = WorkManager.getInstance(context)
    
    fun scheduleDailyImageUpdate() {
        // Programmer la mise à jour quotidienne à 6h00 du matin
        val updateTime = LocalTime.of(6, 0)
        val now = LocalTime.now()
        
        val delay = if (now.isBefore(updateTime)) {
            Duration.between(now, updateTime).toMinutes()
        } else {
            Duration.between(now, updateTime.plusHours(24)).toMinutes()
        }
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val imageOfDayWorkRequest = OneTimeWorkRequestBuilder<ImageOfDayUpdateWorker>()
            .setInitialDelay(delay, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .addTag("image_of_day_update")
            .build()
        
        // Annuler les travaux précédents avec le même tag
        workManager.cancelAllWorkByTag("image_of_day_update")
        
        // Programmer le nouveau travail
        workManager.enqueue(imageOfDayWorkRequest)
        
        // Programmer aussi un travail périodique pour s'assurer que l'image est mise à jour
        val periodicWorkRequest = PeriodicWorkRequestBuilder<ImageOfDayUpdateWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(delay, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .addTag("image_of_day_periodic")
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            "image_of_day_periodic",
            ExistingPeriodicWorkPolicy.REPLACE,
            periodicWorkRequest
        )
    }
    
    fun cancelAllImageOfDayWork() {
        workManager.cancelAllWorkByTag("image_of_day_update")
        workManager.cancelAllWorkByTag("image_of_day_periodic")
    }
}





















