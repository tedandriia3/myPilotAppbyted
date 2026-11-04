package com.pilotprep.app.util

data class ExamConfig(
    val subject: String,
    val questionCount: Int,
    val durationMinutes: Int
)

object SharedData {
    // Liste pour "Mise en Pratique" CPL & ATPL, et "Simulation d'Examen"
    val cplAtplPracticeSubjects = listOf(
        "DROIT AÉRIEN ET PROCÉDURES ATC",
        "CELLULE ET SYSTÈMES",
        "INSTRUMENTATION",
        "MASSES ET CENTRAGE",
        "PERFORMANCES AVIONS",
        "PRÉPARATION ET SUIVI DU VOL",
        "PERFORMANCE HUMAINE",
        "MÉTÉOROLOGIE",
        "NAVIGATION",
        "RADIONAVIGATION",
        "PROCÉDURES OPÉRATIONNELLES",
        "PRINCIPES DU VOL",
        "COMMUNICATION"
    )

    // Configuration des examens CPL et ATPL
    val cplAtplExamConfigs = mapOf(
        "DROIT AÉRIEN ET PROCÉDURES ATC" to ExamConfig("DROIT AÉRIEN ET PROCÉDURES ATC", 42, 75),
        "CELLULE ET SYSTÈMES" to ExamConfig("CELLULE ET SYSTÈMES", 28, 45),
        "INSTRUMENTATION" to ExamConfig("INSTRUMENTATION", 32, 50),
        "MASSES ET CENTRAGE" to ExamConfig("MASSES ET CENTRAGE", 16, 30),
        "PERFORMANCES AVIONS" to ExamConfig("PERFORMANCES AVIONS", 20, 35),
        "PRÉPARATION ET SUIVI DU VOL" to ExamConfig("PRÉPARATION ET SUIVI DU VOL", 20, 35),
        "PERFORMANCE HUMAINE" to ExamConfig("PERFORMANCE HUMAINE", 16, 30),
        "MÉTÉOROLOGIE" to ExamConfig("MÉTÉOROLOGIE", 30, 50),
        "NAVIGATION" to ExamConfig("NAVIGATION", 42, 75),
        "RADIONAVIGATION" to ExamConfig("RADIONAVIGATION", 20, 35),
        "PROCÉDURES OPÉRATIONNELLES" to ExamConfig("PROCÉDURES OPÉRATIONNELLES", 20, 35),
        "PRINCIPES DU VOL" to ExamConfig("PRINCIPES DU VOL", 20, 35),
        "COMMUNICATION" to ExamConfig("COMMUNICATION", 28, 45)
    )

    // Configuration des examens PPL
    val pplExamConfigs = mapOf(
        "DROIT AÉRIEN" to ExamConfig("DROIT AÉRIEN", 16, 30),
        "CELLULE ET SYSTÈMES" to ExamConfig("CELLULE ET SYSTÈMES", 16, 30),
        "PERFORMANCES AVIONS" to ExamConfig("PERFORMANCES AVIONS", 16, 30),
        "PRÉPARATION ET SUIVI DU VOL" to ExamConfig("PRÉPARATION ET SUIVI DU VOL", 16, 30),
        "PERFORMANCE HUMAINE" to ExamConfig("PERFORMANCE HUMAINE", 16, 30),
        "MÉTÉOROLOGIE" to ExamConfig("MÉTÉOROLOGIE", 16, 30),
        "NAVIGATION (GENERAL + RADIONAVIGATION)" to ExamConfig("NAVIGATION (GENERAL + RADIONAVIGATION)", 16, 30),
        "PROCÉDURES OPÉRATIONNELLES" to ExamConfig("PROCÉDURES OPÉRATIONNELLES", 16, 30),
        "PRINCIPES DU VOL" to ExamConfig("PRINCIPES DU VOL", 16, 30),
        "COMMUNICATION (VFR)" to ExamConfig("COMMUNICATION (VFR)", 16, 30)
    )

    // Liste spécifique pour "Mise en Pratique" PPL
    val pplPracticeSubjects = listOf(
        "DROIT AÉRIEN",
        "CELLULE ET SYSTÈMES",
        "PERFORMANCES AVIONS",
        "PRÉPARATION ET SUIVI DU VOL",
        "PERFORMANCE HUMAINE",
        "MÉTÉOROLOGIE",
        "NAVIGATION (GENERAL + RADIONAVIGATION)",
        "PROCÉDURES OPÉRATIONNELLES",
        "PRINCIPES DU VOL",
        "COMMUNICATION (VFR)"
    )

    // --- LISTES POUR LA SECTION "THÉORIE DU VOL" ---
    val summarySubjects = listOf(
        "DROIT AÉRIEN ET PROCÉDURES ATC",
        "CELLULE ET SYSTÈMES",
        "INSTRUMENTATION",
        "MASSES ET CENTRAGE",
        "PERFORMANCES AVIONS",
        "PRÉPARATION ET SUIVI DU VOL",
        "PERFORMANCE HUMAINE",
        "MÉTÉOROLOGIE",
        "NAVIGATION",
        "RADIONAVIGATION",
        "PROCÉDURES OPÉRATIONNELLES",
        "PRINCIPES DU VOL",
        "COMMUNICATION"
    )

    // Fonction utilitaire pour obtenir la configuration d'examen
    fun getExamConfig(subject: String): ExamConfig? {
        return cplAtplExamConfigs[subject]
    }

    fun getExamConfigFor(license: String, subject: String): ExamConfig? {
        return when (license.uppercase()) {
            "PPL" -> pplExamConfigs[subject]
            "CPL", "ATPL" -> cplAtplExamConfigs[subject]
            else -> null
        }
    }

    // Fonction utilitaire pour formater la durée
    fun formatDuration(minutes: Int): String {
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        return if (hours > 0) {
            "${hours}h ${remainingMinutes}min"
        } else {
            "${remainingMinutes}min"
        }
    }
}