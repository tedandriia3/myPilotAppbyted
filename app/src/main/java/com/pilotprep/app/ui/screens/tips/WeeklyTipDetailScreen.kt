package com.pilotprep.app.ui.screens.tips

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pilotprep.app.data.model.WeeklyTip
import com.pilotprep.app.viewmodels.WeeklyTipDetailUiState
import com.pilotprep.app.viewmodels.WeeklyTipViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyTipDetailScreen(
    navController: NavController,
    tipId: String
) {
    val viewModel: WeeklyTipViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
    val detailUiState by viewModel.detailUiState.collectAsStateWithLifecycle()

    // Charger les détails de l'astuce
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
                    IconButton(onClick = { /* TODO: Implémenter le partage */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Partager")
                    }
                }
            )
        }
    ) { paddingValues ->
        val currentState = detailUiState
        when (currentState) {
            is WeeklyTipDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Chargement de l'astuce...")
                    }
                }
            }
            is WeeklyTipDetailUiState.Success -> {
                WeeklyTipDetailContent(
                    tip = currentState.tip,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is WeeklyTipDetailUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Erreur de chargement",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { navController.popBackStack() }) {
                            Text("Retour")
                        }
                    }
                }
            }
            is WeeklyTipDetailUiState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Astuce non trouvée",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { navController.popBackStack() }) {
                            Text("Retour")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyTipDetailContent(
    tip: WeeklyTip,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // En-tête avec informations de la semaine
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Semaine",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Semaine ${tip.weekOfYear}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (tip.category.isNotEmpty()) {
                            Text(
                                text = tip.category,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        // Image principale (si disponible)
        if (tip.imageUrl.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(tip.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Image de l'astuce",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // Titre principal
        item {
            Text(
                text = tip.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Contenu complet (seulement si introduction existe)
        if (tip.introduction.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Introduction",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = buildMarkdownInline(tip.introduction),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                        )
                    }
                }
            }
        }

        // Contenu détaillé
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Astuce détaillée",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val contentText = if (tip.fullContent.isNotEmpty()) {
                        tip.fullContent
                    } else {
                        "Contenu détaillé non disponible pour cette astuce."
                    }
                    
                    WeeklyTipMarkdown(
                        text = contentText,
                        h2ColorHex = tip.h2ColorHex
                    )
                }
            }
        }

        // Pied de page avec informations de mise à jour
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Astuce de la Semaine",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Semaine ${tip.weekOfYear} • Cycle de 52 semaines",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyTipMarkdown(text: String, h2ColorHex: String) {
    val blocks = remember(text) { parseWeeklyTipMarkdown(text) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        blocks.forEach { b ->
            when (b) {
                is WTBlock.H2 -> Text(
                    text = buildMarkdownInline(b.text),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = parseHexOr(MaterialTheme.colorScheme.primary, h2ColorHex)
                )
                is WTBlock.Paragraph -> Text(
                    text = buildMarkdownInline(b.text),
                    style = MaterialTheme.typography.bodyLarge
                )
                is WTBlock.UnorderedList -> {
                    Column { b.items.forEach { item -> Text(buildMarkdownInline(item)) } }
                }
            }
        }
    }
}

private sealed interface WTBlock {
    data class H2(val text: String) : WTBlock
    data class Paragraph(val text: String) : WTBlock
    data class UnorderedList(val items: List<String>) : WTBlock
}

private fun parseWeeklyTipMarkdown(raw: String): List<WTBlock> {
    if (raw.isBlank()) return emptyList()
    val lines = raw.lines()
    val blocks = mutableListOf<WTBlock>()
    var ulBuffer = mutableListOf<String>()
    fun flushUl() { if (ulBuffer.isNotEmpty()) { blocks.add(WTBlock.UnorderedList(ulBuffer.toList())); ulBuffer = mutableListOf() } }
    for (line in lines) {
        val t = line.trim()
        if (t.isEmpty()) { flushUl(); continue }
        when {
            t.startsWith("## ") -> { flushUl(); blocks.add(WTBlock.H2(t.removePrefix("## ").trim())) }
            t.startsWith("- ") || t.startsWith("* ") -> ulBuffer.add(t.drop(2).trim())
            else -> { flushUl(); blocks.add(WTBlock.Paragraph(t)) }
        }
    }
    flushUl()
    return blocks
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
