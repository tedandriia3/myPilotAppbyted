package com.pilotprep.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.airbnb.lottie.compose.*
import com.pilotprep.app.R
import com.pilotprep.app.data.model.Question
import com.pilotprep.app.ui.components.MarshallerLoadingAnimation
import com.pilotprep.app.ui.theme.CorrectGreen
import com.pilotprep.app.ui.theme.IncorrectRed
import com.pilotprep.app.viewmodels.QuizViewModel

@Composable
fun QuizScreen(
    navController: NavController,
    license: String,
    subject: String,
    mode: String,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startQuiz(license, subject)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    MarshallerLoadingAnimation()
                }
            }
            uiState.isQuizFinished -> {
                QuizResultScreen(
                    score = uiState.score,
                    totalQuestions = uiState.questions.size,
                    onRestart = { viewModel.startQuiz(license, subject) },
                    onHome = { navController.popBackStack("home", false) }
                )
            }
            uiState.questions.isNotEmpty() -> {
                val currentQuestion = uiState.questions[uiState.currentQuestionIndex]
                QuestionContent(
                    question = currentQuestion,
                    questionNumber = uiState.currentQuestionIndex + 1,
                    totalQuestions = uiState.questions.size,
                    selectedOption = uiState.selectedOptionIndex,
                    onOptionSelected = { index -> viewModel.selectOption(index) },
                )
            }
        }

        AnimatedVisibility(
            visible = uiState.selectedOptionIndex != null && !uiState.isQuizFinished,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            NextQuestionButton(onClick = { viewModel.nextQuestion() })
        }
    }
}

@Composable
fun QuestionContent(
    question: Question,
    questionNumber: Int,
    totalQuestions: Int,
    selectedOption: Int?,
    onOptionSelected: (Int) -> Unit
) {
    val isAnswerRevealed = selectedOption != null
    var showZoomableQuestionImage by remember { mutableStateOf(false) }
    var showZoomableExplanationImage by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Question $questionNumber / $totalQuestions",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = question.questionText,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 30.sp
        )

        if (question.imageUrl != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Image(
                painter = rememberAsyncImagePainter(
                    model = question.imageUrl,
                    placeholder = painterResource(id = R.drawable.ic_heritage)
                ),
                contentDescription = "Image de la question",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showZoomableQuestionImage = true },
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        question.options.forEachIndexed { index, text ->
            OptionItem(
                text = text,
                isSelected = index == selectedOption,
                isCorrect = index == question.correctOptionIndex.toInt(),
                isRevealed = isAnswerRevealed,
                onClick = { if (!isAnswerRevealed) onOptionSelected(index) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (isAnswerRevealed) {
            val isCorrect = selectedOption == question.correctOptionIndex.toInt()
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = if (isCorrect) "Correct!" else "Incorrect",
                color = if (isCorrect) CorrectGreen else IncorrectRed,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Explication : ${question.explanation}", style = MaterialTheme.typography.bodyLarge)

            if (question.explanationImageUrl != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Image(
                    painter = rememberAsyncImagePainter(
                        model = question.explanationImageUrl,
                        placeholder = painterResource(id = R.drawable.ic_heritage)
                    ),
                    contentDescription = "Image de l'explication",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showZoomableExplanationImage = true },
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showZoomableQuestionImage && question.imageUrl != null) {
        ZoomableImageDialog(
            imageUrl = question.imageUrl!!,
            onDismiss = { showZoomableQuestionImage = false }
        )
    }

    if (showZoomableExplanationImage && question.explanationImageUrl != null) {
        ZoomableImageDialog(
            imageUrl = question.explanationImageUrl!!,
            onDismiss = { showZoomableExplanationImage = false }
        )
    }
}

@Composable
fun ZoomableImageDialog(imageUrl: String, onDismiss: () -> Unit) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
        offset += offsetChange
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onDismiss)
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = imageUrl),
                contentDescription = "Image agrandie",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
                    .transformable(state = state)
            )
        }
    }
}

@Composable
fun NextQuestionButton(onClick: () -> Unit) {
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(R.raw.lottie_arrow_next)
    )

    var isPlaying by remember { mutableStateOf(false) }
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = isPlaying
    )

    LaunchedEffect(progress) {
        if (progress == 1f) {
            isPlaying = false
        }
    }

    FloatingActionButton(
        onClick = {
            isPlaying = true
            onClick()
        },
        shape = CircleShape
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(48.dp)
        )
    }
}

@Composable
fun OptionItem(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    isRevealed: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            !isRevealed -> MaterialTheme.colorScheme.surface
            isSelected && isCorrect -> CorrectGreen.copy(alpha = 0.3f)
            isSelected && !isCorrect -> IncorrectRed.copy(alpha = 0.3f)
            isCorrect -> CorrectGreen.copy(alpha = 0.3f)
            else -> MaterialTheme.colorScheme.surface
        }, label = "OptionBackgroundColor"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            !isRevealed -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            isSelected && isCorrect -> CorrectGreen
            isSelected && !isCorrect -> IncorrectRed
            isCorrect -> CorrectGreen
            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        }, label = "OptionBorderColor"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(text = text, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun QuizResultScreen(
    score: Int,
    totalQuestions: Int,
    onRestart: () -> Unit,
    onHome: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Quiz Terminé !", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Votre score: $score / $totalQuestions", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onRestart) {
            Text("Recommencer")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onHome) {
            Text("Retour à l'accueil")
        }
    }
}