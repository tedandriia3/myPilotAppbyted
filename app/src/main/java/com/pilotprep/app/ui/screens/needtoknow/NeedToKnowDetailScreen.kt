package com.pilotprep.app.ui.screens.needtoknow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pilotprep.app.data.model.ContentBlock
import com.pilotprep.app.data.model.NeedToKnowContent
import com.pilotprep.app.ui.components.MarshallerLoadingAnimation
import com.pilotprep.app.viewmodels.NeedToKnowDetailViewModel
import androidx.compose.ui.graphics.toArgb
import androidx.compose.animation.core.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeedToKnowDetailScreen(
    contentId: String,
    navController: NavController,
    viewModel: NeedToKnowDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val content = uiState.content
    
    // Charger le contenu au démarrage
    LaunchedEffect(contentId) {
        viewModel.loadContent(contentId)
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        
        // Contenu principal
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    MarshallerLoadingAnimation()
                }
            }
            
            content == null -> {
                ErrorContent(
                    error = "Contenu non trouvé",
                    onRetry = { navController.popBackStack() }
                )
            }
            
            else -> {
                ContentDetail(content = content)
            }
        }
    }
}

@Composable
private fun ContentDetail(content: NeedToKnowContent) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // En-tête
        item {
            ContentHeader(content = content)
        }
        
        // Contenu
        items(content.content) { block ->
            ContentBlockItem(block = block)
        }
    }
}

@Composable
private fun ContentHeader(content: NeedToKnowContent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Titre statique et centré
            Text(
                text = content.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ScrollingTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    var containerWidth by remember { mutableStateOf(0) }
    
    // Démarrer l'animation automatique pour les titres longs
    LaunchedEffect(text, containerWidth) {
        // Attendre que le layout soit mesuré
        delay(500)
        
        // Vérifier si le contenu dépasse la largeur disponible
        if (scrollState.maxValue > 0) {
            while (true) {
                delay(2000) // Attendre 2 secondes au début
                // Animation lente vers la fin
                scrollState.animateScrollTo(
                    scrollState.maxValue,
                    animationSpec = tween(
                        durationMillis = 10000, // 10 secondes pour aller à la fin (encore plus lent)
                        easing = EaseInOutCubic
                    )
                )
                delay(2000) // Rester à la fin 2 secondes
                // Animation lente de retour
                scrollState.animateScrollTo(
                    0,
                    animationSpec = tween(
                        durationMillis = 10000, // 10 secondes pour revenir au début (encore plus lent)
                        easing = EaseInOutCubic
                    )
                )
                delay(1000) // Pause avant de recommencer
            }
        }
    }
    
    Box(
        modifier = modifier.onSizeChanged { size ->
            containerWidth = size.width
        }
    ) {
        Row(
            modifier = Modifier
                .then(
                    if (containerWidth > 0) {
                        val minWidthDp = with(density) { (containerWidth + 200).toDp() }
                        Modifier.widthIn(min = minWidthDp)
                    } else {
                        Modifier
                    }
                )
                .horizontalScroll(scrollState),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            // Ajouter un espace et une copie invisible pour permettre le défilement complet
            // L'espace doit être suffisant pour que le contenu dépasse toujours la largeur disponible
            Spacer(modifier = Modifier.width(200.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.0f) // Invisible mais prend de l'espace
            )
        }
    }
}


@Composable
private fun ContentBlockItem(block: ContentBlock) {
    when (block.type) {
        "h1" -> {
            Text(
                text = block.text,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        "h2" -> {
            Text(
                text = buildMarkdownInline(block.text),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = parseHexOr(themeColor = MaterialTheme.colorScheme.primary, hex = block.colorHex),
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }
        "h3" -> {
            Text(
                text = block.text,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        "p" -> {
            Text(
                text = buildMarkdownInline(block.text),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        "image" -> {
            if (block.imageUrl.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(block.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
        "ul", "ol" -> {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                block.items.forEach { item ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = if (block.type == "ul") "•" else "1.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = buildMarkdownInline(item),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        "quote" -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = buildMarkdownInline(block.text),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
        "warning" -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3CD)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Attention",
                        tint = Color(0xFF856404),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = buildMarkdownInline(block.text),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF856404),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        "tip" -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFD1ECF1)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = "Conseil",
                        tint = Color(0xFF0C5460),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = buildMarkdownInline(block.text),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF0C5460),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}


@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Error,
            contentDescription = "Erreur",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Erreur",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retour")
        }
    }
}

@Composable
private fun buildMarkdownInline(text: String) = buildAnnotatedString {
    if (text.isEmpty()) return@buildAnnotatedString
    var i = 0
    while (i < text.length) {
        if (i + 1 < text.length && text[i] == '*' && text[i + 1] == '*') {
            // bold **...**
            val start = i + 2
            val end = text.indexOf("**", start)
            if (end > start) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(text.substring(start, end))
                }
                i = end + 2
                continue
            }
        }
        if (text[i] == '*' ) {
            // italic *...*
            val start = i + 1
            val end = text.indexOf('*', start)
            if (end > start) {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(text.substring(start, end))
                }
                i = end + 1
                continue
            }
        }
        if (text[i] == '_' ) {
            // italic _..._
            val start = i + 1
            val end = text.indexOf('_', start)
            if (end > start) {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(text.substring(start, end))
                }
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
    return try {
        Color(android.graphics.Color.parseColor(value))
    } catch (e: IllegalArgumentException) {
        themeColor
    }
}
