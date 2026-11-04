package com.pilotprep.app.ui.screens.audio

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.horizontalScroll
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pilotprep.app.ui.screens.atc.AudioPlayer
import com.pilotprep.app.viewmodels.PodcastViewModel
import kotlinx.coroutines.delay
import androidx.compose.animation.core.*
import com.pilotprep.app.util.AudioPlayerManager
import androidx.compose.runtime.collectAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Slider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PodcastPlayerScreen(
    navController: NavController,
    audioPlayerManager: AudioPlayerManager
) {
    // On récupère le ViewModel partagé au niveau de l'activité
    val viewModel: PodcastViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playbackState by audioPlayerManager.playbackState.collectAsState()
    val podcast = uiState.selectedPodcast

    if (podcast == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Aucun podcast sélectionné. Retournez en arrière pour en choisir un.")
        }
        return
    }

    LaunchedEffect(podcast.id) {
        if (playbackState.currentAudioId != podcast.id) {
            audioPlayerManager.prepare(podcast.audioUrl, podcast.id)
        }
    }

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { 
                    ScrollingTitle(
                        text = podcast.title,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            ) 
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Suppression du titre dupliqué - il est déjà dans la TopAppBar
            Spacer(Modifier.height(16.dp))
            Text(
                text = podcast.description, 
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(Modifier.height(32.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Slider(
                            value = playbackState.position.toFloat(),
                            onValueChange = { audioPlayerManager.seekTo(it.toLong()) },
                            valueRange = 0f..(playbackState.duration.toFloat().takeIf { it > 0 } ?: 0f),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = playbackState.duration > 0
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formatTime(playbackState.position),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = formatTime(playbackState.duration),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { audioPlayerManager.seekTo((playbackState.position - 5000).coerceAtLeast(0L)) }, enabled = playbackState.duration > 0) {
                                Icon(Icons.Default.FastRewind, contentDescription = "Reculer 5s")
                            }
                            IconButton(onClick = { if (playbackState.isPlaying) audioPlayerManager.pause() else audioPlayerManager.play(podcast.audioUrl, podcast.id) }, enabled = true) {
                                Icon(
                                    if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (playbackState.isPlaying) "Pause" else "Lecture",
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                            IconButton(onClick = { audioPlayerManager.seekTo((playbackState.position + 5000).coerceAtMost(playbackState.duration)) }, enabled = playbackState.duration > 0) {
                                Icon(Icons.Default.FastForward, contentDescription = "Avancer 5s")
                            }
                        }
                    }
                    IconButton(
                        onClick = {
                            audioPlayerManager.seekTo(0L)
                            if (!playbackState.isPlaying) {
                                audioPlayerManager.play(podcast.audioUrl, podcast.id)
                            }
                        },
                        enabled = playbackState.duration > 0
                    ) {
                        Icon(Icons.Default.Replay, contentDescription = "Rejouer l'audio")
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioPlayerControls(audioPlayerManager: AudioPlayerManager, playbackState: AudioPlayerManager.PlaybackState, podcast: com.pilotprep.app.data.model.Podcast) {
    val durationMinutes = (playbackState.duration / 1000 / 60).toInt()
    val progress = if (playbackState.duration > 0) playbackState.position.toFloat() / playbackState.duration else 0f
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        IconButton(onClick = { audioPlayerManager.seekTo((playbackState.position - 15000).coerceAtLeast(0)) }) {
            Icon(Icons.Filled.Replay10, contentDescription = null)
        }
        IconButton(onClick = {
            if (playbackState.isPlaying) audioPlayerManager.pause() else audioPlayerManager.play(podcast.audioUrl, podcast.id)
        }) {
            Icon(
                if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (playbackState.isPlaying) "Pause" else "Play"
            )
        }
        IconButton(onClick = { audioPlayerManager.seekTo((playbackState.position + 15000).coerceAtMost(playbackState.duration)) }) {
            Icon(Icons.Filled.Forward10, contentDescription = null)
        }
        Text(text = "${durationMinutes}min", modifier = Modifier.padding(start = 8.dp))
    }
    LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth())
}

@Composable
private fun ScrollingTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var isScrolling by remember { mutableStateOf(false) }
    
    // Démarrer l'animation automatique pour les titres longs
    LaunchedEffect(text) {
        if (text.length > 25) { // Seulement pour les titres assez longs
            while (true) {
                delay(2000) // Attendre 2 secondes
                isScrolling = true
                // Animation lente avec easing pour ralentir progressivement
                scrollState.animateScrollTo(
                    scrollState.maxValue,
                    animationSpec = tween(
                        durationMillis = 10000, // 10 secondes pour aller à la fin (encore plus lent)
                        easing = EaseInOutCubic // Ralentit au début et à la fin
                    )
                )
                delay(2000) // Rester à la fin 2 secondes
                // Animation lente de retour avec easing
                scrollState.animateScrollTo(
                    0,
                    animationSpec = tween(
                        durationMillis = 10000, // 10 secondes pour revenir au début (encore plus lent)
                        easing = EaseInOutCubic // Ralentit au début et à la fin
                    )
                )
                delay(1000) // Pause avant de recommencer
                isScrolling = false
            }
        }
    }
    
    Row(
        modifier = modifier.horizontalScroll(scrollState),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        // Ajouter un espace pour permettre le défilement complet
        if (text.length > 25) {
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.0f) // Invisible mais prend de l'espace
            )
        }
    }
}

private fun formatTime(timeInMillis: Long): String {
    val totalSeconds = timeInMillis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}