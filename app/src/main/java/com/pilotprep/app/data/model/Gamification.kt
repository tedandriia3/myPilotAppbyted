package com.pilotprep.app.data.model

import androidx.annotation.Keep
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Keep
enum class AchievementType(val category: String) {
    // Quiz et Performance
    QUIZ_MASTER("Quiz"),
    PERFECT_SCORE("Performance"),
    SPEED_DEMON("Performance"),
    CONSISTENCY("Performance"),
    
    // Étude et Temps
    STUDY_MARATHON("Étude"),
    EARLY_BIRD("Étude"),
    NIGHT_OWL("Étude"),
    WEEKEND_WARRIOR("Étude"),
    
    // Astuces et Contenu
    TIP_COLLECTOR("Contenu"),
    WEEKLY_READER("Contenu"),
    KNOWLEDGE_SEEKER("Contenu"),
    
    // Engagement
    STREAK_MASTER("Engagement"),
    DAILY_LEARNER("Engagement"),
    MONTHLY_CHAMPION("Engagement"),
    
    // Spécial
    FIRST_STEPS("Spécial"),
    MILESTONE("Spécial"),
    LEGEND("Spécial")
}

@Keep
data class Badge(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val icon: String = "",
    val rarity: BadgeRarity = BadgeRarity.COMMON,
    val category: String = "",
    val points: Int = 0,
    val requirements: List<BadgeRequirement> = emptyList()
)

@Keep
data class BadgeRequirement(
    val type: RequirementType,
    val value: Int,
    val description: String = ""
)

@Keep
enum class RequirementType {
    QUIZ_COMPLETED,
    PERFECT_SCORE,
    STUDY_TIME_MINUTES,
    STREAK_DAYS,
    WEEKLY_TIPS_READ,
    LEVEL_REACHED,
    POINTS_EARNED
}

@Keep
enum class BadgeRarity(val color: String, val multiplier: Float) {
    COMMON("#9E9E9E", 1.0f),
    UNCOMMON("#4CAF50", 1.2f),
    RARE("#2196F3", 1.5f),
    EPIC("#9C27B0", 2.0f),
    LEGENDARY("#FF9800", 3.0f)
}

@Keep
data class Level(
    val level: Int,
    val name: String,
    val requiredXP: Int,
    val rewards: List<LevelReward> = emptyList()
)

@Keep
data class LevelReward(
    val type: RewardType,
    val value: Int,
    val description: String = ""
)

@Keep
enum class RewardType {
    POINTS,
    BADGE,
    TITLE,
    UNLOCK_FEATURE
}

/**
 * Utilitaires pour le système de gamification
 */
object GamificationUtils {
    
    /**
     * Calcule le niveau basé sur les points d'expérience
     */
    fun calculateLevel(experiencePoints: Int): Int {
        return when {
            experiencePoints < 100 -> 1
            experiencePoints < 300 -> 2
            experiencePoints < 600 -> 3
            experiencePoints < 1000 -> 4
            experiencePoints < 1500 -> 5
            experiencePoints < 2100 -> 6
            experiencePoints < 2800 -> 7
            experiencePoints < 3600 -> 8
            experiencePoints < 4500 -> 9
            experiencePoints < 5500 -> 10
            else -> 10 + ((experiencePoints - 5500) / 1000)
        }
    }
    
    /**
     * Calcule les points d'expérience nécessaires pour le niveau suivant
     */
    fun getXPForNextLevel(currentLevel: Int): Int {
        return when (currentLevel) {
            1 -> 100
            2 -> 300
            3 -> 600
            4 -> 1000
            5 -> 1500
            6 -> 2100
            7 -> 2800
            8 -> 3600
            9 -> 4500
            10 -> 5500
            else -> 5500 + ((currentLevel - 10) * 1000)
        }
    }
    
    /**
     * Calcule les points d'expérience pour un quiz
     */
    fun calculateQuizXP(score: Int, totalQuestions: Int, timeSpentSeconds: Int): Int {
        val accuracy = score.toFloat() / totalQuestions
        val basePoints = (accuracy * 50).toInt()
        
        // Bonus pour la rapidité (max 20 points)
        val timeBonus = when {
            timeSpentSeconds < 60 -> 20
            timeSpentSeconds < 120 -> 15
            timeSpentSeconds < 180 -> 10
            timeSpentSeconds < 300 -> 5
            else -> 0
        }
        
        // Bonus pour score parfait
        val perfectBonus = if (accuracy == 1.0f) 30 else 0
        
        return basePoints + timeBonus + perfectBonus
    }
    
    /**
     * Calcule les points d'expérience pour le temps d'étude
     */
    fun calculateStudyXP(minutesStudied: Int): Int {
        return (minutesStudied * 0.5).toInt() // 0.5 XP par minute
    }
    
    /**
     * Calcule les points d'expérience pour lire une astuce
     */
    fun calculateTipXP(): Int {
        return 10 // 10 XP par astuce lue
    }
    
    /**
     * Génère les badges par défaut
     */
    fun getDefaultBadges(): List<Badge> {
        return listOf(
            Badge(
                id = "first_quiz",
                name = "Premier Pas",
                description = "Complétez votre premier quiz",
                icon = "🎯",
                rarity = BadgeRarity.COMMON,
                category = "Débutant",
                points = 50,
                requirements = listOf(
                    BadgeRequirement(RequirementType.QUIZ_COMPLETED, 1, "Compléter 1 quiz")
                )
            ),
            Badge(
                id = "perfect_score",
                name = "Perfectionniste",
                description = "Obtenez un score parfait à un quiz",
                icon = "💯",
                rarity = BadgeRarity.UNCOMMON,
                category = "Performance",
                points = 100,
                requirements = listOf(
                    BadgeRequirement(RequirementType.PERFECT_SCORE, 1, "Score parfait")
                )
            ),
            Badge(
                id = "study_marathon",
                name = "Marathon d'Étude",
                description = "Étudiez pendant 2 heures d'affilée",
                icon = "📚",
                rarity = BadgeRarity.RARE,
                category = "Étude",
                points = 200,
                requirements = listOf(
                    BadgeRequirement(RequirementType.STUDY_TIME_MINUTES, 120, "120 minutes d'étude")
                )
            ),
            Badge(
                id = "streak_master",
                name = "Maître de la Régularité",
                description = "Maintenez une série de 7 jours",
                icon = "🔥",
                rarity = BadgeRarity.EPIC,
                category = "Engagement",
                points = 300,
                requirements = listOf(
                    BadgeRequirement(RequirementType.STREAK_DAYS, 7, "7 jours consécutifs")
                )
            ),
            Badge(
                id = "quiz_master",
                name = "Maître des Quiz",
                description = "Complétez 50 quiz",
                icon = "🏆",
                rarity = BadgeRarity.LEGENDARY,
                category = "Expert",
                points = 500,
                requirements = listOf(
                    BadgeRequirement(RequirementType.QUIZ_COMPLETED, 50, "50 quiz complétés")
                )
            )
        )
    }
    
    /**
     * Génère les niveaux par défaut
     */
    fun getDefaultLevels(): List<Level> {
        return listOf(
            Level(1, "Apprenti Pilote", 0),
            Level(2, "Élève Pilote", 100),
            Level(3, "Pilote Junior", 300),
            Level(4, "Pilote Confirmé", 600),
            Level(5, "Pilote Expérimenté", 1000),
            Level(6, "Instructeur", 1500),
            Level(7, "Capitaine", 2100),
            Level(8, "Commandant", 2800),
            Level(9, "Expert", 3600),
            Level(10, "Maître Pilote", 4500),
            Level(11, "Légende", 5500)
        )
    }
    
    /**
     * Formate les points d'expérience
     */
    fun formatXP(xp: Int): String {
        return when {
            xp >= 1000000 -> "${xp / 1000000}M"
            xp >= 1000 -> "${xp / 1000}K"
            else -> xp.toString()
        }
    }
    
    /**
     * Calcule le pourcentage de progression vers le niveau suivant
     */
    fun getLevelProgress(currentXP: Int, currentLevel: Int): Float {
        val currentLevelXP = getXPForLevel(currentLevel)
        val nextLevelXP = getXPForNextLevel(currentLevel)
        val progressXP = currentXP - currentLevelXP
        
        return progressXP.toFloat() / (nextLevelXP - currentLevelXP)
    }
    
    private fun getXPForLevel(level: Int): Int {
        return when (level) {
            1 -> 0
            2 -> 100
            3 -> 300
            4 -> 600
            5 -> 1000
            6 -> 1500
            7 -> 2100
            8 -> 2800
            9 -> 3600
            10 -> 4500
            else -> 4500 + ((level - 10) * 1000)
        }
    }
}


