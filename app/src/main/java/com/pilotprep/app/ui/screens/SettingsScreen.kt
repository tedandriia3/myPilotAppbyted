package com.pilotprep.app.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pilotprep.app.viewmodels.AuthViewModel
import com.pilotprep.app.viewmodels.SettingsViewModel
import com.pilotprep.app.viewmodels.ThemeOption

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
) {
    // On récupère le AuthViewModel pour avoir les infos de l'utilisateur
    val authViewModel: AuthViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    val uiState by viewModel.uiState.collectAsState()
    var showThemeDialog by remember { mutableStateOf(false) }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = uiState.selectedTheme,
            onDismiss = { showThemeDialog = false },
            onThemeSelected = { theme ->
                viewModel.onThemeSelected(theme)
                showThemeDialog = false
            }
        )
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // --- Section Compte MISE À JOUR ---
        item {
            SettingsRow(
                icon = Icons.Default.AccountCircle,
                title = "Compte",
                subtitle = if (authState.isLoggedIn) authState.user?.displayName ?: "Utilisateur connecté" else "Ajouter un compte",
                onClick = {
                    if (authState.isLoggedIn) {
                        // Si connecté, on va au bon tableau de bord
                        when (authState.userRole) {
                            "ADMIN" -> navController.navigate("establishment_dashboard")
                            "PERSONAL" -> navController.navigate("personal_dashboard")
                        }
                    } else {
                        // Si pas connecté, on va à la page de sélection
                        navController.navigate("auth_landing")
                    }
                }
            )
        }
        item { Divider() }

        // --- Section Thème ---
        item {
            SettingsRow(
                icon = Icons.Default.Palette,
                title = "Thème",
                subtitle = uiState.selectedTheme.name.lowercase().replaceFirstChar { it.uppercaseChar() },
                onClick = { showThemeDialog = true }
            )
        }

        // --- Section Limite des données ---
        item {
            SettingsSwitchRow(
                icon = Icons.Default.NetworkCell,
                title = "Limiter l'utilisation des données cellulaires",
                subtitle = "Pour la mise à jour du contenu et des médias",
                checked = uiState.isDataSaverEnabled,
                onCheckedChange = { viewModel.onDataSaverToggled(it) }
            )
        }
        item { Divider() }

        // --- Section À propos ---
        item {
            SettingsRow(
                icon = Icons.Default.Info,
                title = "À propos de l'application",
                onClick = { navController.navigate("about") }
            )
        }

        // --- Section Maintenance ---
        item { Divider() }
        item {
            SettingsRow(
                icon = Icons.Default.CleaningServices,
                title = "Nettoyage des données",
                subtitle = "Corriger les contenus dupliqués",
                onClick = { navController.navigate("data_cleanup") }
            )
        }
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: ThemeOption,
    onDismiss: () -> Unit,
    onThemeSelected: (ThemeOption) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thème") },
        text = {
            Column {
                val themeOptions = mapOf(
                    ThemeOption.SYSTEM to "Valeur par défaut du système",
                    ThemeOption.LIGHT to "Clair",
                    ThemeOption.DARK to "Foncé"
                )
                themeOptions.forEach { (option, text) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(option) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (option == currentTheme),
                            onClick = { onThemeSelected(option) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}