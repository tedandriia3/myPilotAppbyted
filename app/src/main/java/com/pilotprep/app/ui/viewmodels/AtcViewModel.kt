package com.pilotprep.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pilotprep.app.data.model.AtcExercise
import com.pilotprep.app.data.repository.AtcRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Les déclarations pour VerificationState et AtcUiState ont été supprimées d'ici
// Elles se trouvent maintenant dans AtcState.kt

@HiltViewModel
class AtcViewModel @Inject constructor(
    private val repository: AtcRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AtcUiState())
    val uiState = _uiState.asStateFlow()

    fun fetchExercises(type: String, forceRefresh: Boolean = false) {
        if (_uiState.value.exercises.isNotEmpty() && !forceRefresh) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, exercises = emptyList(), currentExercise = null) }
            val exercises = repository.getExercises(type)
            _uiState.update { it.copy(isLoading = false, exercises = exercises) }
        }
    }

    fun selectExercise(exercise: AtcExercise) {
        _uiState.update {
            it.copy(
                currentExercise = exercise,
                userInputs = List(exercise.solutions.size) { "" },
                verificationState = VerificationState.UNVERIFIED,
                correctionResults = emptyList(),
                score = 0
            )
        }
    }

    fun onUserInputChange(index: Int, text: String) {
        val newInputs = _uiState.value.userInputs.toMutableList()
        if (index < newInputs.size) {
            newInputs[index] = text
            _uiState.update { it.copy(userInputs = newInputs) }
        }
    }

    fun validateAnswers() {
        val exercise = _uiState.value.currentExercise ?: return
        val results = mutableListOf<Boolean>()
        var correctCount = 0

        exercise.solutions.forEachIndexed { index, solution ->
            val isCorrect = solution.equals(_uiState.value.userInputs.getOrElse(index) { "" }.trim(), ignoreCase = true)
            results.add(isCorrect)
            if (isCorrect) correctCount++
        }
        _uiState.update {
            it.copy(
                verificationState = VerificationState.VALIDATED,
                correctionResults = results,
                score = correctCount
            )
        }
    }

    fun showSolutions() {
        _uiState.update {
            it.copy(
                verificationState = VerificationState.SOLUTIONS_SHOWN
            )
        }
    }

    fun backToList() {
        _uiState.update { it.copy(currentExercise = null) }
    }
}