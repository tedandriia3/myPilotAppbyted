package com.pilotprep.app.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "pilot_prep_session",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    private val _sessionState = MutableStateFlow(SessionState())
    val sessionState: Flow<SessionState> = _sessionState.asStateFlow()
    
    init {
        loadSession()
    }
    
    /**
     * Sauvegarde automatique de la session
     */
    fun saveSession(session: SessionState) {
        try {
            sharedPreferences.edit().apply {
                putString("user_id", session.userId)
                putString("user_email", session.userEmail)
                putString("user_name", session.userName)
                putLong("last_login", session.lastLogin)
                putLong("session_start", session.sessionStart)
                putInt("current_week", session.currentWeek)
                putString("last_quiz_id", session.lastQuizId)
                putInt("last_quiz_score", session.lastQuizScore)
                putLong("total_study_time", session.totalStudyTime)
                putBoolean("is_offline_mode", session.isOfflineMode)
                putString("preferred_language", session.preferredLanguage)
                putBoolean("dark_mode_enabled", session.darkModeEnabled)
                putString("notification_settings", session.notificationSettings)
                apply()
            }
            _sessionState.value = session
        } catch (e: Exception) {
            // Log error but don't crash
            android.util.Log.e("SessionManager", "Erreur lors de la sauvegarde: ${e.message}")
        }
    }
    
    /**
     * Charge la session depuis le stockage
     */
    private fun loadSession() {
        try {
            val session = SessionState(
                userId = sharedPreferences.getString("user_id", "") ?: "",
                userEmail = sharedPreferences.getString("user_email", "") ?: "",
                userName = sharedPreferences.getString("user_name", "") ?: "",
                lastLogin = sharedPreferences.getLong("last_login", 0L),
                sessionStart = sharedPreferences.getLong("session_start", System.currentTimeMillis()),
                currentWeek = sharedPreferences.getInt("current_week", 1),
                lastQuizId = sharedPreferences.getString("last_quiz_id", "") ?: "",
                lastQuizScore = sharedPreferences.getInt("last_quiz_score", 0),
                totalStudyTime = sharedPreferences.getLong("total_study_time", 0L),
                isOfflineMode = sharedPreferences.getBoolean("is_offline_mode", false),
                preferredLanguage = sharedPreferences.getString("preferred_language", "fr") ?: "fr",
                darkModeEnabled = sharedPreferences.getBoolean("dark_mode_enabled", false),
                notificationSettings = sharedPreferences.getString("notification_settings", "all") ?: "all"
            )
            _sessionState.value = session
        } catch (e: Exception) {
            android.util.Log.e("SessionManager", "Erreur lors du chargement: ${e.message}")
            _sessionState.value = SessionState()
        }
    }
    
    /**
     * Met à jour une propriété spécifique
     */
    fun updateProperty(update: SessionState.() -> SessionState) {
        val currentSession = _sessionState.value
        val updatedSession = currentSession.update()
        saveSession(updatedSession)
    }
    
    /**
     * Efface la session
     */
    fun clearSession() {
        try {
            sharedPreferences.edit().clear().apply()
            _sessionState.value = SessionState()
        } catch (e: Exception) {
            android.util.Log.e("SessionManager", "Erreur lors de l'effacement: ${e.message}")
        }
    }
    
    /**
     * Sauvegarde automatique périodique
     */
    fun enableAutoSave(intervalMs: Long = 30000L) {
        // Cette fonction pourrait être étendue pour implémenter une sauvegarde périodique
        // Pour l'instant, la sauvegarde se fait à chaque modification
    }
}

/**
 * État de la session utilisateur
 */
data class SessionState(
    val userId: String = "",
    val userEmail: String = "",
    val userName: String = "",
    val lastLogin: Long = 0L,
    val sessionStart: Long = System.currentTimeMillis(),
    val currentWeek: Int = 1,
    val lastQuizId: String = "",
    val lastQuizScore: Int = 0,
    val totalStudyTime: Long = 0L, // en millisecondes
    val isOfflineMode: Boolean = false,
    val preferredLanguage: String = "fr",
    val darkModeEnabled: Boolean = false,
    val notificationSettings: String = "all" // "all", "important", "none"
) {
    /**
     * Durée de la session actuelle
     */
    val currentSessionDuration: Long
        get() = System.currentTimeMillis() - sessionStart
    
    /**
     * Temps total d'étude formaté
     */
    val formattedTotalStudyTime: String
        get() = formatDuration(totalStudyTime)
    
    /**
     * Durée de session actuelle formatée
     */
    val formattedCurrentSessionDuration: String
        get() = formatDuration(currentSessionDuration)
    
    private fun formatDuration(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m ${seconds}s"
            else -> "${seconds}s"
        }
    }
}

/**
 * Extensions pour faciliter les mises à jour
 */
fun SessionState.updateUserId(userId: String) = copy(userId = userId)
fun SessionState.updateUserEmail(email: String) = copy(userEmail = email)
fun SessionState.updateUserName(name: String) = copy(userName = name)
fun SessionState.updateLastLogin() = copy(lastLogin = System.currentTimeMillis())
fun SessionState.updateCurrentWeek(week: Int) = copy(currentWeek = week)
fun SessionState.updateLastQuiz(quizId: String, score: Int) = copy(
    lastQuizId = quizId,
    lastQuizScore = score
)
fun SessionState.addStudyTime(timeMs: Long) = copy(
    totalStudyTime = totalStudyTime + timeMs
)
fun SessionState.toggleOfflineMode() = copy(isOfflineMode = !isOfflineMode)
fun SessionState.updateLanguage(language: String) = copy(preferredLanguage = language)
fun SessionState.toggleDarkMode() = copy(darkModeEnabled = !darkModeEnabled)
fun SessionState.updateNotificationSettings(settings: String) = copy(notificationSettings = settings)
