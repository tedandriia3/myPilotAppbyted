package com.pilotprep.app.ui.screens.atc

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.pilotprep.app.R
import com.pilotprep.app.data.model.AtcExercise
import com.pilotprep.app.ui.components.PullToRefreshLottie
import com.pilotprep.app.viewmodels.AtcUiState
import com.pilotprep.app.viewmodels.AtcViewModel
import com.pilotprep.app.viewmodels.VerificationState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AtcExerciseScreen(
    navController: NavController,
    type: String,
    viewModel: AtcViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(type) {
        if (uiState.currentExercise == null) {
            viewModel.fetchExercises(type)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (uiState.currentExercise == null) "ATC Talk - $type" else uiState.currentExercise!!.title)
                },
                navigationIcon = {
                    if (uiState.currentExercise != null) {
                        IconButton(onClick = { viewModel.backToList() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Retour à la liste"
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            if (uiState.currentExercise == null) {
                PullToRefreshLottie(
                    isRefreshing = uiState.isLoading,
                    onRefresh = { viewModel.fetchExercises(type, forceRefresh = true) }
                ) {
                    ExerciseList(uiState = uiState, onExerciseSelected = { viewModel.selectExercise(it) })
                }
            } else {
                ExerciseDetail(uiState = uiState, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ExerciseList(uiState: AtcUiState, onExerciseSelected: (AtcExercise) -> Unit) {
    if (uiState.isLoading && uiState.exercises.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                    text = "Chargement des exercices...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(uiState.exercises) { exercise ->
                ListItem(
                    headlineContent = { Text(exercise.title) },
                    modifier = Modifier.clickable { onExerciseSelected(exercise) }
                )
                Divider()
            }
        }
    }
}

@Composable
fun ExerciseDetail(uiState: AtcUiState, viewModel: AtcViewModel) {
    val exercise = uiState.currentExercise!!
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        AudioPlayer(audioUrl = exercise.audioUrl)
        Spacer(Modifier.height(24.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            TranscriptionArea(uiState = uiState, onInputChange = { index, text -> viewModel.onUserInputChange(index, text) })
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                Button(
                    onClick = { viewModel.validateAnswers() },
                    enabled = uiState.verificationState == VerificationState.UNVERIFIED
                ) {
                    Text("Valider")
                }
                Button(
                    onClick = { viewModel.showSolutions() },
                    enabled = uiState.verificationState != VerificationState.SOLUTIONS_SHOWN
                ) {
                    Text("Vérifier")
                }
                Button(onClick = { viewModel.selectExercise(exercise) }) {
                    Text("Réessayer")
                }
            }
            if (uiState.verificationState == VerificationState.VALIDATED) {
                Text(
                    "Score: ${uiState.score} / ${exercise.solutions.size}",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun TranscriptionArea(uiState: AtcUiState, onInputChange: (Int, String) -> Unit) {
    val exercise = uiState.currentExercise!!
    val dialogueLines = exercise.transcriptTemplate.split("\n")
    var currentBlankIndex = 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            dialogueLines.forEach { line ->
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    mainAxisSpacing = 4.dp,
                    crossAxisSpacing = 8.dp,
                    crossAxisAlignment = FlowCrossAxisAlignment.Center
                ) {
                    val parts = line.split("_______")
                    parts.forEachIndexed { partIndex, part ->
                        Text(part, style = MaterialTheme.typography.bodyLarge)
                        if (partIndex < parts.size - 1) {
                            val blankIndex = currentBlankIndex++
                            BlankInputField(
                                value = uiState.userInputs.getOrElse(blankIndex) { "" },
                                onValueChange = { onInputChange(blankIndex, it) },
                                verificationState = uiState.verificationState,
                                isCorrect = uiState.correctionResults.getOrElse(blankIndex) { false },
                                correctAnswer = exercise.solutions.getOrElse(blankIndex) { "" }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BlankInputField(
    value: String,
    onValueChange: (String) -> Unit,
    verificationState: VerificationState,
    isCorrect: Boolean,
    correctAnswer: String
) {
    when (verificationState) {
        VerificationState.UNVERIFIED -> {
            NormalInputField(value = value, onValueChange = onValueChange)
        }
        VerificationState.VALIDATED -> {
            val color = if (isCorrect) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
            Text(
                text = value.ifEmpty { "_______" },
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
        VerificationState.SOLUTIONS_SHOWN -> {
            Text(
                text = correctAnswer,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF4CAF50)
            )
        }
    }
}

@Composable
private fun NormalInputField(value: String, onValueChange: (String) -> Unit) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .defaultMinSize(minWidth = 90.dp)
            .width(IntrinsicSize.Min)
            .padding(horizontal = 4.dp),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        ),
        singleLine = true,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { innerTextField ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.Center) {
                    if (value.isEmpty()) {
                        Text(
                            text = "_______",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray.copy(alpha = 0.5f)
                        )
                    }
                    innerTextField()
                }
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    )
}

@Composable
fun AudioPlayer(audioUrl: String) {
    val context = LocalContext.current

    val exoPlayer = remember(audioUrl) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(audioUrl))
            prepare()
        }
    }

    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    var isReady by remember { mutableStateOf(false) }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingValue: Boolean) {
                isPlaying = isPlayingValue
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    duration = exoPlayer.duration.coerceAtLeast(0L)
                    isReady = true
                }
                if (playbackState == Player.STATE_ENDED) {
                    currentPosition = 0L
                    isPlaying = false
                    exoPlayer.seekTo(0)
                    exoPlayer.pause()
                }
            }
            override fun onPlayerError(error: PlaybackException) {
                Log.e("AudioPlayerError", "ExoPlayer error: ", error)
                Toast.makeText(context, "Impossible de lire l'audio.", Toast.LENGTH_LONG).show()
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    LaunchedEffect(isPlaying, isReady) {
        while(isPlaying && isReady) {
            currentPosition = exoPlayer.currentPosition
            delay(1000)
        }
    }

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
                    value = currentPosition.toFloat(),
                    onValueChange = { if(isReady) exoPlayer.seekTo(it.toLong()) },
                    valueRange = 0f..(duration.toFloat().takeIf { it > 0 } ?: 0f),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isReady
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = formatTime(currentPosition), style = MaterialTheme.typography.bodySmall)
                    Text(text = formatTime(duration), style = MaterialTheme.typography.bodySmall)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { if(isReady) exoPlayer.seekTo(exoPlayer.currentPosition - 5000) }, enabled = isReady) {
                        Icon(Icons.Default.FastRewind, contentDescription = "Rewind")
                    }
                    IconButton(onClick = { if(isReady) { if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play() } }, enabled = isReady) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    IconButton(onClick = { if(isReady) exoPlayer.seekTo(exoPlayer.currentPosition + 5000) }, enabled = isReady) {
                        Icon(Icons.Default.FastForward, contentDescription = "Fast Forward")
                    }
                }
            }
            IconButton(
                onClick = {
                    if (isReady) {
                        exoPlayer.seekTo(0)
                        currentPosition = 0L
                        if (!exoPlayer.isPlaying) {
                            exoPlayer.play()
                        }
                    }
                },
                enabled = isReady
            ) {
                Icon(Icons.Default.Replay, contentDescription = "Rejouer l'audio")
            }
        }
    }
}

private fun formatTime(timeInMillis: Long): String {
    val totalSeconds = timeInMillis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}