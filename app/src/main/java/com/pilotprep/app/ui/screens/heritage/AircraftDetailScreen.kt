package com.pilotprep.app.ui.screens.heritage

import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.pilotprep.app.R
import com.pilotprep.app.viewmodels.HeritageViewModel
import com.pilotprep.app.ui.print.PrintableContentRegistry
import com.pilotprep.app.ui.print.PrintablePayload

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AircraftDetailScreen(
    navController: NavController,
    aircraftId: String // On garde l'ID au cas où (pour un rechargement futur, par ex.)
) {
    // On récupère la MÊME instance du ViewModel partagé
    val viewModel: HeritageViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
    val selectedAircraft by viewModel.selectedAircraft.collectAsState()
    val isLoadingAircraft by viewModel.isLoadingAircraft.collectAsState()
    
    // Si l'avion n'est pas chargé, on le charge depuis Firebase
    LaunchedEffect(aircraftId) {
        if (selectedAircraft?.id != aircraftId) {
            viewModel.loadAircraftById(aircraftId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedAircraft?.name ?: "Détails") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            isLoadingAircraft -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Animation Lottie de chargement
                        val composition by rememberLottieComposition(
                            spec = LottieCompositionSpec.RawRes(R.raw.lottie_loading_airplane)
                        )
                        LottieAnimation(
                            composition = composition,
                            iterations = LottieConstants.IterateForever,
                            modifier = Modifier.size(200.dp)
                        )
                        Text(
                            text = "Chargement de l'avion...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            selectedAircraft == null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Avion introuvable",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "L'avion avec l'ID \"$aircraftId\" n'a pas été trouvé.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Button(onClick = { navController.popBackStack() }) {
                            Text("Retour")
                        }
                    }
                }
            }
            else -> {
            // Fournit une version HTML imprimable de la fiche technique
            val aircraft = selectedAircraft!!
            val html = remember(aircraft) {
                buildString {
                    append("<html><head><meta charset=\"utf-8\"/>")
                    append("<style>@page{margin:18mm;} body{font-family:sans-serif; font-size:12pt; line-height:1.5;} h1{font-size:18pt;margin:0 0 8pt;} h2{font-size:14pt;margin:14pt 0 6pt;} p{margin:0 0 8pt;} table{border-collapse:collapse;width:100%;margin:8pt 0;} td{padding:6pt;border-bottom:1px solid #ddd;} img{max-width:100%;height:auto;margin:8pt 0; color:#444}</style>")
                    append("</head><body>")
                    append("<h1>").append(aircraft.name).append("</h1>")
                    if (!aircraft.imageUrl.isNullOrBlank()) {
                        append("<img src=\"").append(aircraft.imageUrl).append("\" alt=\"").append(aircraft.name).append("\"/>")
                    }
                    append("<table>")
                    append("<tr><td>Année</td><td>").append(aircraft.year).append("</td></tr>")
                    append("<tr><td>Moteur</td><td>").append(aircraft.engine).append("</td></tr>")
                    append("<tr><td>Vitesse Maximale</td><td>").append(aircraft.maxSpeed).append("</td></tr>")
                    append("<tr><td>Envergure</td><td>").append(aircraft.wingspan).append("</td></tr>")
                    append("</table>")
                    append("<p>").append(aircraft.description).append("</p>")
                    append("<footer style='font-size:10pt; color:#666; margin-top:12pt;'>Pilot Prep • ")
                    append(java.time.LocalDate.now().toString())
                    append("</footer>")
                    append("</body></html>")
                }
            }
            LaunchedEffect(html) {
                PrintableContentRegistry.setProvider {
                    PrintablePayload(html = html, jobName = "PilotPrep-" + aircraft.name)
                }
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                item {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = selectedAircraft!!.imageUrl,
                            placeholder = painterResource(id = R.drawable.ic_heritage)
                        ),
                        contentDescription = selectedAircraft!!.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                item {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = selectedAircraft!!.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        DetailRow(label = "Année", value = selectedAircraft!!.year)
                        DetailRow(label = "Moteur", value = selectedAircraft!!.engine)
                        DetailRow(label = "Vitesse Maximale", value = selectedAircraft!!.maxSpeed)
                        DetailRow(label = "Envergure", value = selectedAircraft!!.wingspan)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = selectedAircraft!!.description,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    } // Fin du when
    } // Fin du lambda du Scaffold
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = "$label: ",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}