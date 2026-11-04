package com.pilotprep.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pilotprep.app.BuildConfig
import com.pilotprep.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("À propos de l'application") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // --- MODIFICATION ICI ---
                AboutItem(title = "Version de l'application", value = "1.0 (Build ${BuildConfig.VERSION_CODE})")
                // --- FIN DE LA MODIFICATION ---
            }
            item {
                AboutItem(title = "Version Android minimale", value = "Android 8.0 (API 26)")
            }
            item {
                AboutItem(title = "Concepteur", value = "Andriantsalama Tojo Edina")
            }
            item {
                AboutItem(title = "Licences", value = "Les licences open source seront listées ici.")
            }
            
            // Espace avant l'image de l'entreprise
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
            
            // Logo de l'entreprise
            item {
                CompanyLogo()
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun AboutItem(title: String, value: String) {
    Column {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun CompanyLogo() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo de l'entreprise - Image depuis res/drawable
        Image(
            painter = painterResource(id = R.drawable.logo_entreprise),
            contentDescription = "Logo de l'entreprise",
            modifier = Modifier.size(width = 120.dp, height = 120.dp),
            contentScale = ContentScale.Fit
        )
        
        // Nom de l'entreprise
        Spacer(modifier = Modifier.height(15.dp))
        Text(
            text = "'Za hiketrika miaraka amin'i Natolotra",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}