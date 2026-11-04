package com.pilotprep.app.util

import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Optimiseur de navigation pour gérer efficacement les NavControllers
 */
@Singleton
class NavigationOptimizer @Inject constructor() {
    
    private val _activeNavControllers = MutableStateFlow<Map<String, NavHostController>>(emptyMap())
    val activeNavControllers = _activeNavControllers.asStateFlow()
    
    private val maxControllers = 3 // Limite le nombre de controllers actifs
    
    /**
     * Obtient ou crée un NavController pour une session
     */
    @Composable
    fun getOrCreateNavController(sessionId: String): NavHostController {
        val currentControllers = _activeNavControllers.value
        
        return currentControllers[sessionId] ?: run {
            val newController = rememberNavController()
            
            // Nettoyer les anciens controllers si on dépasse la limite
            if (currentControllers.size >= maxControllers) {
                cleanupOldControllers()
            }
            
            _activeNavControllers.value = currentControllers + (sessionId to newController)
            newController
        }
    }
    
    /**
     * Nettoie les controllers inactifs
     */
    private fun cleanupOldControllers() {
        val currentControllers = _activeNavControllers.value
        if (currentControllers.size > maxControllers) {
            // Garder seulement les 2 plus récents
            val sortedControllers = currentControllers.toList()
                .sortedByDescending { it.second.currentBackStackEntry?.destination?.route }
                .take(maxControllers - 1)
            
            _activeNavControllers.value = sortedControllers.toMap()
        }
    }
    
    /**
     * Supprime un controller spécifique
     */
    fun removeNavController(sessionId: String) {
        val currentControllers = _activeNavControllers.value
        _activeNavControllers.value = currentControllers - sessionId
    }
    
    /**
     * Nettoie tous les controllers
     */
    fun cleanup() {
        _activeNavControllers.value = emptyMap()
    }
}
