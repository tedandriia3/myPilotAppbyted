package com.pilotprep.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Liste avec chargement paresseux optimisé
 */
@Composable
fun <T> LazyLoadingList(
    items: List<T>,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    itemContent: @Composable (T, Int) -> Unit,
    loadingContent: @Composable () -> Unit = { DefaultLoadingItem() },
    emptyContent: @Composable () -> Unit = { DefaultEmptyItem() },
    errorContent: @Composable (String) -> Unit = { DefaultErrorItem(it) },
    isLoading: Boolean = false,
    hasError: Boolean = false,
    errorMessage: String = "",
    onLoadMore: (() -> Unit)? = null,
    loadMoreThreshold: Int = 5
) {
    val visibleItemsInfo = remember { mutableStateOf<List<Int>>(emptyList()) }
    
    // Détecter quand on approche de la fin de la liste
    LaunchedEffect(state.layoutInfo.visibleItemsInfo) {
        val lastVisibleIndex = state.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        val totalItems = state.layoutInfo.totalItemsCount
        
        if (lastVisibleIndex >= totalItems - loadMoreThreshold && onLoadMore != null) {
            onLoadMore()
        }
    }

    when {
        hasError -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                errorContent(errorMessage)
            }
        }
        items.isEmpty() && !isLoading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                emptyContent()
            }
        }
        else -> {
            LazyColumn(
                state = state,
                modifier = modifier,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items.size) { index ->
                    itemContent(items[index], index)
                }
                
                if (isLoading) {
                    item {
                        loadingContent()
                    }
                }
            }
        }
    }
}

/**
 * Item de chargement par défaut
 */
@Composable
private fun DefaultLoadingItem() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Chargement...",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Item vide par défaut
 */
@Composable
private fun DefaultEmptyItem() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Aucun élément trouvé",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Essayez de rafraîchir ou de modifier vos critères de recherche",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Item d'erreur par défaut
 */
@Composable
private fun DefaultErrorItem(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Erreur de chargement",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Composable pour le chargement progressif avec pagination
 */
@Composable
fun <T> ProgressiveLoadingList(
    items: List<T>,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    hasMoreItems: Boolean = true,
    onLoadMore: () -> Unit,
    itemContent: @Composable (T, Int) -> Unit,
    loadingItemCount: Int = 3
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items.size) { index ->
            itemContent(items[index], index)
        }
        
        if (isLoading && hasMoreItems) {
            items(loadingItemCount) {
                DefaultLoadingItem()
            }
        }
    }
    
    // Détecter quand charger plus d'éléments
    LaunchedEffect(items.size) {
        if (items.isNotEmpty() && hasMoreItems && !isLoading) {
            delay(100) // Petit délai pour éviter les appels trop fréquents
            onLoadMore()
        }
    }
}


