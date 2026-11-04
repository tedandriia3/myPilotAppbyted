package com.pilotprep.app.ui.screens.auth

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pilotprep.app.viewmodels.AuthViewModel

@Composable
private fun EditDisplayNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifier le pseudo") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Pseudo") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name.trim()) }) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalDashboardScreen(
    navController: NavController
) {
    // On récupère le ViewModel partagé pour avoir les infos de l'utilisateur
    val viewModel: AuthViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
    val authState by viewModel.uiState.collectAsStateWithLifecycle()
    val user = authState.user

    LaunchedEffect(Unit) {
        viewModel.refreshCurrentUser()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mon Compte") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                authState.isLoading -> {
                    Spacer(Modifier.height(32.dp))
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text("Chargement des informations du compte...", style = MaterialTheme.typography.bodyMedium)
                }
                user != null -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.displayName ?: "Utilisateur",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    var showEditName by remember { mutableStateOf(false) }
                    IconButton(onClick = { showEditName = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Modifier le pseudo")
                    }
                    if (showEditName) {
                        EditDisplayNameDialog(
                            currentName = user.displayName ?: "",
                            onDismiss = { showEditName = false },
                            onConfirm = { newName ->
                                if (newName.isNotBlank()) {
                                    viewModel.changeDisplayName(newName)
                                }
                                showEditName = false
                            }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                // Avatar (photo URL si disponible)
                val photoUrl = user.photoUrl?.toString() ?: ""
                if (photoUrl.isNotBlank()) {
                    com.pilotprep.app.util.OptimizedAvatarImage(
                        imageUrl = photoUrl,
                        contentDescription = "Avatar utilisateur",
                        size = 72.dp
                    )
                    Spacer(Modifier.height(12.dp))
                }
                Text(
                    text = user.email ?: "",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(32.dp))

                // Lier "Ma Progression" au module "Mon Progrès"
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Ma Progression", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Text("Consultez vos statistiques détaillées et votre historique d'étude.", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { navController.navigate("progress_dashboard/${user.uid}") }) {
                            Text("Voir Mon Progrès")
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f)) // Pousse le bouton vers le bas

                // Changer le mot de passe (simple)
                var newPassword by remember { mutableStateOf("") }
                var passwordVisible by rememberSaveable { mutableStateOf(false) }
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Nouveau mot de passe") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (passwordVisible) "Masquer le mot de passe" else "Afficher le mot de passe"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { if (newPassword.length >= 6) viewModel.changePassword(newPassword) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = newPassword.length >= 6
                ) { Text("Mettre à jour le mot de passe") }
                Spacer(Modifier.height(16.dp))

                // Option d'export supprimée

                // Supprimer le compte
                Button(
                    onClick = {
                        viewModel.deleteAccount {
                            navController.navigate("home") { popUpTo(0) { inclusive = true } }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Supprimer mon compte") }
                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.signOut()
                        // On retourne à l'accueil après la déconnexion
                        navController.navigate("home") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Se déconnecter")
                }
                }
                else -> {
                    Text(
                        text = "Aucune information de compte disponible.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = {
                        navController.navigate("auth_landing") {
                            popUpTo("home")
                        }
                    }) {
                        Text("Se connecter")
                    }
                }
            }
        }
    }
}