package com.pilotprep.app.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.pilotprep.app.viewmodels.BookmarkViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    navController: NavController,
    currentLessonId: String? = null,
    bookmarkViewModel: BookmarkViewModel = hiltViewModel(),
    showPrint: Boolean = false,
    onPrintClick: (() -> Unit)? = null
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // État du bouton signet
    val isBookmarked = currentLessonId?.let { bookmarkViewModel.isBookmarked(it) } ?: false

    TopAppBar(
        title = { Text(text = title) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        actions = {
            // Bouton Imprimer (conditionnel)
            if (showPrint && onPrintClick != null) {
                IconButton(onClick = { onPrintClick() }) {
                    Icon(Icons.Default.Print, contentDescription = "Imprimer")
                }
            }
            // Bouton Signet (visible uniquement pour les leçons)
            currentLessonId?.let { lessonId ->
                IconButton(
                    onClick = {
                        if (isBookmarked) {
                            bookmarkViewModel.removeBookmark(lessonId)
                            Toast.makeText(
                                context,
                                "Signet supprimé",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            bookmarkViewModel.addBookmark(
                                lessonId = lessonId,
                                title = title,
                                module = "Théorie du Vol", // À adapter selon le contexte
                                route = "lesson_detail/$lessonId"
                            )
                            Toast.makeText(
                                context,
                                "Leçon ajoutée aux signets !",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (isBookmarked) "Retirer des signets" else "Ajouter aux signets",
                        tint = if (isBookmarked) Color(0xFFFFC107) else MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            // Bouton Recherche
            IconButton(onClick = { navController.navigate("search") }) {
                Icon(Icons.Default.Search, contentDescription = "Recherche")
            }

            // Menu Options
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Options")
            }

            // Menu déroulant
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Paramètres") },
                    onClick = {
                        navController.navigate("settings")
                        menuExpanded = false
                    },
                    leadingIcon = { Icon(Icons.Default.Settings, contentDescription = "Paramètres") }
                )

                DropdownMenuItem(
                    text = { Text("Historique") },
                    onClick = {
                        navController.navigate("history")
                        menuExpanded = false
                    },
                    leadingIcon = { Icon(Icons.Default.History, contentDescription = "Historique") }
                )

                DropdownMenuItem(
                    text = { Text("Mes Signets") },
                    onClick = {
                        navController.navigate("bookmarks")
                        menuExpanded = false
                    },
                    leadingIcon = { Icon(Icons.Default.Bookmark, contentDescription = "Signets") }
                )

                DropdownMenuItem(
                    text = { Text("Langue") },
                    onClick = {
                        Toast.makeText(context, "Bientôt disponible : Changement de langue", Toast.LENGTH_SHORT).show()
                        menuExpanded = false
                    },
                    leadingIcon = { Icon(Icons.Default.Language, contentDescription = "Langue") }
                )

                Divider()

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

                if (showPrint && onPrintClick != null) {
                    DropdownMenuItem(
                        text = { Text("Imprimer") },
                        onClick = {
                            onPrintClick()
                            menuExpanded = false
                        },
                        leadingIcon = { Icon(Icons.Default.Print, contentDescription = "Imprimer") }
                    )
                }
            }
        }
    )
}