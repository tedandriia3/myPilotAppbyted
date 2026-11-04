package com.pilotprep.app.ui.screens.history

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pilotprep.app.viewmodels.HistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    historyViewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by historyViewModel.uiState.collectAsState()
    val selectedFilter by historyViewModel.selectedFilter.collectAsState()
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar avec titre et actions
        HistoryTopBar(
            onBackClick = { navController.popBackStack() },
            onDeleteAllClick = { showDeleteAllDialog = true },
            onSelectionModeToggle = historyViewModel::toggleSelectionMode,
            isSelectionMode = uiState.isSelectionMode,
            selectedCount = uiState.selectedEntries.size
        )

        // Filtres
        HistoryFilters(
            selectedFilter = selectedFilter,
            onFilterChange = historyViewModel::updateFilter
        )

        // Contenu principal
        when {
            uiState.isLoading -> {
                HistoryLoadingIndicator()
            }
            uiState.entries.isEmpty() -> {
                HistoryEmptyState()
            }
            else -> {
                HistoryContent(
                    entries = uiState.entries,
                    isSelectionMode = uiState.isSelectionMode,
                    selectedEntries = uiState.selectedEntries,
                    onEntryClick = { entry -> historyViewModel.onEntryClick(entry, navController) },
                    onEntryLongClick = { entry -> historyViewModel.onEntryLongClick(entry) },
                    onEntryDelete = historyViewModel::deleteEntry,
                    onSelectionToggle = historyViewModel::toggleEntrySelection,
                    onLoadMore = historyViewModel::loadMoreEntries
                )
            }
        }
    }

    // Dialog de confirmation pour supprimer tout l'historique
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Effacer tout l'historique") },
            text = { Text("Êtes-vous sûr de vouloir supprimer tout votre historique de consultation ? Cette action est irréversible.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        historyViewModel.clearAllHistory()
                        showDeleteAllDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryTopBar(
    onBackClick: () -> Unit,
    onDeleteAllClick: () -> Unit,
    onSelectionModeToggle: () -> Unit,
    isSelectionMode: Boolean,
    selectedCount: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }

                    Text(
                        text = if (isSelectionMode) {
                            if (selectedCount > 0) "$selectedCount sélectionné${if (selectedCount > 1) "s" else ""}"
                            else "Sélection"
                        } else {
                            "Historique"
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row {
                    if (!isSelectionMode) {
                        IconButton(onClick = onDeleteAllClick) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Effacer tout",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    IconButton(onClick = onSelectionModeToggle) {
                        Icon(
                            imageVector = if (isSelectionMode) Icons.Default.Close else Icons.Default.Checklist,
                            contentDescription = if (isSelectionMode) "Annuler la sélection" else "Mode sélection"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryFilters(
    selectedFilter: HistoryFilter,
    onFilterChange: (HistoryFilter) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        LazyRow(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(HistoryFilter.values()) { filter ->
                FilterChip(
                    onClick = { onFilterChange(filter) },
                    label = { Text(filter.displayName) },
                    selected = selectedFilter == filter,
                    leadingIcon = {
                        Icon(
                            imageVector = filter.icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}

@Composable
private fun HistoryContent(
    entries: List<HistoryEntry>,
    isSelectionMode: Boolean,
    selectedEntries: Set<String>,
    onEntryClick: (HistoryEntry) -> Unit,
    onEntryLongClick: (HistoryEntry) -> Unit,
    onEntryDelete: (HistoryEntry) -> Unit,
    onSelectionToggle: (HistoryEntry) -> Unit,
    onLoadMore: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = entries,
            key = { it.id }
        ) { entry ->
            HistoryEntryCard(
                entry = entry,
                isSelected = selectedEntries.contains(entry.id),
                isSelectionMode = isSelectionMode,
                onClick = {
                    if (isSelectionMode) {
                        onSelectionToggle(entry)
                    } else {
                        onEntryClick(entry)
                    }
                },
                onLongClick = { onEntryLongClick(entry) },
                onDelete = { onEntryDelete(entry) },
                onSelectionToggle = onSelectionToggle
            )
        }

        // Bouton "Charger plus" si nécessaire
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onLoadMore,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text("Charger plus d'entrées")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun HistoryEntryCard(
    entry: HistoryEntry,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit,
    onSelectionToggle: (HistoryEntry) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox de sélection (visible en mode sélection)
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onSelectionToggle(entry) },
                    modifier = Modifier.padding(end = 12.dp)
                )
            }

            // Icône du type de contenu
            Surface(
                color = entry.contentType.color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = entry.contentType.icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = entry.contentType.color
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Contenu principal
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (!entry.description.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = entry.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Type de contenu
                    Text(
                        text = "${entry.contentType.emoji} ${entry.contentType.displayName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = entry.contentType.color,
                        fontWeight = FontWeight.Medium
                    )

                    // Date relative
                    Text(
                        text = entry.timestamp.toRelativeTimeString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Progression (si applicable)
                    if (entry.progress > 0f) {
                        Text(
                            text = "${(entry.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Menu d'actions (visible hors mode sélection)
            if (!isSelectionMode) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryLoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Chargement de l'historique...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HistoryEmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Aucun historique",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Votre historique de consultation apparaîtra ici lorsque vous commencerez à explorer le contenu.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
