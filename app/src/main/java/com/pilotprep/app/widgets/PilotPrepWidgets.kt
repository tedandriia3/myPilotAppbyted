package com.pilotprep.app.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.pilotprep.app.MainActivity
import com.pilotprep.app.R

/**
 * Widget pour afficher les statistiques de progression
 */
class ProgressWidget : AppWidgetProvider() {
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
    
    override fun onEnabled(context: Context) {
        // Premier widget créé
    }
    
    override fun onDisabled(context: Context) {
        // Dernier widget supprimé
    }
    
    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_progress)
            
            // Intent pour ouvrir l'app
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            
            // Mettre à jour les données (simulées pour l'exemple)
            views.setTextViewText(R.id.widget_title, "PilotPrep")
            views.setTextViewText(R.id.widget_level, "Niveau 5")
            views.setTextViewText(R.id.widget_points, "1,250 XP")
            views.setTextViewText(R.id.widget_streak, "7 jours")
            
            // Mettre à jour le widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

/**
 * Widget pour l'astuce de la semaine
 */
class WeeklyTipWidget : AppWidgetProvider() {
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
    
    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_weekly_tip)
            
            // Intent pour ouvrir l'astuce
            val intent = Intent(context, MainActivity::class.java).apply {
                putExtra("action", "open_weekly_tip")
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            
            // Données simulées
            views.setTextViewText(R.id.widget_title, "Astuce de la Semaine")
            views.setTextViewText(R.id.widget_tip_title, "Communications Radio")
            views.setTextViewText(R.id.widget_tip_preview, "Maîtrisez les communications...")
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

/**
 * Widget pour démarrer un quiz rapide
 */
class QuickQuizWidget : AppWidgetProvider() {
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
    
    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_quick_quiz)
            
            // Intent pour démarrer un quiz
            val intent = Intent(context, MainActivity::class.java).apply {
                putExtra("action", "start_quiz")
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_button, pendingIntent)
            
            views.setTextViewText(R.id.widget_title, "Quiz Rapide")
            views.setTextViewText(R.id.widget_button, "Commencer")
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}



