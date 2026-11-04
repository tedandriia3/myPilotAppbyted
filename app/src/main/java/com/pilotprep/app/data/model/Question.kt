package com.pilotprep.app.data.model

import androidx.annotation.Keep

@Keep
data class Question(
    val id: String = "",
    var license: String = "",
    var subject: String = "",
    var questionText: String = "",
    var options: List<String> = emptyList(),
    var correctOptionIndex: Long = 0,
    var explanation: String = "",
    var imageUrl: String? = null,
    var explanationImageUrl: String? = null // <-- CHAMP AJOUTÉ ICI
)