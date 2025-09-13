package com.pilotprep.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pilotprep.app.data.local.QuestionEntity
import com.pilotprep.app.ui.theme.CorrectGreen
import com.pilotprep.app.ui.theme.IncorrectRed
import com.pilotprep.app.ui.viewmodels.QuizViewModel
import kotlinx.coroutines.delay
import androidx.compose.material3.Text
import com.pilotprep.app.ui.components.MarshallerLoadingAnimation

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

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        if (mode == "exam") {
            Text(
                "Mode: Simulation d'Examen",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
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
                    onHome = { navController.popBackStack("home", inclusive = false) }
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
                    isAnswerRevealed = uiState.selectedOptionIndex != null
                )

                // This LaunchedEffect will trigger when an answer is selected and revealed
                if (uiState.selectedOptionIndex != null) {
                    LaunchedEffect(uiState.currentQuestionIndex) {
                        delay(3500) // Wait before moving to the next question
                        viewModel.nextQuestion()
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionContent(
    question: QuestionEntity,
    questionNumber: Int,
    totalQuestions: Int,
    selectedOption: Int?,
    onOptionSelected: (Int) -> Unit,
    isAnswerRevealed: Boolean
) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
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
        Spacer(modifier = Modifier.height(24.dp))
        question.options.forEachIndexed { index, text ->
            OptionItem(
                text = text,
                isSelected = index == selectedOption,
                isCorrect = index == question.correctOptionIndex,
                isRevealed = isAnswerRevealed,
                onClick = { if (!isAnswerRevealed) onOptionSelected(index) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (isAnswerRevealed) {
            val isCorrect = selectedOption == question.correctOptionIndex
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = if (isCorrect) "Correct!" else "Incorrect",
                color = if (isCorrect) CorrectGreen else IncorrectRed,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Explanation: ${question.explanation}", style = MaterialTheme.typography.bodyLarge)
        }
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
            isCorrect -> CorrectGreen.copy(alpha = 0.3f) // Highlight correct answer if wrong one was chosen
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
        Text("Quiz Finished!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Your Score: $score / $totalQuestions", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onRestart) {
            Text("Try Again")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onHome) {
            Text("Back to Home")
        }
    }
}