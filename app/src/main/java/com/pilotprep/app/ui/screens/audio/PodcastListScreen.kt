package com.pilotprep.app.ui.screens.audio

import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize // <-- L'IMPORT MANQUANT
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pilotprep.app.viewmodels.PodcastViewModel
import com.pilotprep.app.ui.components.MarshallerLoadingAnimation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PodcastListScreen(
    navController: NavController
) {
    val viewModel: PodcastViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Voix du Ciel") })
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    MarshallerLoadingAnimation()
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    items(uiState.podcasts) { podcast ->
                        ListItem(
                            headlineContent = { 
                                Text(
                                    text = podcast.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            trailingContent = { 
                                Text(
                                    text = podcast.duration,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            modifier = Modifier.clickable {
                                viewModel.selectPodcast(podcast)
                                navController.navigate("podcast_player")
                            }
                        )
                        Divider()
                    }
                }
            }
        }
    }
}