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
fun AtcTalkSelectionScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "ATC Talk",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Choisissez un régime de vol",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(48.dp))

        // Bouton pour VFR
        Button(
            onClick = {
                // Pour l'instant, on navigue vers un placeholder
                navController.navigate("atc_talk_content/VFR")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            Text("VFR", style = MaterialTheme.typography.titleLarge)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bouton pour IFR
        Button(
            onClick = {
                // Pour l'instant, on navigue vers un placeholder
                navController.navigate("atc_talk_content/IFR")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            Text("IFR", style = MaterialTheme.typography.titleLarge)
        }
    }
}