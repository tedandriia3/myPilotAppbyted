package com.pilotprep.app.ui.screens

import androidx.activity.ComponentActivity
import androidx.annotation.RawRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.pilotprep.app.R
import com.pilotprep.app.viewmodels.AuthViewModel
import com.pilotprep.app.viewmodels.ImageOfDayViewModel
import com.pilotprep.app.viewmodels.WeeklyTipViewModel
import com.pilotprep.app.ui.components.ImageOfDayCard
import com.pilotprep.app.ui.components.WeeklyTipCard
import com.pilotprep.app.util.AnalyticsTracker

@Composable
fun HomeScreen(navController: NavController) {
    // hiltViewModel gère déjà la mémorisation automatiquement
    val context = LocalContext.current as ComponentActivity
    val authViewModel: AuthViewModel = hiltViewModel(context)
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    
    val imageOfDayViewModel: ImageOfDayViewModel = hiltViewModel(context)
    val imageOfDayState by imageOfDayViewModel.uiState.collectAsStateWithLifecycle()
    
    val weeklyTipViewModel: WeeklyTipViewModel = hiltViewModel(context)
    val weeklyTipState by weeklyTipViewModel.uiState.collectAsStateWithLifecycle()

    val analytics = remember { AnalyticsTracker(context.applicationContext) }

    LaunchedEffect(Unit) {
        analytics.trackScreenView("HomeScreen")
    }
    
    // Nettoyer le cache lors de la destruction
    com.pilotprep.app.util.LifecycleCleanup {
        com.pilotprep.app.util.PerformanceOptimizer.cleanExpiredEntries()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // --- Liens Rapides ---
        item {
            SectionTitle(title = "Liens Rapides")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                QuickLinkCard(
                    title = "Cours",
                    lottieResId = R.raw.lottie_cours,
                    onClick = {
                        analytics.trackEvent("quick_link_click", mapOf("link" to "cours"))
                        navController.navigate("theory_selection")
                    },
                    modifier = Modifier.weight(1f)
                )
                QuickLinkCard(
                    title = "Quiz",
                    lottieResId = R.raw.lottie_quiz,
                    onClick = {
                        analytics.trackEvent("quick_link_click", mapOf("link" to "quiz"))
                        navController.navigate("license_selection/practice")
                    },
                    modifier = Modifier.weight(1f)
                )
                QuickLinkCard(
                    title = "Audio",
                    lottieResId = R.raw.lottie_audio,
                    onClick = {
                        analytics.trackEvent("quick_link_click", mapOf("link" to "audio"))
                        navController.navigate("audio")
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // --- SECTION COMPTE ---
        item {
            SectionTitle(title = "Comptes")
            AccountCard(
                isLoggedIn = authState.isLoggedIn,
                userName = authState.user?.displayName,
                onClick = {
                    if (authState.isLoggedIn) {
                        when (authState.userRole) {
                            "ADMIN" -> navController.navigate("establishment_dashboard")
                            "PERSONAL" -> navController.navigate("personal_dashboard")
                            else -> navController.navigate("auth_landing")
                        }
                    } else {
                        navController.navigate("auth_landing")
                    }
                }
            )
        }

        // --- SECTION PROGRESSION ---
        item {
            SectionTitle(title = "Mon Progrès")
            ProgressCard(
                onClick = { 
                    val userId = authState.user?.uid ?: "default_user"
                    navController.navigate("progress_dashboard/$userId")
                }
            )
        }

        // --- Astuce de la semaine (dynamique) ---
        item {
            SectionTitle(title = "Astuce de la Semaine")
            WeeklyTipCard(
                uiState = weeklyTipState,
                onTipClick = { tipId ->
                    navController.navigate("weekly_tip_detail/$tipId")
                },
                onRefresh = {
                    weeklyTipViewModel.refreshWeeklyTip()
                }
            )
        }

        // --- Image du jour (dynamique) ---
        item {
            SectionTitle(title = "Image du Jour")
            ImageOfDayCard(
                uiState = imageOfDayState,
                onImageClick = { aircraftId ->
                    navController.navigate("aircraft_detail/$aircraftId")
                },
                onRefresh = {
                    imageOfDayViewModel.refreshImageOfDay()
                }
            )
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickLinkCard(
    title: String,
    @RawRes lottieResId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieResId))
    Card(
        onClick = onClick,
        modifier = modifier.height(120.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun InfoCard(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(content, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun ProgressCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animation Lottie pour les statistiques
            val composition by rememberLottieComposition(
                spec = LottieCompositionSpec.RawRes(R.raw.lottie_stats_animation) // Remplacez par votre fichier
            )
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Voir mes Statistiques",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Suivez votre progression et vos performances",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Voir plus",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun AccountCard(isLoggedIn: Boolean, userName: String?, onClick: () -> Unit) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_account))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(60.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = if (isLoggedIn) {
                    if (!userName.isNullOrBlank()) userName else "Mon Compte"
                } else {
                    "Se connecter"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}