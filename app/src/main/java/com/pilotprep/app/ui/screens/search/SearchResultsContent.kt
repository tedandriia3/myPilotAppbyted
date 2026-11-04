package com.pilotprep.app.ui.screens.search

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun SearchResultsContent(
    results: List<SearchResult>,
    filters: List<SearchFilter>,
    selectedFilters: Set<String>,
    onFilterChange: (String, Boolean) -> Unit,
    onResultClick: (SearchResult) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Barre de filtres
        SearchFiltersBar(
            filters = filters,
            selectedFilters = selectedFilters,
            onFilterChange = onFilterChange
        )

        // Informations sur les résultats
        SearchResultsInfo(
            totalResults = results.size,
            queryTime = "0.2s" // Simulé
        )

        // Résultats de recherche
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(results) { result ->
                SearchResultCard(
                    result = result,
                    onClick = { onResultClick(result) }
                )
            }

            // Message de fin
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Fin des résultats",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SearchFiltersBar(
    filters: List<SearchFilter>,
    selectedFilters: Set<String>,
    onFilterChange: (String, Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
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
                    text = "Filtres",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                if (selectedFilters.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            filters.forEach { filter ->
                                if (selectedFilters.contains(filter.id)) {
                                    onFilterChange(filter.id, false)
                                }
                            }
                        }
                    ) {
                        Text("Effacer tout")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    FilterChip(
                        onClick = {
                            onFilterChange(filter.id, !selectedFilters.contains(filter.id))
                        },
                        label = {
                            Text(
                                text = filter.displayName,
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        selected = selectedFilters.contains(filter.id),
                        leadingIcon = if (filter.icon != null) {
                            {
                                Icon(
                                    imageVector = filter.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultsInfo(
    totalResults: Int,
    queryTime: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$totalResults résultat${if (totalResults > 1) "s" else ""} trouvé${if (totalResults > 1) "s" else ""}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Recherche en $queryTime",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SearchResultCard(
    result: SearchResult,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // En-tête avec catégorie et favori
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = result.category.icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = result.category.color
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = result.category.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        color = result.category.color,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = { /* Toggle favorite */ }
                ) {
                    Icon(
                        imageVector = if (result.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Ajouter aux favoris",
                        tint = if (result.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Titre
            Text(
                text = result.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Extrait avec mise en évidence
            Text(
                text = result.excerpt,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Métadonnées
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Niveau de difficulté
                    DifficultyChip(level = result.difficultyLevel)

                    Spacer(modifier = Modifier.width(8.dp))

                    // Type de média
                    MediaTypeChip(type = result.mediaType)
                }

                Text(
                    text = result.lastUpdated,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Score de pertinence (optionnel)
            if (result.relevanceScore > 0.8f) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFFFC107)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Très pertinent",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFFC107),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun DifficultyChip(level: DifficultyLevel) {
    val (text, color) = when (level) {
        DifficultyLevel.BEGINNER -> "Basique" to Color(0xFF4CAF50)
        DifficultyLevel.INTERMEDIATE -> "Intermédiaire" to Color(0xFFFF9800)
        DifficultyLevel.ADVANCED -> "Avancé" to Color(0xFFF44336)
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun MediaTypeChip(type: MediaType) {
    val (text, color) = when (type) {
        MediaType.TEXT -> "Texte" to Color(0xFF2196F3)
        MediaType.VIDEO -> "Vidéo" to Color(0xFFE91E63)
        MediaType.IMAGE -> "Image" to Color(0xFF9C27B0)
        MediaType.QUIZ -> "Quiz" to Color(0xFF3F51B5)
        MediaType.AUDIO -> "Audio" to Color(0xFFFF5722)
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
