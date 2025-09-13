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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pilotprep.app.ui.components.SubjectGridCard // <-- IMPORT
import com.pilotprep.app.util.SharedData

@Composable
fun TheorySubjectSelectionScreen(navController: NavController, license: String) {
    val subjects = when (license) {
        "PPL" -> SharedData.pplTheorySubjects
        "CPL" -> SharedData.cplTheorySubjects
        "ATPL" -> SharedData.atplTheorySubjects
        else -> emptyList()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Théorie du vol", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Licence: $license", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(24.dp))

        // --- MODIFICATION ICI : Remplacer LazyColumn par LazyVerticalGrid ---
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), // 2 colonnes pour les sous-menus
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(subjects) { subject ->
                // On utilise notre nouveau composant de carte
                SubjectGridCard(
                    title = subject,
                    onClick = {
                        navController.navigate("review_sheet/$license/$subject")
                    }
                )
            }
        }
    }
}