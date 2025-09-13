package com.pilotprep.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pilotprep.app.data.QuestionRepository
import com.pilotprep.app.data.local.QuestionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuizUiState(
    val isLoading: Boolean = true,
    val questions: List<QuestionEntity> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val score: Int = 0,
    val isQuizFinished: Boolean = false
)

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repository: QuestionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    fun startQuiz(license: String, subject: String, questionCount: Int = 5) {
        viewModelScope.launch {
            _uiState.value = QuizUiState(isLoading = true) // Reset and show loading
            val questions = repository.getQuizQuestions(license, subject, questionCount)
            _uiState.value = QuizUiState(isLoading = false, questions = questions)
        }
    }

    fun selectOption(index: Int) {
        if (_uiState.value.selectedOptionIndex != null) return // Already answered

        val isCorrect = index == _uiState.value.questions[_uiState.value.currentQuestionIndex].correctOptionIndex
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
                    selectedOptionIndex = null
                )
            }
        } else {
            _uiState.update { it.copy(isQuizFinished = true) }
        }
    }
}