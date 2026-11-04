package com.pilotprep.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pilotprep.app.util.SharedData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectGridCard(
    title: String, 
    onClick: () -> Unit,
    license: String = "",
    mode: String = ""
) {
    val examConfig = if (mode == "exam") {
        SharedData.getExamConfigFor(license, title)
    } else null

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(if (examConfig != null) 140.dp else 120.dp), // Plus haut si infos d'examen
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge
            )
            
            // Afficher les informations d'examen si disponibles
            examConfig?.let { config ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${config.questionCount} questions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = SharedData.formatDuration(config.durationMinutes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}