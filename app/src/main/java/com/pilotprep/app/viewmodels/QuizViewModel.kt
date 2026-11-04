package com.pilotprep.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pilotprep.app.data.model.*
import com.pilotprep.app.data.repository.QuizRepository
import com.pilotprep.app.data.repository.UserStatsRepository
import com.pilotprep.app.services.UserStatsService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class QuizUiState(
    val isLoading: Boolean = true,
    val questions: List<Question> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val score: Int = 0,
    val isQuizFinished: Boolean = false,
    val sessionStartTime: Date? = null,
    val sessionId: String? = null,
    val error: String? = null
)

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repository: QuizRepository,
    private val userStatsRepository: UserStatsRepository,
    private val userStatsService: UserStatsService // Injecter le service partagé
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var currentLicense: String = ""
    private var currentSubject: String = ""
    private var currentUserId: String = ""

    fun startQuiz(license: String, subject: String, userId: String = "default_user") {
        viewModelScope.launch {
            _uiState.value = QuizUiState(isLoading = true)
            
            val result = repository.getQuestionsForPractice(license, subject, 10)
            
            result.fold(
                onSuccess = { questions ->
                    val sessionId = UUID.randomUUID().toString()
                    
                    currentLicense = license
                    currentSubject = subject
                    currentUserId = userId
                    
                    _uiState.value = QuizUiState(
                        isLoading = false, 
                        questions = questions,
                        sessionStartTime = Date(),
                        sessionId = sessionId
                    )
                },
                onFailure = { error ->
                    _uiState.value = QuizUiState(
                        isLoading = false,
                        questions = emptyList(),
                        error = error.message ?: "Erreur lors du chargement des questions"
                    )
                }
            )
        }
    }

    fun selectOption(index: Int) {
        if (_uiState.value.selectedOptionIndex != null) return

        val isCorrect = index == _uiState.value.questions[_uiState.value.currentQuestionIndex].correctOptionIndex.toInt()
        _uiState.update {
            it.copy(
                selectedOptionIndex = index,
                score = if (isCorrect) it.score + 1 else it.score
            )
        }
    }

    fun nextQuestion() {
        val nextIndex = _uiState.value.currentQuestionIndex + 1
        if (nextIndex < _uiState.value.questions.size) {
            _uiState.update {
                it.copy(
                    currentQuestionIndex = nextIndex,
                    selectedOptionIndex = null // Réinitialise pour la nouvelle question
                )
            }
        } else {
            finishQuiz()
        }
    }

    private fun finishQuiz() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val sessionStartTime = currentState.sessionStartTime ?: Date()
            val sessionEndTime = Date()
            val durationMinutes = (sessionEndTime.time - sessionStartTime.time) / (1000 * 60)
            
            val finalScore = if (currentState.questions.isNotEmpty()) {
                (currentState.score.toFloat() / currentState.questions.size * 100)
            } else 0f

            // Enregistrer la session d'étude via UserStatsService
            userStatsService.recordStudySession(
                userId = currentUserId,
                subject = currentSubject,
                license = currentLicense,
                sessionType = SessionType.QUIZ,
                durationMinutes = durationMinutes,
                questionsAnswered = currentState.questions.size,
                correctAnswers = currentState.score
            )

            _uiState.update { 
                it.copy(isQuizFinished = true) 
            }
        }
    }

    fun resetQuiz() {
        _uiState.value = QuizUiState()
        currentLicense = ""
        currentSubject = ""
        currentUserId = ""
    }
}
