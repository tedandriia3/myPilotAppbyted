package com.pilotprep.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun HomeScreen(navController: NavController) {
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
                QuickLinkCard(title = "Cours", onClick = { navController.navigate("theory_selection") }, modifier = Modifier.weight(1f))
                QuickLinkCard(title = "Quiz", onClick = { navController.navigate("license_selection/practice") }, modifier = Modifier.weight(1f))
                QuickLinkCard(title = "Audio", onClick = { navController.navigate("audio") }, modifier = Modifier.weight(1f))
            }
        }

        // --- Astuce de la semaine ---
        item {
            SectionTitle(title = "Astuce de la semaine")
            InfoCard(title = "Bientôt disponible", content = "Revenez bientôt pour des conseils d'expert.")
        }

        // --- Image du jour ---
        item {
            SectionTitle(title = "Image du jour")
            InfoCard(title = "Patrimoine du Ciel", content = "Une image et une anecdote d'un avion célèbre apparaîtront ici chaque jour.")
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
fun QuickLinkCard(title: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(title, fontWeight = FontWeight.Bold)
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