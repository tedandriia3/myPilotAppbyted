package com.pilotprep.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.pilotprep.app.data.model.Lesson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LessonDataSeeder @Inject constructor(
    private val db: FirebaseFirestore
) {

    fun addSampleLessons() {
        // DÉSACTIVÉ : Le LessonDataSeeder ne crée plus de données génériques
        // Le contenu unique est maintenant géré par LessonDataCleaner
        println("⚠️  LessonDataSeeder désactivé - Utilisez l'outil de nettoyage des données pour créer du contenu unique")
        
        // Ancien code commenté pour éviter la création de données dupliquées
        /*
        // Leçon 1: Droit Aérien
        val droitAerienLesson = Lesson(
            id = "",
            category = "summary",
            subject = "DROIT AÉRIEN ET PROCÉDURES ATC",
            title = "Règles de l'air et procédures ATC",
            content = listOf(
                mapOf("type" to "h2", "text" to "Introduction au droit aérien"),
                mapOf("type" to "p", "text" to "Le droit aérien régit l'ensemble des règles qui s'appliquent à la navigation aérienne. Ces règles sont établies par l'Organisation de l'Aviation Civile Internationale (OACI)."),
                mapOf("type" to "h2", "text" to "Les classes d'espace aérien"),
                mapOf("type" to "p", "text" to "L'espace aérien est divisé en plusieurs classes : A, B, C, D, E, F et G. Chaque classe a ses propres règles d'accès et de communication."),
                mapOf("type" to "h2", "text" to "Responsabilités du pilote"),
                mapOf("type" to "p", "text" to "Le pilote commandant de bord est responsable de la sécurité de l'aéronef et de ses occupants. Il doit respecter les règles de l'air et suivre les instructions du contrôle aérien.")
            )
        )

        // Leçon 2: Cellule et Systèmes
        val celluleLesson = Lesson(
            id = "",
            category = "summary",
            subject = "CELLULE ET SYSTÈMES",
            title = "Structure de l'avion et systèmes principaux",
            content = listOf(
                mapOf("type" to "h2", "text" to "Structure générale"),
                mapOf("type" to "p", "text" to "Un avion se compose principalement de la cellule (fuselage, ailes, empennage), du groupe motopropulseur et des systèmes auxiliaires."),
                mapOf("type" to "h2", "text" to "Systèmes hydrauliques"),
                mapOf("type" to "p", "text" to "Le système hydraulique permet d'actionner les gouvernes et le train d'atterrissage avec une force importante.")
            )
        )

        // Ajouter les leçons à Firestore
        db.collection("lessons")
            .add(droitAerienLesson)
            .addOnSuccessListener { documentReference ->
                println("Leçon 'Droit Aérien' ajoutée avec l'ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                println("Erreur lors de l'ajout de la leçon 'Droit Aérien': $e")
            }

        db.collection("lessons")
            .add(celluleLesson)
            .addOnSuccessListener { documentReference ->
                println("Leçon 'Cellule et Systèmes' ajoutée avec l'ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                println("Erreur lors de l'ajout de la leçon 'Cellule et Systèmes': $e")
            }
        */
    }
}