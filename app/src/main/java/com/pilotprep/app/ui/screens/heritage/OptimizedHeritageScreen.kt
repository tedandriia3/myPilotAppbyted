package com.pilotprep.app.ui.screens.heritage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.pilotprep.app.data.model.Aircraft
import com.pilotprep.app.viewmodels.OptimizedAircraftViewModel

/**
 * Écran du patrimoine aéronautique optimisé avec cache intelligent et gestion de la mémoire
 */
@Composable
fun OptimizedHeritageScreen(
    navController: NavController,
    viewModel: OptimizedAircraftViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    // Charger les données au démarrage
    LaunchedEffect(Unit) {
        viewModel.loadAllAircraft()
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Barre de recherche optimisée
        SearchBar(
            query = searchQuery,
            onQueryChange = viewModel::searchAircraft,
            modifier = Modifier.padding(16.dp)
        )

        // Filtres par catégorie
        CategoryFilter(
            selectedCategory = selectedCategory,
            onCategorySelected = viewModel::filterByCategory,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Liste des avions
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Erreur: ${uiState.error}",
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.filteredAircraft) { aircraft ->
                    AircraftCard(
                        aircraft = aircraft,
                        onClick = {
                            navController.navigate("aircraft_detail/${aircraft.id}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text("Rechercher un avion...") },
        modifier = modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Composable
private fun CategoryFilter(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("", "Commercial", "Militaire", "Privé", "Vintage")
    
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            FilterChip(
                onClick = { onCategorySelected(category) },
                label = { Text(category.ifBlank { "Tous" }) },
                selected = selectedCategory == category
            )
        }
    }
}

@Composable
private fun AircraftCard(
    aircraft: Aircraft,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Image optimisée
            AsyncImage(
                model = aircraft.imageUrl,
                contentDescription = aircraft.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Informations de l'avion
            Text(
                text = aircraft.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = aircraft.year,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (aircraft.description.isNotBlank()) {
                Text(
                    text = aircraft.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}
