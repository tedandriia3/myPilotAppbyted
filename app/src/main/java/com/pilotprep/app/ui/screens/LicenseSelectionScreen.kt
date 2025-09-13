package com.pilotprep.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun LicenseSelectionScreen(navController: NavController, mode: String) {
    val title = if (mode == "practice") "Mise en Pratique" else "Simulation d'Examen"

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Choisissez votre licence", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(32.dp))

            val licenses = listOf("PPL", "CPL", "ATPL")
            licenses.forEach { license ->
                Button(
                    onClick = { navController.navigate("subject_selection/$license/$mode") },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text(license)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}