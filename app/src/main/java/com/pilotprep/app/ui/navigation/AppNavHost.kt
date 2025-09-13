package com.pilotprep.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.pilotprep.app.ui.screens.*

@Composable
fun AppNavigation(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = "home", // On commence sur l'écran d'accueil
        modifier = modifier
    ) {
        // Écrans principaux de la Bottom Bar
        composable("home") { HomeScreen(navController = navController) }
        composable("mediatheque") { MediathequeScreen(navController = navController) }

        // --- Le reste de la navigation, accessible depuis la médiathèque ---
        composable("theory_selection") { TheorySelectionScreen(navController = navController) }
        composable(
            route = "theory_subject_selection/{license}",
            arguments = listOf(navArgument("license") { type = NavType.StringType })
        ) { backStackEntry ->
            val license = backStackEntry.arguments?.getString("license") ?: "N/A"
            TheorySubjectSelectionScreen(navController = navController, license = license)
        }
        composable(
            route = "review_sheet/{license}/{subject}",
            arguments = listOf(/*...*/)
        ) { /*...*/ }

        composable(
            route = "license_selection/{mode}",
            arguments = listOf(/*...*/)
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "practice"
            LicenseSelectionScreen(navController = navController, mode = mode)
        }
        composable(
            route = "subject_selection/{license}/{mode}",
            arguments = listOf(/*...*/)
        ) { backStackEntry ->
            val license = backStackEntry.arguments?.getString("license") ?: ""
            val mode = backStackEntry.arguments?.getString("mode") ?: "practice"
            SubjectSelectionScreen(navController = navController, license = license, mode = mode)
        }
        composable(
            route = "quiz/{license}/{subject}/{mode}",
            arguments = listOf(/*...*/)
        ) { backStackEntry ->
            val license = backStackEntry.arguments?.getString("license") ?: ""
            val subject = backStackEntry.arguments?.getString("subject") ?: ""
            val mode = backStackEntry.arguments?.getString("mode") ?: "practice"
            QuizScreen(navController = navController, license = license, subject = subject, mode = mode)
        }

        composable("heritage") { PlaceholderScreen(title = "Patrimoine du Ciel", content = "Le contenu sera bientôt disponible.") }
        composable("audio") { PlaceholderScreen(title = "Voix du Ciel", content = "Le contenu sera bientôt disponible.") }
    }
}