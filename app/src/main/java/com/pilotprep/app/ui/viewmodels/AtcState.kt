package com.pilotprep.app.viewmodels

import com.pilotprep.app.data.model.AtcExercise

// On déplace les définitions ici pour les rendre publiques et accessibles partout

enum class VerificationState {
    UNVERIFIED,
    VALIDATED,
    SOLUTIONS_SHOWN
}

data class AtcUiState(
    val isLoading: Boolean = true,
    val exercises: List<AtcExercise> = emptyList(),
    val currentExercise: AtcExercise? = null,
    val userInputs: List<String> = emptyList(),
    val verificationState: VerificationState = VerificationState.UNVERIFIED,
    val correctionResults: List<Boolean> = emptyList(),
    val score: Int = 0
)