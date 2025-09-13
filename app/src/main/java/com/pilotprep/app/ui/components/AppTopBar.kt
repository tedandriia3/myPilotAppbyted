package com.pilotprep.app.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(title: String) {
    var menuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current // On récupère le contexte pour les actions (Toast, Intent)

    TopAppBar(
        title = { Text(text = title) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        actions = {
            // Icônes de la barre (inchangées)
            IconButton(onClick = { /* TODO: Logique pour les favoris */ }) {
                Icon(Icons.Default.PushPin, contentDescription = "Favoris")
            }
            IconButton(onClick = { /* TODO: Logique pour la recherche */ }) {
                Icon(Icons.Default.Search, contentDescription = "Recherche")
            }
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Options")
            }

            // --- MISE À JOUR DU MENU DÉROULANT ---
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                // 1. Paramètres (anciennement Réglages)
                DropdownMenuItem(
                    text = { Text("Paramètres") },
                    onClick = {
                        Toast.makeText(context, "Bientôt disponible : Écran des paramètres", Toast.LENGTH_SHORT).show()
                        menuExpanded = false
                        // TODO: navController.navigate("settings")
                    },
                    leadingIcon = { Icon(Icons.Default.Settings, contentDescription = "Paramètres") }
                )

                // 2. Historique
                DropdownMenuItem(
                    text = { Text("Historique") },
                    onClick = {
                        Toast.makeText(context, "Bientôt disponible : Écran de l'historique", Toast.LENGTH_SHORT).show()
                        menuExpanded = false
                        // TODO: navController.navigate("history")
                    },
                    leadingIcon = { Icon(Icons.Default.History, contentDescription = "Historique") }
                )

                Divider() // Un séparateur visuel

                // 3. Options d'affichage
                DropdownMenuItem(
                    text = { Text("Options d'affichage") },
                    onClick = {
                        Toast.makeText(context, "Bientôt disponible : Options d'affichage", Toast.LENGTH_SHORT).show()
                        menuExpanded = false
                        // TODO: Afficher une boîte de dialogue pour les options
                    },
                    leadingIcon = { Icon(Icons.Default.TextFormat, contentDescription = "Options d'affichage") }
                )

                // 4. Langue
                DropdownMenuItem(
                    text = { Text("Langue") },
                    onClick = {
                        Toast.makeText(context, "Bientôt disponible : Changement de langue", Toast.LENGTH_SHORT).show()
                        menuExpanded = false
                        // TODO: Afficher une boîte de dialogue pour choisir la langue
                    },
                    leadingIcon = { Icon(Icons.Default.Language, contentDescription = "Langue") }
                )

                Divider() // Un séparateur visuel

                // 5. Transmettre (Partager)
                DropdownMenuItem(
                    text = { Text("Transmettre") },
                    onClick = {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Découvrez Pilot Prep, l'application ultime pour préparer vos examens de pilote ! [Lien vers le store]")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Partager via")
                        context.startActivity(shareIntent)
                        menuExpanded = false
                    },
                    leadingIcon = { Icon(Icons.Default.Share, contentDescription = "Transmettre") }
                )

                // 6. Imprimer
                DropdownMenuItem(
                    text = { Text("Imprimer") },
                    onClick = {
                        Toast.makeText(context, "Bientôt disponible : Fonction d'impression", Toast.LENGTH_SHORT).show()
                        menuExpanded = false
                        // TODO: Implémenter la logique d'impression avec PrintManager
                    },
                    leadingIcon = { Icon(Icons.Default.Print, contentDescription = "Imprimer") }
                )
            }
        }
    )
}