package com.pilotprep.app.data.model

import androidx.annotation.Keep

@Keep
data class AtcExercise(
    val id: String = "",
    var type: String = "",
    var title: String = "",
    var audioUrl: String = "",
    var transcriptTemplate: String = "",
    var solutions: List<String> = emptyList()
)