package com.pilotprep.app.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.pilotprep.app.ui.components.AppBottomNavigationBar
import com.pilotprep.app.ui.components.AppTopBar
import com.pilotprep.app.ui.navigation.AppNavigation

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Logique pour déterminer le titre de la TopBar en fonction de la route
    val title = when {
        currentRoute?.startsWith("home") == true -> "Accueil"
        currentRoute?.startsWith("mediatheque") == true -> "Médiathèque"
        currentRoute?.startsWith("theory") == true -> "Théorie du vol"
        currentRoute?.startsWith("license_selection/practice") == true -> "Mise en Pratique"
        currentRoute?.startsWith("license_selection/exam") == true -> "Simulation d'Examen"
        currentRoute?.startsWith("quiz") == true -> "Quiz en cours"
        else -> "Pilot Prep"
    }

    Scaffold(
        topBar = { AppTopBar(title = title) },
        bottomBar = { AppBottomNavigationBar(navController = navController) },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: Logique pour le FAB */ }) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter")
            }
        }
    ) { innerPadding ->
        // Passer le navController et le padding au NavHost
        AppNavigation(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}