package com.pilotprep.app.ui.screens.auth

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pilotprep.app.viewmodels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthLandingScreen(navController: NavController) {
    // On récupère le ViewModel partagé pour connaître l'état de connexion
    val authViewModel: AuthViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    // --- LOGIQUE DE REDIRECTION ---
    // Si l'utilisateur est déjà connecté, on le redirige vers le bon tableau de bord
    // et on l'empêche de voir cet écran.
    if (authState.isLoggedIn) {
        LaunchedEffect(authState.userRole) {
            val destination = when (authState.userRole) {
                "ADMIN" -> "establishment_dashboard"
                "PERSONAL" -> "personal_dashboard"
                else -> null // Ne rien faire si le rôle n'est pas encore chargé
            }
            if (destination != null) {
                navController.navigate(destination) {
                    // On retire l'écran "auth_landing" de l'historique
                    popUpTo("auth_landing") { inclusive = true }
                }
            }
        }
    }
    // --- FIN DE LA LOGIQUE DE REDIRECTION ---

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Création de Compte") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Choisissez votre type de compte",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(48.dp))

            Button(
                onClick = { navController.navigate("personal_auth") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                Text("Compte Personnel", style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "Pour les étudiants et passionnés qui s'entraînent de manière autonome.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(48.dp))

            Button(
                onClick = { navController.navigate("establishment_auth") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                Text("Compte Établissement", style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "Pour les écoles de pilotage et les instructeurs qui souhaitent suivre leurs élèves.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}