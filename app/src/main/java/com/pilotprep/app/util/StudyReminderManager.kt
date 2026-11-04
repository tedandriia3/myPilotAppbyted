package com.pilotprep.app.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.Calendar

/**
 * Gestionnaire de rappels d'étude
 */
class StudyReminderManager(private val context: Context) {
    
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val sharedPreferences = context.getSharedPreferences("study_reminders", Context.MODE_PRIVATE)
    
    companion object {
        private const val TAG = "StudyReminderManager"
        private const val ACTION_REMINDER = "com.pilotprep.app.STUDY_REMINDER"
        
        // IDs pour différents types de rappels
        const val REMINDER_MORNING = 1
        const val REMINDER_AFTERNOON = 2
        const val REMINDER_EVENING = 3
    }
    
    /**
     * Configure un rappel quotidien
     */
    fun scheduleDailyReminder(reminderId: Int, hour: Int, minute: Int, enabled: Boolean) {
        if (!enabled) {
            cancelReminder(reminderId)
            return
        }
        
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            
            // Si l'heure est déjà passée aujourd'hui, programmer pour demain
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        
        val intent = Intent(context, StudyReminderReceiver::class.java).apply {
            action = ACTION_REMINDER
            putExtra("reminder_id", reminderId)
            putExtra("hour", hour)
            putExtra("minute", minute)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Utiliser setExactAndAllowWhileIdle pour API 23+
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
        
        // Sauvegarder dans les préférences
        sharedPreferences.edit().apply {
            putBoolean("reminder_${reminderId}_enabled", true)
            putInt("reminder_${reminderId}_hour", hour)
            putInt("reminder_${reminderId}_minute", minute)
        }.apply()
        
        Log.d(TAG, "✅ Rappel configuré: ID=$reminderId, Heure=$hour:$minute")
    }
    
    /**
     * Annule un rappel
     */
    fun cancelReminder(reminderId: Int) {
        val intent = Intent(context, StudyReminderReceiver::class.java).apply {
            action = ACTION_REMINDER
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
        
        // Désactiver dans les préférences
        sharedPreferences.edit().putBoolean("reminder_${reminderId}_enabled", false).apply()
        
        Log.d(TAG, "🗑️ Rappel annulé: ID=$reminderId")
    }
    
    /**
     * Vérifie si un rappel est activé
     */
    fun isReminderEnabled(reminderId: Int): Boolean {
        return sharedPreferences.getBoolean("reminder_${reminderId}_enabled", false)
    }
    
    /**
     * Obtient l'heure d'un rappel
     */
    fun getReminderTime(reminderId: Int): Pair<Int, Int>? {
        val hour = sharedPreferences.getInt("reminder_${reminderId}_hour", -1)
        val minute = sharedPreferences.getInt("reminder_${reminderId}_minute", -1)
        
        return if (hour >= 0 && minute >= 0) Pair(hour, minute) else null
    }
    
    /**
     * Programme les rappels par défaut si activés
     */
    fun scheduleDefaultReminders() {
        if (isReminderEnabled(REMINDER_MORNING)) {
            getReminderTime(REMINDER_MORNING)?.let { (hour, minute) ->
                scheduleDailyReminder(REMINDER_MORNING, hour, minute, true)
            }
        }
        
        if (isReminderEnabled(REMINDER_AFTERNOON)) {
            getReminderTime(REMINDER_AFTERNOON)?.let { (hour, minute) ->
                scheduleDailyReminder(REMINDER_AFTERNOON, hour, minute, true)
            }
        }
        
        if (isReminderEnabled(REMINDER_EVENING)) {
            getReminderTime(REMINDER_EVENING)?.let { (hour, minute) ->
                scheduleDailyReminder(REMINDER_EVENING, hour, minute, true)
            }
        }
        
        Log.d(TAG, "📅 Rappels par défaut configurés")
    }
}

/**
 * Receiver pour les rappels d'étude
 */
class StudyReminderReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getIntExtra("reminder_id", -1)
        
        if (reminderId > 0) {
            Log.d("StudyReminder", "🔔 Rappel déclenché: ID=$reminderId")
            
            // Afficher une notification
            showStudyNotification(context, reminderId)
            
            // Reprogrammer pour demain
            val manager = StudyReminderManager(context)
            val time = manager.getReminderTime(reminderId)
            time?.let { (hour, minute) ->
                manager.scheduleDailyReminder(reminderId, hour, minute, true)
            }
        }
    }
    
    private fun showStudyNotification(context: Context, reminderId: Int) {
        val channelId = "study_reminders"
        
        // Créer le canal de notification (requis pour Android 8+)
        val name = "Rappels d'étude"
        val descriptionText = "Rappels pour votre apprentissage"
        val importance = android.app.NotificationManager.IMPORTANCE_HIGH
        val channel = android.app.NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
        }
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.createNotificationChannel(channel)
        
        val message = when (reminderId) {
            StudyReminderManager.REMINDER_MORNING -> "Bon matin! Il est temps de commencer votre apprentissage 🚀"
            StudyReminderManager.REMINDER_AFTERNOON -> "Pause déjeuner? Profitez-en pour réviser 📚"
            StudyReminderManager.REMINDER_EVENING -> "Bonne soirée! Révisez un peu avant de dormir 🌙"
            else -> "C'est l'heure de réviser!"
        }
        
        val notification = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Rappel d'étude")
            .setContentText(message)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(reminderId, notification)
    }
}




