package com.pilotprep.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.pilotprep.app.R
import com.pilotprep.app.data.model.Question
import com.pilotprep.app.ui.components.CongratulationsAnimation
import com.pilotprep.app.ui.components.MarshallerLoadingAnimation
import com.pilotprep.app.viewmodels.ExamViewModel

@Composable
fun ExamScreen(
    navController: NavController,
    license: String,
    subject: String,
    viewModel: ExamViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startExam(license, subject)
    }

    Scaffold(
        topBar = {
            if (!uiState.isFinished) {
                ExamTopBar(
                    timeLeft = uiState.timeLeft,
                    onFinishExam = { viewModel.finishExam() }
                )
            }
        },
        bottomBar = {
            if (!uiState.isFinished && uiState.questions.isNotEmpty()) {
                ExamBottomBar(
                    currentQuestionIndex = uiState.currentQuestionIndex,
                    totalQuestions = uiState.questions.size,
                    onPrevious = { viewModel.previousQuestion() },
                    onNext = { viewModel.nextQuestion() }
                )
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    MarshallerLoadingAnimation()
                }
            }
            uiState.isFinished -> {
                ExamResultScreen(
                    score = uiState.score,
                    totalQuestions = uiState.questions.size,
                    incorrectQuestions = uiState.incorrectQuestions,
                    // --- LA CORRECTION DÉFINITIVE EST ICI ---
                    onBackToHome = {
                        // Cette commande signifie "Retourne à l'écran 'home' dans l'historique".
                        // C'est la méthode stable et correcte.
                        navController.popBackStack("home", inclusive = false)
                    }
                    // --- FIN DE LA CORRECTION ---
                )
            }
            uiState.questions.isNotEmpty() -> {
                val currentQuestion = uiState.questions[uiState.currentQuestionIndex]
                QuestionContent(
                    question = currentQuestion,
                    questionNumber = uiState.currentQuestionIndex + 1,
                    selectedAnswer = uiState.userAnswers[currentQuestion.id],
                    onAnswerSelected = { answerIndex ->
                        viewModel.selectAnswer(currentQuestion.id, answerIndex)
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamTopBar(timeLeft: Int, onFinishExam: () -> Unit) {
    TopAppBar(
        title = { Text("Simulation d'Examen") },
        actions = {
            Text(
                text = "Temps: %02d:%02d".format(timeLeft / 60, timeLeft % 60),
                modifier = Modifier.padding(end = 8.dp)
            )
            Button(onClick = onFinishExam) {
                Text("Terminer")
            }
        }
    )
}

@Composable
fun ExamBottomBar(
    currentQuestionIndex: Int,
    totalQuestions: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    BottomAppBar {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onPrevious, enabled = currentQuestionIndex > 0) {
                Text("Précédent")
            }
            Text("${currentQuestionIndex + 1} / $totalQuestions")
            Button(onClick = onNext, enabled = currentQuestionIndex < totalQuestions - 1) {
                Text("Suivant")
            }
        }
    }
}

@Composable
fun QuestionContent(
    modifier: Modifier = Modifier,
    question: Question,
    questionNumber: Int,
    selectedAnswer: Int?,
    onAnswerSelected: (Int) -> Unit
) {
    var showZoomableImage by remember { mutableStateOf(false) }

    LazyColumn(modifier = modifier.padding(16.dp)) {
        item {
            Text("Question $questionNumber", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(question.questionText, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)

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
                        .clickable { showZoomableImage = true },
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(Modifier.height(16.dp))
        }

        itemsIndexed(question.options) { index, optionText ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (selectedAnswer == index),
                        onClick = { onAnswerSelected(index) }
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (selectedAnswer == index),
                    onClick = { onAnswerSelected(index) }
                )
                Spacer(Modifier.width(8.dp))
                Text(optionText)
            }
        }
    }

    if (showZoomableImage && question.imageUrl != null) {
        ZoomableImageDialog(
            imageUrl = question.imageUrl!!,
            onDismiss = { showZoomableImage = false }
        )
    }
}

@Composable
fun ExamResultScreen(
    score: Int,
    totalQuestions: Int,
    incorrectQuestions: List<Question>,
    onBackToHome: () -> Unit
) {
    val percentage = if (totalQuestions > 0) (score.toFloat() / totalQuestions.toFloat() * 100).toInt() else 0
    val hasPassed = percentage >= 75

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasPassed) {
            CongratulationsAnimation()
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (hasPassed) "Félicitations !" else "Continuez vos efforts !",
                        style = MaterialTheme.typography.headlineLarge,
                        color = if (hasPassed) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Votre score : $score / $totalQuestions ($percentage%)",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(Modifier.height(32.dp))
                }
            }

            if (incorrectQuestions.isNotEmpty()) {
                item {
                    Text(
                        "Corrections",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                items(incorrectQuestions) { question ->
                    CorrectionItem(question = question)
                    Spacer(Modifier.height(16.dp))
                }
            }

            item {
                Button(
                    onClick = onBackToHome,
                    modifier = Modifier.padding(top = 32.dp)
                ) {
                    Text("Retour à l'accueil")
                }
            }
        }
    }
}

@Composable
fun CorrectionItem(question: Question) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = question.questionText,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )

            if (question.imageUrl != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = rememberAsyncImagePainter(model = question.imageUrl),
                    contentDescription = "Image de la question corrigée",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Réponse correcte : ${question.options[question.correctOptionIndex.toInt()]}",
                color = Color(0xFF4CAF50)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Explication : ${question.explanation}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}