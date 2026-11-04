package com.pilotprep.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.pilotprep.app.ui.screens.*
import com.pilotprep.app.ui.screens.atc.AtcExerciseScreen
import com.pilotprep.app.ui.screens.audio.PodcastListScreen
import com.pilotprep.app.ui.screens.audio.PodcastPlayerScreen
import com.pilotprep.app.ui.screens.auth.AuthLandingScreen
import com.pilotprep.app.ui.screens.auth.EstablishmentAuthScreen
import com.pilotprep.app.ui.screens.auth.EstablishmentDashboardScreen
import com.pilotprep.app.ui.screens.auth.PersonalAuthScreen
import com.pilotprep.app.ui.screens.auth.PersonalDashboardScreen
import com.pilotprep.app.ui.screens.heritage.AircraftDetailScreen
import com.pilotprep.app.ui.screens.heritage.HeritageScreen
import com.pilotprep.app.ui.screens.theory.LessonDetailScreen
import com.pilotprep.app.ui.screens.theory.LessonListScreen
import com.pilotprep.app.ui.screens.theory.TheorySelectionScreen
import com.pilotprep.app.ui.screens.theory.TheorySubjectSelectionScreen
import com.pilotprep.app.ui.screens.needtoknow.NeedToKnowListScreen
import com.pilotprep.app.ui.screens.needtoknow.NeedToKnowDetailScreen
import com.pilotprep.app.ui.screens.search.SearchScreen
import com.pilotprep.app.ui.screens.stats.ProgressDashboardScreen
import com.pilotprep.app.ui.screens.WeeklyTipDetailScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.android.EntryPointAccessors

@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface AudioManagerEntryPoint {
    fun audioPlayerManager(): com.pilotprep.app.util.AudioPlayerManager
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = "home"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // --- ÉCRANS PRINCIPAUX DE LA BOTTOM BAR ---
        composable("home") { HomeScreen(navController = navController) }
        composable("mediatheque") { MediathequeScreen(navController = navController) }

        // --- FLUX THÉORIE ---
        composable("theory_selection") { TheorySelectionScreen(navController = navController) }
        composable(
            route = "theory_subject_selection/{category}",
            arguments = listOf(navArgument("category") { type = NavType.StringType })
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: "summary"
            TheorySubjectSelectionScreen(navController = navController, category = category)
        }
        composable(
            route = "review_sheet/{category}/{subject}",
            arguments = listOf(
                navArgument("category") { type = NavType.StringType },
                navArgument("subject") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: "N/A"
            val subject = backStackEntry.arguments?.getString("subject") ?: "N/A"
            
            if (category.lowercase() == "need_to_know") {
                // Redirection vers NeedToKnowListScreen pour la catégorie "Need to Know"
                NeedToKnowListScreen(navController = navController)
            } else {
                // Utilisation normale de LessonListScreen pour les autres catégories
                LessonListScreen(navController = navController, license = category, subject = subject)
            }
        }
        // --- NEED TO KNOW ---
        composable("need_to_know_list") {
            NeedToKnowListScreen(navController = navController)
        }
        composable(
            route = "need_to_know_detail/{contentId}",
            arguments = listOf(navArgument("contentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val contentId = backStackEntry.arguments?.getString("contentId") ?: ""
            NeedToKnowDetailScreen(
                contentId = contentId,
                navController = navController
            )
        }
        
        composable(
            route = "lesson_detail/{lessonId}",
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
            LessonDetailScreen(navController = navController, lessonId = lessonId)
        }

        // --- FLUX PRATIQUE & EXAMEN ---
        composable(
            route = "license_selection/{mode}",
            arguments = listOf(navArgument("mode") { type = NavType.StringType })
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "practice"
            LicenseSelectionScreen(navController = navController, mode = mode)
        }
        composable(
            route = "subject_selection/{license}/{mode}",
            arguments = listOf(
                navArgument("license") { type = NavType.StringType },
                navArgument("mode") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val license = backStackEntry.arguments?.getString("license") ?: ""
            val mode = backStackEntry.arguments?.getString("mode") ?: "practice"
            SubjectSelectionScreen(navController = navController, license = license, mode = mode)
        }
        composable(
            route = "quiz/{license}/{subject}/{mode}",
            arguments = listOf(
                navArgument("license") { type = NavType.StringType },
                navArgument("subject") { type = NavType.StringType },
                navArgument("mode") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val license = backStackEntry.arguments?.getString("license") ?: ""
            val subject = backStackEntry.arguments?.getString("subject") ?: ""
            val mode = backStackEntry.arguments?.getString("mode") ?: "practice"
            if (mode == "exam") {
                ExamScreen(navController = navController, license = license, subject = subject)
            } else {
                QuizScreen(navController = navController, license = license, subject = subject, mode = mode)
            }
        }

        // --- FLUX PATRIMOINE ---
        composable("heritage") { HeritageScreen(navController = navController) }
        composable(
            route = "aircraft_detail/{aircraftId}",
            arguments = listOf(navArgument("aircraftId") { type = NavType.StringType })
        ) { backStackEntry ->
            val aircraftId = backStackEntry.arguments?.getString("aircraftId") ?: ""
            AircraftDetailScreen(navController = navController, aircraftId = aircraftId)
        }

        // --- FLUX AUDIO ---
        composable("audio") { PodcastListScreen(navController = navController) }
        composable("podcast_player") {
            val context = LocalContext.current
            val audioPlayerManager = EntryPointAccessors.fromApplication(
                context.applicationContext,
                AudioManagerEntryPoint::class.java
            ).audioPlayerManager()
            PodcastPlayerScreen(navController = navController, audioPlayerManager = audioPlayerManager)
        }

        // --- FLUX ATC TALK ---
        composable("atc_talk") { AtcTalkSelectionScreen(navController = navController) }
        composable(
            route = "atc_talk_content/{type}",
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "N/A"
            AtcExerciseScreen(navController = navController, type = type)
        }

        // --- ROUTES D'AUTHENTIFICATION ---
        composable("auth_landing") { AuthLandingScreen(navController = navController) }
        composable("personal_auth") { PersonalAuthScreen(navController = navController) }
        composable("establishment_auth") { EstablishmentAuthScreen(navController = navController) }
        composable("personal_dashboard") { PersonalDashboardScreen(navController = navController) }
        composable("establishment_dashboard") { EstablishmentDashboardScreen(navController = navController) }
        
        // --- ROUTES DE PROGRESSION ---
        composable(
            route = "progress_dashboard/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: "default_user"
            ProgressDashboardScreen(navController = navController, userId = userId)
        }

        // --- ROUTES GLOBALES ---
        composable("search") { SearchScreen(navController = navController) }
        composable("settings") { SettingsScreen(navController = navController) }
        composable("about") { AboutScreen(navController = navController) }
        composable("history") { 
            // Placeholder pour l'historique
            androidx.compose.material3.Text("Historique - En développement")
        }
        composable("bookmarks") { 
            // Placeholder pour les signets
            androidx.compose.material3.Text("Signets - En développement")
        }
        
        // --- ROUTE DÉTAIL ASTUCE ---
        composable(
            route = "weekly_tip_detail/{tipId}",
            arguments = listOf(navArgument("tipId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tipId = backStackEntry.arguments?.getString("tipId") ?: ""
            WeeklyTipDetailScreen(
                tipId = tipId,
                navController = navController
            )
        }
        
    }
}