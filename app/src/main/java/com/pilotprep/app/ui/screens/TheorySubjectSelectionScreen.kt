package com.pilotprep.app.ui.screens.theory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pilotprep.app.ui.components.SubjectGridCard
import com.pilotprep.app.util.SharedData

@Composable
fun TheorySubjectSelectionScreen(navController: NavController, category: String) {
    // Redirection automatique pour Need to Know
    LaunchedEffect(category) {
        if (category == "need_to_know") {
            navController.navigate("need_to_know_list") {
                popUpTo("theory_selection") { inclusive = false }
            }
        }
    }

    val (title, subjects) = when (category) {
        "summary" -> "Résumé" to SharedData.summarySubjects
        "need_to_know" -> "Need To Know" to emptyList() // Redirection automatique
        else -> "Inconnu" to emptyList()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Théorie du vol", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Catégorie: $title", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(24.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(subjects) { subject ->
                SubjectGridCard(
                    title = subject,
                    onClick = {
                        navController.navigate("review_sheet/$category/$subject")
                    }
                )
            }
        }
    }
}