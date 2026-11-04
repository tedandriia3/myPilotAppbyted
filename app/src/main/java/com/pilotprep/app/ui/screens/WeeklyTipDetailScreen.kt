package com.pilotprep.app.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.pilotprep.app.R
import com.pilotprep.app.viewmodels.WeeklyTipViewModel
import com.pilotprep.app.data.model.WeeklyTip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyTipDetailScreen(
    tipId: String,
    navController: NavController,
    viewModel: WeeklyTipViewModel = hiltViewModel()
) {
    val detailUiState by viewModel.detailUiState.collectAsStateWithLifecycle()
    
    // Charger l'astuce spécifique
    LaunchedEffect(tipId) {
        viewModel.loadTipDetail(tipId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Astuce de la Semaine") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshWeeklyTip() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualiser")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val currentState = detailUiState) {
            is com.pilotprep.app.viewmodels.WeeklyTipDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
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
                        Text("Chargement de l'astuce...")
                    }
                }
            }
            
            is com.pilotprep.app.viewmodels.WeeklyTipDetailUiState.Success -> {
                val tip = currentState.tip
                WeeklyTipDetailContent(
                    tip = tip,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            
            is com.pilotprep.app.viewmodels.WeeklyTipDetailUiState.Error -> {
                val errorMessage = currentState.message
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "❌ Erreur",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Button(onClick = { viewModel.refreshWeeklyTip() }) {
                            Text("Réessayer")
                        }
                    }
                }
            }
            
            is com.pilotprep.app.viewmodels.WeeklyTipDetailUiState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "⚠️ Aucune astuce",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = "Aucune astuce trouvée pour cette semaine",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Button(onClick = { viewModel.refreshWeeklyTip() }) {
                            Text("Actualiser")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyTipDetailContent(
    tip: WeeklyTip,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // En-tête de l'astuce
        item {
            // Utiliser une couleur de fond claire pour le thème clair
            val backgroundColor = if (isSystemInDarkTheme()) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
            
            // Utiliser une couleur de texte appropriée
            val textColor = if (isSystemInDarkTheme()) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = backgroundColor
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = tip.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                            text = buildMarkdownInline(tip.introduction),
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Semaine ${tip.weekOfYear}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor
                        )
                        Text(
                            text = tip.category,
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor
                        )
                    }
                }
            }
        }
        
        // Contenu principal
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Contenu de l'astuce",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    WeeklyTipMarkdown(
                        text = tip.fullContent,
                        h2ColorHex = tip.h2ColorHex
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyTipMarkdown(text: String, h2ColorHex: String) {
    val blocks = remember(text) { com.pilotprep.app.util.parseWeeklyTipMarkdownBlocks(text) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        blocks.forEach { b ->
            when (b) {
                is com.pilotprep.app.util.WeeklyTipBlock.H2 -> Text(
                    text = buildMarkdownInline(b.text),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = parseHexOr(MaterialTheme.colorScheme.primary, h2ColorHex)
                )
                is com.pilotprep.app.util.WeeklyTipBlock.Paragraph -> Text(
                    text = buildMarkdownInline(b.text),
                    style = MaterialTheme.typography.bodyLarge
                )
                is com.pilotprep.app.util.WeeklyTipBlock.UnorderedList -> {
                    Column { b.items.forEach { item -> Text(buildMarkdownInline(item)) } }
                }
            }
        }
    }
}

@Composable
private fun buildMarkdownInline(text: String) = buildAnnotatedString {
    if (text.isEmpty()) return@buildAnnotatedString
    var i = 0
    while (i < text.length) {
        if (i + 1 < text.length && text[i] == '*' && text[i + 1] == '*') {
            val start = i + 2
            val end = text.indexOf("**", start)
            if (end > start) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(text.substring(start, end)) }
                i = end + 2
                continue
            }
        }
        if (text[i] == '*') {
            val start = i + 1
            val end = text.indexOf('*', start)
            if (end > start) {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(text.substring(start, end)) }
                i = end + 1
                continue
            }
        }
        if (text[i] == '_') {
            val start = i + 1
            val end = text.indexOf('_', start)
            if (end > start) {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(text.substring(start, end)) }
                i = end + 1
                continue
            }
        }
        append(text[i])
        i++
    }
}

@Composable
private fun parseHexOr(themeColor: Color, hex: String?): Color {
    val value = hex?.trim().orEmpty()
    if (value.isEmpty()) return themeColor
    return try { Color(android.graphics.Color.parseColor(value)) } catch (_: IllegalArgumentException) { themeColor }
}
