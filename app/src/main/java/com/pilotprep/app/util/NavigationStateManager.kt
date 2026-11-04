package com.pilotprep.app.util

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object NavigationStateManager {
    private const val PREFS_NAME = "navigation_state_prefs"
    private const val KEY_HOME_LAST_ROUTE = "home_last_route"
    private const val KEY_MEDIATHEQUE_LAST_ROUTE = "mediatheque_last_route"
    private const val KEY_ACTIVE_SECTION = "active_section"
    
    // États en mémoire pour la navigation active
    private val _homeNavigationState = MutableStateFlow<NavigationState?>(null)
    private val _mediathequeNavigationState = MutableStateFlow<NavigationState?>(null)
    private val _activeSection = MutableStateFlow<String>("home")
    
    val homeNavigationState: StateFlow<NavigationState?> = _homeNavigationState.asStateFlow()
    val mediathequeNavigationState: StateFlow<NavigationState?> = _mediathequeNavigationState.asStateFlow()
    val activeSection: StateFlow<String> = _activeSection.asStateFlow()
    
    data class NavigationState(
        val currentRoute: String,
        val backStack: List<String>,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    fun saveNavigationState(context: Context, section: String, navController: NavController) {
        try {
            val currentRoute = navController.currentBackStackEntry?.destination?.route ?: section
            val backStack = navController.currentBackStack.value.map { it.destination.route ?: "" }
            
            val state = NavigationState(
                currentRoute = currentRoute,
                backStack = backStack
            )
            
            when (section) {
                "home" -> {
                    _homeNavigationState.value = state
                    saveToPreferences(context, KEY_HOME_LAST_ROUTE, currentRoute)
                }
                "mediatheque" -> {
                    _mediathequeNavigationState.value = state
                    saveToPreferences(context, KEY_MEDIATHEQUE_LAST_ROUTE, currentRoute)
                }
            }
            
            _activeSection.value = section
            saveToPreferences(context, KEY_ACTIVE_SECTION, section)
            
            Log.d("NavigationStateManager", "État sauvegardé pour $section: $currentRoute")
        } catch (e: Exception) {
            Log.e("NavigationStateManager", "Erreur lors de la sauvegarde de l'état pour $section: ${e.message}")
        }
    }
    
    fun getLastRoute(context: Context, section: String): String {
        val key = when (section) {
            "home" -> KEY_HOME_LAST_ROUTE
            "mediatheque" -> KEY_MEDIATHEQUE_LAST_ROUTE
            else -> return section
        }
        return getFromPreferences(context, key, section)
    }
    
    fun getActiveSection(context: Context): String {
        return getFromPreferences(context, KEY_ACTIVE_SECTION, "home")
    }
    
    fun clearNavigationState(section: String) {
        when (section) {
            "home" -> _homeNavigationState.value = null
            "mediatheque" -> _mediathequeNavigationState.value = null
        }
    }
    
    fun isAtRoot(section: String, currentRoute: String?): Boolean {
        return when (section) {
            "home" -> currentRoute == "home"
            "mediatheque" -> currentRoute == "mediatheque"
            else -> true
        }
    }
    
    private fun saveToPreferences(context: Context, key: String, value: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(key, value).apply()
    }
    
    private fun getFromPreferences(context: Context, key: String, defaultValue: String): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(key, defaultValue) ?: defaultValue
    }
    
    @Composable
    fun rememberNavigationStateManager(): NavigationStateManager {
        val context = LocalContext.current
        return remember { NavigationStateManager }
    }
}
