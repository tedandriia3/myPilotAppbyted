package com.pilotprep.app.ui.screens.heritage

import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.pilotprep.app.R
import com.pilotprep.app.data.model.Aircraft
import com.pilotprep.app.ui.components.PullToRefreshLottie
import com.pilotprep.app.viewmodels.HeritageUiState
import com.pilotprep.app.viewmodels.HeritageViewModel

@Composable
fun HeritageScreen(
    navController: NavController
) {
    val viewModel: HeritageViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // On extrait la liste des avions et l'état de chargement de l'état global
    val aircraftList = when (val state = uiState) {
        is HeritageUiState.Success -> state.aircraftList
        else -> emptyList() // On utilise une liste vide pour les autres états
    }
    val isLoading = uiState is HeritageUiState.Loading

    PullToRefreshLottie(
        isRefreshing = isLoading,
        onRefresh = { viewModel.fetchAircraft(forceRefresh = true) }
    ) {
        // On affiche TOUJOURS la grille. Elle sera vide au premier chargement,
        // puis contiendra les données. Pendant un "pull-to-refresh", elle
        // restera visible sous l'animation.
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(aircraftList) { aircraft ->
                AircraftCard(
                    aircraft = aircraft,
                    onClick = {
                        viewModel.findAircraftById(aircraft.id)
                        navController.navigate("aircraft_detail/${aircraft.id}")
                    }
                )
            }
        }

        // On gère l'état d'erreur séparément, par-dessus la grille vide
        if (uiState is HeritageUiState.Error) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Erreur de chargement: ${(uiState as HeritageUiState.Error).message}")
            }
        }
    }
}

@Composable
fun AircraftCard(aircraft: Aircraft, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = aircraft.imageUrl,
                    placeholder = painterResource(id = R.drawable.ic_heritage)
                ),
                contentDescription = aircraft.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = aircraft.name,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}