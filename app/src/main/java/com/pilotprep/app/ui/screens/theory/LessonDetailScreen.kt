package com.pilotprep.app.ui.screens.theory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import com.pilotprep.app.ui.components.MarshallerLoadingAnimation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.animation.core.*
import kotlinx.coroutines.delay
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pilotprep.app.viewmodels.TheoryViewModel
import com.pilotprep.app.ui.print.PrintableContentRegistry
import com.pilotprep.app.ui.print.PrintablePayload
import com.pilotprep.app.ui.screens.ZoomableImageDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonDetailScreen(
    navController: NavController,
    lessonId: String,
) {
    val viewModel: TheoryViewModel = hiltViewModel()

    val lesson by viewModel.selectedLesson.collectAsState()
    val error by viewModel.error.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(lessonId) {
        viewModel.findLessonById(lessonId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    ScrollingTitle(
                        text = (lesson?.title as? String) ?: "Leçon",
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            error != null -> {
                ErrorContent(paddingValues = paddingValues, message = error ?: "") {
                    viewModel.clearError()
                    navController.popBackStack()
                }
            }
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    MarshallerLoadingAnimation()
                }
            }
            lesson == null -> {
                NotFoundContent(paddingValues = paddingValues) {
                    navController.popBackStack()
                }
            }
            else -> {
                // Enregistre un provider de contenu imprimable (HTML) pour cette leçon
                val lessonTitle = (lesson?.title as? String) ?: "Leçon"
                val html = remember(lesson) {
                    buildString {
                        append("<html><head><meta charset=\"utf-8\"/>")
                        append("<style>@page{margin:18mm;} body{font-family:sans-serif; font-size:12pt; line-height:1.5;} h1{font-size:18pt;margin:0 0 8pt;} h2{font-size:14pt;margin:14pt 0 6pt;} p{margin:0 0 8pt;} ul,ol{margin:0 0 8pt 18pt;} img{max-width:100%;height:auto;margin:8pt 0;}</style>")
                        append("</head><body>")
                        append("<header style='position:running(header);'></header>")
                        append("<h1>").append(lessonTitle).append("</h1>")
                        val contentAny = lesson?.content
                        val markdownText = (contentAny as? String)
                        val structuredItemsAny: List<Any?> = (contentAny as? List<*>)?.toList().orEmpty()
                        val mapAny = (contentAny as? Map<*, *>)
                        val nestedListAny: List<Any?> = when (val nested = mapAny?.get("blocks") ?: mapAny?.get("items")) {
                            is List<*> -> nested.toList()
                            else -> emptyList()
                        }

                        if (!markdownText.isNullOrBlank()) {
                            // Rendu simple: paragraphs only (les images markdown ![]() ne sont pas résolues ici)
                            markdownText.lines().forEach { line ->
                                val t = line.trim()
                                when {
                                    t.startsWith("## ") -> append("<h2>").append(t.removePrefix("## ").trim()).append("</h2>")
                                    t.startsWith("- ") -> append("<ul><li>").append(t.removePrefix("- ").trim()).append("</li></ul>")
                                    t.isNotBlank() -> append("<p>").append(t).append("</p>")
                                }
                            }
                        } else if (structuredItemsAny.isNotEmpty() || nestedListAny.isNotEmpty()) {
                            val source = if (structuredItemsAny.isNotEmpty()) structuredItemsAny else nestedListAny
                            source.forEach { any ->
                                val map = any as? Map<*, *>
                                val type = map?.get("type") as? String
                                val text = map?.get("text")?.toString()
                                val url = map?.get("url")?.toString()
                                when (type) {
                                    "h2" -> append("<h2>").append(text ?: "").append("</h2>")
                                    "p" -> append("<p>").append(text ?: "").append("</p>")
                                    "img" -> append("<img src=\"").append(url ?: text ?: "").append("\" />")
                                    "table" -> {
                                        val headers = (map?.get("headers") as? List<*>)?.mapNotNull { it?.toString() }.orEmpty()
                                        val rows = (map?.get("rows") as? List<*>)?.mapNotNull { row ->
                                            (row as? List<*>)?.mapNotNull { it?.toString() }
                                        }.orEmpty()
                                        append("<table style='width:100%;border-collapse:collapse;margin:8pt 0;'>")
                                        if (headers.isNotEmpty()) {
                                            append("<thead><tr>")
                                            headers.forEach { h ->
                                                append("<th style='border:1px solid #ccc;padding:6pt;text-align:left;background:#f7f7f7;'>").append(h).append("</th>")
                                            }
                                            append("</tr></thead>")
                                        }
                                        append("<tbody>")
                                        rows.forEach { r ->
                                            append("<tr>")
                                            val cells = if (headers.isNotEmpty()) padRowToSize(r, headers.size) else r
                                            cells.forEach { c ->
                                                append("<td style='border:1px solid #ccc;padding:6pt;'>").append(c).append("</td>")
                                            }
                                            append("</tr>")
                                        }
                                        append("</tbody></table>")
                                    }
                                    "ul" -> {
                                        val items = (map?.get("items") as? List<*>)?.mapNotNull { it?.toString() }.orEmpty()
                                        append("<ul>")
                                        items.forEach { append("<li>").append(it).append("</li>") }
                                        append("</ul>")
                                    }
                                    "ol" -> {
                                        val items = (map?.get("items") as? List<*>)?.mapNotNull { it?.toString() }.orEmpty()
                                        append("<ol>")
                                        items.forEach { append("<li>").append(it).append("</li>") }
                                        append("</ol>")
                                    }
                                }
                            }
                        }
                        append("<footer style='position:running(footer); font-size:10pt; color:#666; margin-top:12pt;'>Pilot Prep • ")
                        append(java.time.LocalDate.now().toString())
                        append("</footer>")
                        append("</body></html>")
                    }
                }
                LaunchedEffect(html, lessonTitle) {
                    PrintableContentRegistry.setProvider {
                        PrintablePayload(html = html, jobName = "PilotPrep-$lessonTitle")
                    }
                }
                var zoomImageUrl by remember { mutableStateOf<String?>(null) }
                val contentAny = lesson?.content
                val markdownText = (contentAny as? String)
                val structuredItemsAny: List<Any?> = (contentAny as? List<*>)?.toList().orEmpty()
                val mapAny = (contentAny as? Map<*, *>)
                val nestedListAny: List<Any?> = when (val nested = mapAny?.get("blocks") ?: mapAny?.get("items")) {
                    is List<*> -> nested.toList()
                    else -> emptyList()
                }

                when {
                    // A) Rendu markdown-like
                    !markdownText.isNullOrBlank() -> {
                        val blocks = remember(markdownText) { parseLessonTextToBlocks(markdownText) }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .padding(horizontal = 16.dp)
                        ) {
                            items(blocks) { block ->
                                when (block) {
                                    is RenderBlock.H2 -> {
                                        Text(
                                            text = buildMarkdownInline(block.text),
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                                        )
                                    }
                                    is RenderBlock.Paragraph -> {
                                        Text(
                                            text = buildMarkdownInline(block.text),
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                    }
                                    is RenderBlock.ThemedParagraph -> {
                                        Text(
                                            text = block.text,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = getThemeColor(theme = block.theme),
                                            fontWeight = if (block.theme == "warning") FontWeight.Bold else FontWeight.Normal,
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                    }
                                    is RenderBlock.Image -> {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(block.url)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = block.alt,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { zoomImageUrl = block.url }
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                    is RenderBlock.UnorderedList -> {
                                        Column(modifier = Modifier.padding(bottom = 12.dp)) {
                                            block.items.forEach { item ->
                                                Row(modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)) {
                                                    Text("• ", fontWeight = FontWeight.Bold)
                                                    Text(
                                                        text = buildMarkdownInline(item),
                                                        style = MaterialTheme.typography.bodyLarge
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    is RenderBlock.OrderedList -> {
                                        Column(modifier = Modifier.padding(bottom = 12.dp)) {
                                            block.items.forEachIndexed { index, item ->
                                                Row(modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)) {
                                                    Text("${index + 1}. ", fontWeight = FontWeight.Bold)
                                                    Text(
                                                        text = buildMarkdownInline(item),
                                                        style = MaterialTheme.typography.bodyLarge
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            item { Spacer(modifier = Modifier.height(24.dp)) }
                        }
                    }

                    // B) Rendu structuré (liste d'items)
                    structuredItemsAny.isNotEmpty() || nestedListAny.isNotEmpty() -> {
                        val source = if (structuredItemsAny.isNotEmpty()) structuredItemsAny else nestedListAny
                        val items = remember(source) {
                            source.mapNotNull { any ->
                                when (any) {
                                    is Map<*, *> -> {
                                        val type = any["type"] as? String ?: return@mapNotNull null
                                        val text = any["text"]?.toString()
                                        val url = any["url"]?.toString()
                                        val alt = any["alt"]?.toString()
                                        val theme = any["theme"] as? String
                                        val colorHex = any["colorHex"]?.toString()
                                        val headers: List<String>? = (any["headers"] as? List<*>)?.mapNotNull { it?.toString() }
                                        val rows: List<List<String>>? = (any["rows"] as? List<*>)?.mapNotNull { row ->
                                            (row as? List<*>)?.mapNotNull { it?.toString() }
                                        }
                                        val rawItems = any["items"]
                                        val listItems: List<String>? = when (rawItems) {
                                            is List<*> -> rawItems.mapNotNull { it?.toString() }
                                            else -> null
                                        }
                                        ContentItem(
                                            type = type,
                                            text = text,
                                            url = url,
                                            alt = alt,
                                            items = listItems,
                                            theme = theme,
                                            colorHex = colorHex,
                                            headers = headers,
                                            rows = rows
                                        )
                                    }
                                    is ContentItem -> any
                                    else -> null
                                }
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .padding(horizontal = 16.dp)
                        ) {
                            items(items) { contentItem ->
                                when (contentItem.type) {
                                    "h2" -> {
                                        Text(
                                            text = buildMarkdownInline(contentItem.text ?: ""),
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                                            color = parseHexOr(
                                                themeColor = getThemeColor(theme = contentItem.theme).takeIf { it != Color.Unspecified }
                                                    ?: MaterialTheme.colorScheme.primary,
                                                hex = contentItem.colorHex
                                            )
                                        )
                                    }
                                    "p" -> {
                                        Text(
                                            text = buildMarkdownInline(contentItem.text ?: ""),
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.padding(bottom = 12.dp),
                                            color = parseHexOr(
                                                themeColor = getThemeColor(theme = contentItem.theme).takeIf { it != Color.Unspecified }
                                                    ?: MaterialTheme.colorScheme.onSurface,
                                                hex = contentItem.colorHex
                                            ),
                                            fontWeight = if (contentItem.theme == "warning") FontWeight.Bold else FontWeight.Normal,
                                        )
                                    }
                                    "table" -> {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        val headers = contentItem.headers.orEmpty()
                                        val rows = contentItem.rows.orEmpty()
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 12.dp)
                                        ) {
                                            if (headers.isNotEmpty()) {
                                                Row(Modifier.fillMaxWidth()) {
                                                    headers.forEach { h ->
                                                        Text(
                                                            text = h,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .padding(8.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            rows.forEach { r ->
                                                Row(Modifier.fillMaxWidth()) {
                                                    val cells = if (headers.isNotEmpty()) padRowToSize(r, headers.size) else r
                                                    cells.forEach { c ->
                                                        Text(
                                                            text = c,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .padding(8.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    "img" -> {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(contentItem.url ?: contentItem.text)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = contentItem.alt ?: contentItem.text,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { zoomImageUrl = contentItem.url ?: contentItem.text }
                                                .padding(vertical = 8.dp)
                                        )
                                    }
                                    "ul" -> {
                                        Column(modifier = Modifier.padding(bottom = 12.dp)) {
                                            contentItem.items.orEmpty().forEach { itemText ->
                                                Row(modifier = Modifier.padding(bottom = 6.dp)) {
                                                    Text("• ", style = MaterialTheme.typography.bodyLarge)
                                                    Text(
                                                        text = buildMarkdownInline(itemText),
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    "ol" -> {
                                        Column(modifier = Modifier.padding(bottom = 12.dp)) {
                                            contentItem.items.orEmpty().forEachIndexed { index, itemText ->
                                                Row(modifier = Modifier.padding(bottom = 6.dp)) {
                                                    Text("${index + 1}. ", style = MaterialTheme.typography.bodyLarge)
                                                    Text(
                                                        text = buildMarkdownInline(itemText),
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    else -> {
                                        Text(
                                            text = contentItem.text ?: "",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    }
                                }
                            }
                            item { Spacer(modifier = Modifier.height(24.dp)) }
                        }
                    }

                    // C) Fallback de debug
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .padding(16.dp)
                        ) {
                            item {
                                Text(
                                    text = "Contenu indisponible",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Aucune section reconnue (markdown, liste, ou blocs).",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = "DEBUG – Types:",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(8.dp))
                                Text("content class = ${contentAny?.let { it::class.qualifiedName } ?: "null"}")
                                Text("content toString = ${contentAny?.toString() ?: "null"}")
                                if (mapAny != null) {
                                    Text("map keys = ${mapAny.keys.joinToString()}")
                                    val nested = mapAny["blocks"] ?: mapAny["items"]
                                    Text("map[blocks|items] class = ${nested?.let { it::class.qualifiedName } ?: "null"}")
                                    Text("map[blocks|items] toString = ${nested?.toString() ?: "null"}")
                                }
                            }
                        }
                    }
                }
                if (zoomImageUrl != null) {
                    ZoomableImageDialog(
                        imageUrl = zoomImageUrl!!,
                        onDismiss = { zoomImageUrl = null }
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(
    paddingValues: PaddingValues,
    message: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Erreur",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onBack) { Text("Retour") }
    }
}

@Composable
private fun NotFoundContent(
    paddingValues: PaddingValues,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Leçon introuvable",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Cette leçon n'a pas pu être chargée. Vérifiez votre connexion internet et réessayez.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onBack) { Text("Retour") }
    }
}


@Composable
private fun getThemeColor(theme: String?): Color {
    return when (theme) {
        "warning" -> Color(0xFFD32F2F) // Un rouge Material Design pour les avertissements
        "info" -> Color(0xFF1976D2)    // Un bleu Material Design pour les informations
        else -> Color.Unspecified     // Utilise la couleur par défaut du thème de l'application
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
    return try {
        Color(android.graphics.Color.parseColor(value))
    } catch (_: IllegalArgumentException) {
        themeColor
    }
}

private data class ContentItem(
    val type: String,
    val text: String? = null,
    val url: String? = null,
    val alt: String? = null,
    val items: List<String>? = null,
    val theme: String? = null,
    val colorHex: String? = null,
    val headers: List<String>? = null,
    val rows: List<List<String>>? = null
)

private sealed interface RenderBlock {
    data class H2(val text: String) : RenderBlock
    data class Paragraph(val text: String) : RenderBlock
    data class ThemedParagraph(val text: String, val theme: String) : RenderBlock
    data class Image(val url: String, val alt: String?) : RenderBlock
    data class UnorderedList(val items: List<String>) : RenderBlock
    data class OrderedList(val items: List<String>) : RenderBlock
}

private fun parseLessonTextToBlocks(raw: String): List<RenderBlock> {
    if (raw.isBlank()) return emptyList()

    val lines = raw.lines()
    val blocks = mutableListOf<RenderBlock>()

    var ulBuffer = mutableListOf<String>()
    var olBuffer = mutableListOf<String>()

    fun flushLists() {
        if (ulBuffer.isNotEmpty()) {
            blocks.add(RenderBlock.UnorderedList(ulBuffer.toList()))
            ulBuffer = mutableListOf()
        }
        if (olBuffer.isNotEmpty()) {
            blocks.add(RenderBlock.OrderedList(olBuffer.toList()))
            olBuffer = mutableListOf()
        }
    }

    val imageRegex = Regex("""!\[(.*?)\]\((.*?)\)""")

    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) {
            flushLists()
            continue
        }

        when {
            trimmed.startsWith("## ") -> {
                flushLists()
                blocks.add(RenderBlock.H2(trimmed.removePrefix("## ").trim()))
            }
            trimmed.startsWith("!WARN> ") -> {
                flushLists()
                blocks.add(RenderBlock.ThemedParagraph(trimmed.removePrefix("!WARN> "), "warning"))
            }
            trimmed.startsWith("!INFO> ") -> {
                flushLists()
                blocks.add(RenderBlock.ThemedParagraph(trimmed.removePrefix("!INFO> "), "info"))
            }
            imageRegex.containsMatchIn(trimmed) -> {
                flushLists()
                val m = imageRegex.find(trimmed)
                if (m != null) {
                    val alt = m.groupValues.getOrNull(1)?.ifBlank { null }
                    val url = m.groupValues.getOrNull(2)?.trim().orEmpty()
                    if (url.isNotEmpty()) {
                        blocks.add(RenderBlock.Image(url = url, alt = alt))
                    }
                }
            }
            trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                if (olBuffer.isNotEmpty()) flushLists()
                ulBuffer.add(trimmed.drop(2).trim())
            }
            Regex("""\d+\.\s+.*""").matches(trimmed) -> {
                if (ulBuffer.isNotEmpty()) flushLists()
                val afterDot = trimmed.substringAfter(".").trim()
                olBuffer.add(afterDot)
            }
            else -> {
                flushLists()
                blocks.add(RenderBlock.Paragraph(trimmed))
            }
        }
    }

    flushLists()
    return blocks
}

private fun padRowToSize(row: List<String>, size: Int): List<String> {
    if (row.size >= size) return row.take(size)
    val result = row.toMutableList()
    repeat(size - row.size) { result.add("") }
    return result
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
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            // Ajouter un espace et une copie invisible pour permettre le défilement complet
            // L'espace doit être suffisant pour que le contenu dépasse toujours la largeur disponible
            Spacer(modifier = Modifier.width(200.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.0f) // Invisible mais prend de l'espace
            )
        }
    }
}