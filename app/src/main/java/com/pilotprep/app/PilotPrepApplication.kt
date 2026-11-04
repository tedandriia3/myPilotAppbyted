package com.pilotprep.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil.Coil
import coil.ImageLoader
import com.pilotprep.app.workers.ImageOfDayWorkerManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class PilotPrepApplication : Application(), Configuration.Provider {
    
    @Inject
    lateinit var imageOfDayWorkerManager: ImageOfDayWorkerManager
    @Inject
    lateinit var imageLoader: ImageLoader
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    override fun onCreate() {
        super.onCreate()
        
        // Programmer la mise à jour quotidienne de l'image du jour
        try {
            imageOfDayWorkerManager.scheduleDailyImageUpdate()
            android.util.Log.d("PilotPrepApp", "✅ Worker activé avec succès")
        } catch (e: Exception) {
            android.util.Log.e("PilotPrepApp", "❌ Erreur lors de l'activation du Worker: ${e.message}")
            // Ne pas crasher l'app si le Worker échoue
        }

        // Définir l'ImageLoader global pour Coil
        Coil.setImageLoader(imageLoader)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}