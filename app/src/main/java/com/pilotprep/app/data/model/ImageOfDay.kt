package com.pilotprep.app.data.model

import androidx.annotation.Keep
import java.time.LocalDate

@Keep
data class ImageOfDay(
    val id: String = "",
    val aircraftId: String = "",
    val displayDate: String = "", // Format: "2024-01-15"
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

@Keep
data class DailyImageConfig(
    val currentImageId: String = "",
    val lastUpdated: String = "", // Format: "2024-01-15"
    val rotationStrategy: String = "random" // "random", "sequential", "manual"
)

// Extension pour Aircraft pour faciliter l'affichage de l'image du jour
fun Aircraft.toImageOfDayInfo(): ImageOfDayInfo {
    return ImageOfDayInfo(
        aircraftId = this.id,
        name = this.name,
        imageUrl = this.imageUrl,
        description = this.description,
        year = this.year
    )
}

@Keep
data class ImageOfDayInfo(
    val aircraftId: String,
    val name: String,
    val imageUrl: String,
    val description: String,
    val year: String
)

