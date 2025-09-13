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
fun TheorySelectionScreen(navController: NavController) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Théorie du vol", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Choisissez votre licence", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(32.dp))

            val licenses = listOf("PPL", "CPL", "ATPL")
            licenses.forEach { license ->
                Button(
                    onClick = { navController.navigate("theory_subject_selection/$license") },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text(license)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}