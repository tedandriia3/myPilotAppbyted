package com.pilotprep.app.util

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestionnaire centralisé pour Firebase Analytics et Crashlytics
 * Permet de tracker les événements et les erreurs dans l'application
 */
@Singleton
class AnalyticsTracker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val analytics = FirebaseAnalytics.getInstance(context)
    private val crashlytics = FirebaseCrashlytics.getInstance()
    
    /**
     * Track un événement d'affichage d'écran
     */
    fun trackScreenView(screenName: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
        }
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }
    
    /**
     * Track un événement personnalisé
     */
    fun trackEvent(eventName: String, parameters: Map<String, Any> = emptyMap()) {
        val bundle = Bundle().apply {
            parameters.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Boolean -> putBoolean(key, value)
                }
            }
        }
        analytics.logEvent(eventName, bundle)
    }
    
    /**
     * Track le démarrage d'un quiz
     */
    fun trackQuizStarted(subject: String, license: String, count: Int = 10) {
        trackEvent("quiz_started", mapOf(
            "subject" to subject,
            "license" to license,
            "question_count" to count,
            "timestamp" to System.currentTimeMillis()
        ))
    }
    
    /**
     * Track la completion d'un quiz
     */
    fun trackQuizCompleted(
        subject: String,
        license: String,
        score: Int,
        totalQuestions: Int,
        durationSeconds: Long,
        isPassed: Boolean
    ) {
        trackEvent("quiz_completed", mapOf(
            "subject" to subject,
            "license" to license,
            "score" to score,
            "total_questions" to totalQuestions,
            "percentage" to ((score.toFloat() / totalQuestions * 100).toInt()),
            "duration_seconds" to durationSeconds,
            "is_passed" to isPassed,
            "timestamp" to System.currentTimeMillis()
        ))
    }
    
    /**
     * Track le début d'une session d'étude
     */
    fun trackStudySessionStarted(sessionType: String, subject: String) {
        trackEvent("study_session_started", mapOf(
            "session_type" to sessionType,
            "subject" to subject,
            "timestamp" to System.currentTimeMillis()
        ))
    }
    
    /**
     * Track la fin d'une session d'étude
     */
    fun trackStudySessionEnded(
        sessionType: String,
        durationMinutes: Long,
        itemsCompleted: Int
    ) {
        trackEvent("study_session_ended", mapOf(
            "session_type" to sessionType,
            "duration_minutes" to durationMinutes,
            "items_completed" to itemsCompleted,
            "timestamp" to System.currentTimeMillis()
        ))
    }
    
    /**
     * Track l'ouverture d'un podcast
     */
    fun trackPodcastOpened(podcastId: String, podcastName: String) {
        trackEvent("podcast_opened", mapOf(
            "podcast_id" to podcastId,
            "podcast_name" to podcastName,
            "timestamp" to System.currentTimeMillis()
        ))
    }
    
    /**
     * Track l'ouverture d'un contenu (cours, leçon, etc.)
     */
    fun trackContentOpened(contentType: String, contentId: String, contentName: String) {
        trackEvent("content_opened", mapOf(
            "content_type" to contentType,
            "content_id" to contentId,
            "content_name" to contentName,
            "timestamp" to System.currentTimeMillis()
        ))
    }
    
    /**
     * Track une recherche
     */
    fun trackSearch(query: String, resultCount: Int) {
        trackEvent("search", mapOf(
            "query" to query,
            "result_count" to resultCount,
            "timestamp" to System.currentTimeMillis()
        ))
    }
    
    /**
     * Track un signet créé
     */
    fun trackBookmarkCreated(contentType: String, contentId: String) {
        trackEvent("bookmark_created", mapOf(
            "content_type" to contentType,
            "content_id" to contentId,
            "timestamp" to System.currentTimeMillis()
        ))
    }
    
    /**
     * Track un partage de contenu
     */
    fun trackContentShared(contentType: String, contentId: String, shareMethod: String) {
        trackEvent("content_shared", mapOf(
            "content_type" to contentType,
            "content_id" to contentId,
            "share_method" to shareMethod,
            "timestamp" to System.currentTimeMillis()
        ))
    }
    
    /**
     * Track une erreur Firebase
     */
    fun trackFirebaseError(operation: String, errorType: String, errorMessage: String) {
        trackEvent("firebase_error", mapOf(
            "operation" to operation,
            "error_type" to errorType,
            "error_message" to errorMessage,
            "timestamp" to System.currentTimeMillis()
        ))
    }
    
    /**
     * Track une erreur réseau
     */
    fun trackNetworkError(operation: String, errorMessage: String) {
        trackEvent("network_error", mapOf(
            "operation" to operation,
            "error_message" to errorMessage,
            "timestamp" to System.currentTimeMillis()
        ))
    }
    
    /**
     * Enregistre une exception dans Crashlytics
     */
    fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }
    
    /**
     * Enregistre une exception avec contexte
     */
    fun recordException(throwable: Throwable, context: String) {
        crashlytics.setCustomKey("error_context", context)
        crashlytics.recordException(throwable)
    }
    
    /**
     * Set un attribut utilisateur personnalisé
     */
    fun setUserProperty(name: String, value: String) {
        analytics.setUserProperty(name, value)
    }
    
    /**
     * Set l'ID utilisateur
     */
    fun setUserId(userId: String?) {
        analytics.setUserId(userId)
        userId?.let { crashlytics.setUserId(it) }
    }
    
    /**
     * Set des attributs personnalisés pour Crashlytics
     */
    fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }
    
    fun setCustomKey(key: String, value: Int) {
        crashlytics.setCustomKey(key, value)
    }
    
    fun setCustomKey(key: String, value: Long) {
        crashlytics.setCustomKey(key, value)
    }
    
    fun setCustomKey(key: String, value: Boolean) {
        crashlytics.setCustomKey(key, value)
    }
    
    /**
     * Track l'événement de connexion utilisateur
     */
    fun trackUserLogin(userId: String, loginMethod: String) {
        trackEvent("user_login", mapOf(
            "user_id" to userId,
            "login_method" to loginMethod,
            "timestamp" to System.currentTimeMillis()
        ))
    }
    
    /**
     * Track l'événement de déconnexion utilisateur
     */
    fun trackUserLogout() {
        trackEvent("user_logout", mapOf(
            "timestamp" to System.currentTimeMillis()
        ))
    }
    
    /**
     * Track un événement de progression
     */
    fun trackProgress(contentType: String, progress: Int, total: Int) {
        trackEvent("progress_update", mapOf(
            "content_type" to contentType,
            "progress" to progress,
            "total" to total,
            "percentage" to ((progress.toFloat() / total * 100).toInt()),
            "timestamp" to System.currentTimeMillis()
        ))
    }
}


