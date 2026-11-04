package com.pilotprep.app.ui.screens.bookmarks

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
import com.pilotprep.app.viewmodels.BookmarkViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkScreen(
    navController: NavController,
    bookmarkViewModel: BookmarkViewModel = hiltViewModel()
) {
    val uiState by bookmarkViewModel.uiState.collectAsState()
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar avec titre et actions
        BookmarkTopBar(
            onBackClick = { navController.popBackStack() },
            onDeleteAllClick = { showDeleteAllDialog = true },
            onSelectionModeToggle = bookmarkViewModel::toggleSelectionMode,
            isSelectionMode = uiState.isSelectionMode,
            selectedCount = uiState.selectedBookmarks.size,
            totalBookmarks = uiState.totalBookmarks
        )

        // Filtres par catégorie
        BookmarkCategoryFilters(
            selectedCategory = uiState.filterCategory,
            onCategoryChange = bookmarkViewModel::updateFilterCategory,
            onClearFilter = { bookmarkViewModel.updateFilterCategory(null) }
        )

        // Barre de recherche
        BookmarkSearchBar(
            query = uiState.searchQuery,
            onQueryChange = bookmarkViewModel::updateSearchQuery
        )

        // Contenu principal
        when {
            uiState.isLoading -> {
                BookmarkLoadingIndicator()
            }
            uiState.bookmarks.isEmpty() -> {
                BookmarkEmptyState(
                    hasFilter = uiState.filterCategory != null || uiState.searchQuery.isNotEmpty()
                )
            }
            else -> {
                BookmarkContent(
                    bookmarks = uiState.bookmarks,
                    isSelectionMode = uiState.isSelectionMode,
                    selectedBookmarks = uiState.selectedBookmarks,
                    onBookmarkClick = { bookmark -> bookmarkViewModel.onBookmarkClick(bookmark, navController) },
                    onBookmarkLongClick = { bookmark -> bookmarkViewModel.onBookmarkLongClick(bookmark) },
                    onBookmarkDelete = bookmarkViewModel::deleteBookmark,
                    onSelectionToggle = bookmarkViewModel::toggleBookmarkSelection
                )
            }
        }
    }

    // Dialog de confirmation pour supprimer tout
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Supprimer tous les signets") },
            text = { Text("Êtes-vous sûr de vouloir supprimer tous vos signets ? Cette action est irréversible.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        bookmarkViewModel.clearAllBookmarks()
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
private fun BookmarkTopBar(
    onBackClick: () -> Unit,
    onDeleteAllClick: () -> Unit,
    onSelectionModeToggle: () -> Unit,
    isSelectionMode: Boolean,
    selectedCount: Int,
    totalBookmarks: Int
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

                    Column {
                        Text(
                            text = if (isSelectionMode) {
                                if (selectedCount > 0) "$selectedCount sélectionné${if (selectedCount > 1) "s" else ""}"
                                else "Sélection"
                            } else {
                                "Mes Signets"
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        if (!isSelectionMode && totalBookmarks > 0) {
                            Text(
                                text = "$totalBookmarks leçon${if (totalBookmarks > 1) "s" else ""} marquée${if (totalBookmarks > 1) "s" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row {
                    if (!isSelectionMode && totalBookmarks > 0) {
                        IconButton(onClick = onDeleteAllClick) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Supprimer tout",
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
private fun BookmarkCategoryFilters(
    selectedCategory: BookmarkCategory?,
    onCategoryChange: (BookmarkCategory?) -> Unit,
    onClearFilter: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filtrer par module",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )

                if (selectedCategory != null) {
                    TextButton(onClick = onClearFilter) {
                        Text("Effacer")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(BookmarkCategory.values()) { category ->
                    FilterChip(
                        onClick = {
                            onCategoryChange(
                                if (selectedCategory == category) null else category
                            )
                        },
                        label = {
                            Text(
                                text = category.displayName,
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        selected = selectedCategory == category,
                        leadingIcon = {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = category.color
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = category.color,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun BookmarkSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = {
                Text("Rechercher dans vos signets...")
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Rechercher"
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Effacer"
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}

@Composable
private fun BookmarkContent(
    bookmarks: List<Bookmark>,
    isSelectionMode: Boolean,
    selectedBookmarks: Set<String>,
    onBookmarkClick: (Bookmark) -> Unit,
    onBookmarkLongClick: (Bookmark) -> Unit,
    onBookmarkDelete: (Bookmark) -> Unit,
    onSelectionToggle: (Bookmark) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = bookmarks,
            key = { it.id }
        ) { bookmark ->
            BookmarkCard(
                bookmark = bookmark,
                isSelected = selectedBookmarks.contains(bookmark.id),
                isSelectionMode = isSelectionMode,
                onClick = {
                    if (isSelectionMode) {
                        onSelectionToggle(bookmark)
                    } else {
                        onBookmarkClick(bookmark)
                    }
                },
                onLongClick = { onBookmarkLongClick(bookmark) },
                onDelete = { onBookmarkDelete(bookmark) },
                onSelectionToggle = onSelectionToggle
            )
        }

        // Espacement en bas
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun BookmarkCard(
    bookmark: Bookmark,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit,
    onSelectionToggle: (Bookmark) -> Unit
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
            // Checkbox de sélection
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onSelectionToggle(bookmark) },
                    modifier = Modifier.padding(end = 12.dp)
                )
            }

            // Icône de la catégorie
            Surface(
                color = bookmark.category.color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = bookmark.category.icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = bookmark.category.color
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Contenu principal
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = bookmark.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (!bookmark.description.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = bookmark.description,
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
                    // Module
                    Text(
                        text = bookmark.module,
                        style = MaterialTheme.typography.labelSmall,
                        color = bookmark.category.color,
                        fontWeight = FontWeight.Medium
                    )

                    // Date d'ajout
                    Text(
                        text = bookmark.createdAt.toBookmarkDateString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Progression
                    if (bookmark.progress > 0f) {
                        Text(
                            text = "${(bookmark.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Dernier accès
                if (bookmark.lastAccessed > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Dernier accès: ${bookmark.lastAccessed.toLastAccessedString()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Menu d'actions
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
private fun BookmarkLoadingIndicator() {
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
                text = "Chargement de vos signets...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BookmarkEmptyState(hasFilter: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = if (hasFilter) Icons.Default.SearchOff else Icons.Default.BookmarkBorder,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = if (hasFilter) "Aucun signet trouvé" else "Aucun signet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = if (hasFilter) {
                    "Aucun signet ne correspond à vos critères de recherche. Essayez de modifier vos filtres."
                } else {
                    "Vos leçons marquées de la Théorie du Vol apparaîtront ici. Utilisez le bouton signet dans les leçons pour en ajouter."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
