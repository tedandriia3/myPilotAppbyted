package com.pilotprep.app.ui.sessions

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.pilotprep.app.viewmodels.NavSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionGrid(
    sessions: List<NavSession>,
    onSelect: (String) -> Unit,
    onClose: (String) -> Unit,
    onAddNew: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Vos Écrans") }) },
        floatingActionButton = {
            LargeFloatingActionButton(onClick = onAddNew) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter un nouvel écran")
            }
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(sessions) { index, session ->
                Card(
                    onClick = { onSelect(session.id) },
                    modifier = Modifier.aspectRatio(0.8f),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (session.preview != null) {
                            Image(
                                bitmap = session.preview!!,
                                contentDescription = "Aperçu de l'écran ${index + 1}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                "Écran ${index + 1}",
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        if (sessions.size > 1) {
                            IconButton(
                                onClick = { onClose(session.id) },
                                modifier = Modifier.align(Alignment.TopEnd),
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                )
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Fermer l'écran")
                            }
                        }
                    }
                }
            }
        }
    }
}