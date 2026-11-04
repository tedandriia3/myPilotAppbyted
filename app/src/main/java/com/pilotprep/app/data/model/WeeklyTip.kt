package com.pilotprep.app.data.model

import androidx.annotation.Keep
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Keep
data class WeeklyTip(
    val id: String = "",
    val weekOfYear: Int = 1, // 1-52
    val title: String = "",
    val introduction: String = "",
    val fullContent: String = "",
    val category: String = "",
    val imageUrl: String = "", // Nouvelle propriété pour l'image
    val h2ColorHex: String = "", // Couleur optionnelle pour les titres H2 dans le markdown
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

@Keep
data class WeeklyTipConfig(
    val currentWeekNumber: Int = 1,
    val lastUpdateTimestamp: Long = System.currentTimeMillis(),
    val rotationStrategy: String = "weekly" // "weekly", "manual"
)

@Keep
data class WeeklyTipInfo(
    val tipId: String,
    val weekOfYear: Int,
    val title: String,
    val introduction: String,
    val category: String,
    val imageUrl: String = ""
)

// Extension pour WeeklyTip pour faciliter l'affichage
fun WeeklyTip.toWeeklyTipInfo(): WeeklyTipInfo {
    return WeeklyTipInfo(
        tipId = this.id,
        weekOfYear = this.weekOfYear,
        title = this.title,
        introduction = this.introduction,
        category = this.category,
        imageUrl = this.imageUrl
    )
}

// Utilitaires pour la gestion des semaines
object WeekUtils {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    
    fun getCurrentWeekOfYear(): Int {
        val now = LocalDate.now()
        return getWeekOfYearForDate(now)
    }
    
    fun getWeekOfYearForDate(date: LocalDate): Int {
        // Utiliser l'API Java standard pour le calcul ISO de la semaine
        return date.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear())
    }
    
    fun formatDate(date: LocalDate): String {
        return date.format(dateFormatter)
    }
    
    fun isValidWeekNumber(weekNumber: Int): Boolean {
        return weekNumber in 1..53 // ISO peut avoir 53 semaines certaines années
    }
    
    // Méthode de debug pour vérifier le calcul
    fun debugWeekCalculation(): String {
        val now = LocalDate.now()
        val weekNumber = getWeekOfYearForDate(now)
        return "Date: $now, Semaine ISO: $weekNumber, Jour de l'année: ${now.dayOfYear}"
    }
}

