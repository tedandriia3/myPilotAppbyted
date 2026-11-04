package com.pilotprep.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.pilotprep.app.MainActivity
import com.pilotprep.app.R
import com.pilotprep.app.util.SessionManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudyReminderManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionManager: SessionManager
) {
    
    companion object {
        const val CHANNEL_ID_STUDY_REMINDER = "study_reminder"
        const val CHANNEL_ID_QUIZ_REMINDER = "quiz_reminder"
        const val CHANNEL_ID_WEEKLY_TIP = "weekly_tip"
        
        const val NOTIFICATION_ID_STUDY = 1001
        const val NOTIFICATION_ID_QUIZ = 1002
        const val NOTIFICATION_ID_TIP = 1003
    }
    
    private val notificationManager = NotificationManagerCompat.from(context)
    
    init {
        createNotificationChannels()
    }
    
    /**
     * Crée les canaux de notification
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Canal pour les rappels d'étude
            val studyChannel = NotificationChannel(
                CHANNEL_ID_STUDY_REMINDER,
                "Rappels d'Étude",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications pour vous rappeler d'étudier"
                enableVibration(true)
                setShowBadge(true)
            }
            
            // Canal pour les rappels de quiz
            val quizChannel = NotificationChannel(
                CHANNEL_ID_QUIZ_REMINDER,
                "Rappels de Quiz",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications pour les quiz et évaluations"
                enableVibration(true)
                setShowBadge(true)
            }
            
            // Canal pour les astuces hebdomadaires
            val tipChannel = NotificationChannel(
                CHANNEL_ID_WEEKLY_TIP,
                "Astuces Hebdomadaires",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications pour les nouvelles astuces"
                enableVibration(false)
                setShowBadge(false)
            }
            
            systemNotificationManager.createNotificationChannels(
                listOf(studyChannel, quizChannel, tipChannel)
            )
        }
    }
    
    /**
     * Programme un rappel d'étude quotidien
     */
    fun scheduleDailyStudyReminder(hour: Int, minute: Int) {
        val workRequest = PeriodicWorkRequestBuilder<StudyReminderWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(calculateDelayToNextReminder(hour, minute), TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .addTag("daily_study_reminder")
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "daily_study_reminder",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }
    
    /**
     * Programme un rappel de quiz hebdomadaire
     */
    fun scheduleWeeklyQuizReminder(dayOfWeek: Int, hour: Int, minute: Int) {
        val workRequest = PeriodicWorkRequestBuilder<QuizReminderWorker>(
            7, TimeUnit.DAYS
        )
            .setInitialDelay(calculateDelayToNextReminder(hour, minute), TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag("weekly_quiz_reminder")
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "weekly_quiz_reminder",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }
    
    /**
     * Programme une notification pour la nouvelle astuce hebdomadaire
     */
    fun scheduleWeeklyTipNotification() {
        val workRequest = OneTimeWorkRequestBuilder<WeeklyTipNotificationWorker>()
            .setInitialDelay(1, TimeUnit.HOURS) // Notifier 1h après le changement de semaine
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag("weekly_tip_notification")
            .build()
        
        WorkManager.getInstance(context).enqueueUniqueWork(
            "weekly_tip_notification",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
    
    /**
     * Annule tous les rappels programmés
     */
    fun cancelAllReminders() {
        WorkManager.getInstance(context).cancelAllWorkByTag("daily_study_reminder")
        WorkManager.getInstance(context).cancelAllWorkByTag("weekly_quiz_reminder")
        WorkManager.getInstance(context).cancelAllWorkByTag("weekly_tip_notification")
    }
    
    /**
     * Calcule le délai jusqu'au prochain rappel
     */
    private fun calculateDelayToNextReminder(hour: Int, minute: Int): Long {
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, hour)
        calendar.set(java.util.Calendar.MINUTE, minute)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        
        val targetTime = calendar.timeInMillis
        val delay = if (targetTime > now) {
            targetTime - now
        } else {
            targetTime + (24 * 60 * 60 * 1000) - now // Ajouter 24h
        }
        
        return delay
    }
    
    /**
     * Affiche une notification de rappel d'étude
     */
    suspend fun showStudyReminderNotification() {
        val session = sessionManager.sessionState.first()
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_STUDY_REMINDER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("📚 Temps d'étudier !")
            .setContentText("Il est temps de réviser vos connaissances en aviation")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Continuez votre apprentissage avec PilotPrep. " +
                        "Vous avez étudié ${session.formattedTotalStudyTime} cette semaine."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_STUDY, notification)
    }
    
    /**
     * Affiche une notification de rappel de quiz
     */
    fun showQuizReminderNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("action", "open_quiz")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_QUIZ_REMINDER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🎯 Quiz de la Semaine")
            .setContentText("Testez vos connaissances avec le quiz hebdomadaire")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Un nouveau quiz vous attend ! " +
                        "Mesurez vos progrès et identifiez les domaines à améliorer."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_QUIZ, notification)
    }
    
    /**
     * Affiche une notification pour la nouvelle astuce
     */
    fun showWeeklyTipNotification(tipTitle: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("action", "open_weekly_tip")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_WEEKLY_TIP)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("💡 Nouvelle Astuce")
            .setContentText(tipTitle)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Découvrez la nouvelle astuce de la semaine : $tipTitle"))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_TIP, notification)
    }
}

/**
 * Worker pour les rappels d'étude quotidiens
 */
@HiltWorker
class StudyReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val reminderManager: StudyReminderManager
) : Worker(context, params) {
    
    override fun doWork(): Result {
        return try {
            runBlocking {
                reminderManager.showStudyReminderNotification()
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}

/**
 * Worker pour les rappels de quiz hebdomadaires
 */
@HiltWorker
class QuizReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val reminderManager: StudyReminderManager
) : Worker(context, params) {
    
    override fun doWork(): Result {
        return try {
            runBlocking {
                reminderManager.showQuizReminderNotification()
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}

/**
 * Worker pour les notifications d'astuces hebdomadaires
 */
@HiltWorker
class WeeklyTipNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val reminderManager: StudyReminderManager
) : Worker(context, params) {
    
    override fun doWork(): Result {
        return try {
            runBlocking {
                val tipTitle = inputData.getString("tip_title") ?: "Astuce de la Semaine"
                reminderManager.showWeeklyTipNotification(tipTitle)
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
