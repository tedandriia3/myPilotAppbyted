package com.pilotprep.app.ui.screens.theory

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun TheorySelectionScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Théorie du vol",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Choisissez une section",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        // Le bouton "Résumé" ne change pas de comportement
        Button(
            onClick = { navController.navigate("theory_subject_selection/summary") },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            Text("Résumé", style = MaterialTheme.typography.titleLarge)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- MODIFICATION ICI ---
        // Le bouton "Need to know" navigue directement à la liste des leçons
        Button(
            onClick = { navController.navigate("review_sheet/need_to_know/all") },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            Text("Need To Know", style = MaterialTheme.typography.titleLarge)
        }
        // --- FIN DE LA MODIFICATION ---
    }
}