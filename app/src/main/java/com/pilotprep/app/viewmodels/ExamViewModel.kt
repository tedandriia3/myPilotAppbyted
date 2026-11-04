package com.pilotprep.app.viewmodels

import androidx.lifecycle.ViewModel // <-- L'IMPORT CRUCIAL QUI MANQUAIT
import androidx.lifecycle.viewModelScope
import com.pilotprep.app.data.model.Question
import com.pilotprep.app.data.repository.QuizRepository
import com.pilotprep.app.util.SharedData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExamUiState(
    val isLoading: Boolean = true,
    val questions: List<Question> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val userAnswers: Map<String, Int> = emptyMap(), // Map<questionId, selectedOptionIndex>
    val timeLeft: Int = 600, // 10 minutes par défaut
    val isFinished: Boolean = false,
    val score: Int = 0,
    val incorrectQuestions: List<Question> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ExamViewModel @Inject constructor(
    private val repository: QuizRepository
) : ViewModel() { // <-- Il étend bien ViewModel, donc l'import est nécessaire

    private val _uiState = MutableStateFlow(ExamUiState())
    val uiState = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun startExam(license: String, subject: String) {
        viewModelScope.launch {
            _uiState.value = ExamUiState(isLoading = true)
            
            // Utiliser la configuration d'examen spécifique au sujet
            val examConfig = SharedData.getExamConfig(subject)
            val questionCount = examConfig?.questionCount ?: 20 // Fallback à 20 si pas de config
            val durationMinutes = examConfig?.durationMinutes ?: 20 // Fallback à 20min si pas de config
            
            val result = repository.getQuestionsForExam(license, subject, questionCount)
            
            result.fold(
                onSuccess = { questions ->
                    _uiState.value = ExamUiState(
                        isLoading = false, 
                        questions = questions, 
                        timeLeft = durationMinutes * 60 // Convertir minutes en secondes
                    )
                    startTimer()
                },
                onFailure = { error ->
                    _uiState.value = ExamUiState(
                        isLoading = false, 
                        questions = emptyList(), 
                        error = error.message ?: "Erreur lors du chargement des questions"
                    )
                }
            )
        }
    }

    fun selectAnswer(questionId: String, answerIndex: Int) {
        val answers = _uiState.value.userAnswers.toMutableMap()
        answers[questionId] = answerIndex
        _uiState.update { it.copy(userAnswers = answers) }
    }

    fun nextQuestion() {
        if (_uiState.value.currentQuestionIndex < _uiState.value.questions.size - 1) {
            _uiState.update { it.copy(currentQuestionIndex = it.currentQuestionIndex + 1) }
        }
    }

    fun previousQuestion() {
        if (_uiState.value.currentQuestionIndex > 0) {
            _uiState.update { it.copy(currentQuestionIndex = it.currentQuestionIndex - 1) }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeLeft > 0 && !_uiState.value.isFinished) {
                delay(1000)
                _uiState.update { it.copy(timeLeft = it.timeLeft - 1) }
            }
            if (!_uiState.value.isFinished) {
                finishExam() // Le temps est écoulé
            }
        }
    }

    fun finishExam() {
        timerJob?.cancel()
        var correctAnswers = 0
        val incorrectQuestionsList = mutableListOf<Question>()

        _uiState.value.questions.forEach { question ->
            val userAnswer = _uiState.value.userAnswers[question.id]
            val correctAnswerIndex = question.correctOptionIndex.toInt()

            if (userAnswer == correctAnswerIndex) {
                correctAnswers++
            } else {
                incorrectQuestionsList.add(question)
            }
        }

        _uiState.update { it.copy(
            isFinished = true,
            score = correctAnswers,
            incorrectQuestions = incorrectQuestionsList
        )}
    }
}