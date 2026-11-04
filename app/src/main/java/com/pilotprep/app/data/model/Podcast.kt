package com.pilotprep.app.data.model

import androidx.annotation.Keep

@Keep
data class Podcast(
    val id: String = "",
    var title: String = "",
    var description: String = "",
    var audioUrl: String = "",
    var duration: String = "",
    var orderIndex: Long = 0
)