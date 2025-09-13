package com.pilotprep.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pilotprep.app.ui.components.SubjectGridCard // <-- IMPORT
import com.pilotprep.app.util.SharedData

@Composable
fun SubjectSelectionScreen(navController: NavController, license: String, mode: String) {
    val title = if (mode == "practice") "Mise en Pratique" else "Simulation d'Examen"

    val isPplExam = license == "PPL" && mode == "exam"

    val subjects = when {
        license == "PPL" && mode == "practice" -> SharedData.pplPracticeSubjects
        license == "CPL" || license == "ATPL" -> SharedData.allSubjects
        else -> emptyList()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Licence: $license", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(24.dp))

        if (isPplExam) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Ça va être disponible bientôt",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // --- MODIFICATION ICI : Remplacer LazyColumn par LazyVerticalGrid ---
            LazyVerticalGrid(
                columns = GridCells.Fixed(2), // 2 colonnes pour plus de lisibilité
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(subjects) { subject ->
                    // On utilise notre nouveau composant de carte
                    SubjectGridCard(
                        title = subject,
                        onClick = {
                            navController.navigate("quiz/$license/$subject/$mode")
                        }
                    )
                }
            }
        }
    }
}