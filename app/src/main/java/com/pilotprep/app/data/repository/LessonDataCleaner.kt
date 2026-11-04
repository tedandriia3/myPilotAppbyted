package com.pilotprep.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.pilotprep.app.data.model.Lesson
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LessonDataCleaner @Inject constructor(
    private val db: FirebaseFirestore
) {

    /**
     * Nettoie les données de leçons en supprimant les doublons et en s'assurant
     * que chaque matière a son propre contenu unique
     */
    suspend fun cleanLessonData() {
        cleanLessonData(forceUpdate = false)
    }

    /**
     * Nettoie les données de leçons avec option de forcer la mise à jour
     */
    suspend fun cleanLessonData(forceUpdate: Boolean = false) {
        try {
            println("🧹 Début du nettoyage des données de leçons...")
            
            // Récupérer toutes les leçons
            val snapshot = db.collection("lessons").get().await()
            val lessons = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Lesson::class.java)?.copy(id = doc.id)
            }
            
            println("📊 ${lessons.size} leçons trouvées dans Firebase")
            
            // Grouper par matière
            val lessonsBySubject = lessons.groupBy { it.subject }
            
            // Pour chaque matière, vérifier s'il y a des doublons et mettre à jour le contenu
            lessonsBySubject.forEach { (subject, subjectLessons) ->
                println("🔍 Vérification de la matière: $subject (${subjectLessons.size} leçons)")
                
                if (subjectLessons.size > 1) {
                    println("⚠️  Doublons détectés pour '$subject' - suppression des doublons...")
                    
                    // Garder seulement la première leçon et supprimer les autres
                    val lessonsToDelete = subjectLessons.drop(1)
                    
                    for (lesson in lessonsToDelete) {
                        try {
                            db.collection("lessons").document(lesson.id).delete().await()
                            println("✅ Leçon dupliquée supprimée: ${lesson.title} (ID: ${lesson.id})")
                        } catch (e: Exception) {
                            println("❌ Erreur lors de la suppression de ${lesson.title}: ${e.message}")
                        }
                    }
                }
                
                // Mettre à jour le contenu de la leçon restante avec du contenu unique
                val remainingLesson = subjectLessons.first()
                try {
                    val uniqueContent = createUniqueContentForSubject(subject)
                    val updatedLesson = remainingLesson.copy(content = uniqueContent)
                    
                    db.collection("lessons").document(remainingLesson.id).set(updatedLesson).await()
                    println("✅ Contenu unique mis à jour pour '$subject'")
                } catch (e: Exception) {
                    println("❌ Erreur lors de la mise à jour du contenu pour '$subject': ${e.message}")
                }
            }
            
            // Vérifier que chaque matière a du contenu unique
            ensureUniqueContentForEachSubject()
            
            println("✅ Nettoyage terminé avec succès!")
            
        } catch (e: Exception) {
            println("❌ Erreur lors du nettoyage: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * S'assure que chaque matière a son propre contenu unique
     */
    private suspend fun ensureUniqueContentForEachSubject() {
        val subjects = listOf(
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
            "COMMUNICATION (VFR - IFR)"
        )
        
        for (subject in subjects) {
            val existingLessons = db.collection("lessons")
                .whereEqualTo("category", "summary")
                .whereEqualTo("subject", subject)
                .get().await()
            
            if (existingLessons.isEmpty) {
                println("📝 Création du contenu pour: $subject")
                val content = createUniqueContentForSubject(subject)
                val lesson = Lesson(
                    id = "",
                    category = "summary",
                    subject = subject,
                    title = "Résumé - $subject",
                    content = content
                )
                
                try {
                    db.collection("lessons").add(lesson).await()
                    println("✅ Contenu créé pour: $subject")
                } catch (e: Exception) {
                    println("❌ Erreur lors de la création du contenu pour $subject: ${e.message}")
                }
            } else {
                // Mettre à jour le contenu de la leçon existante
                val lessonDoc = existingLessons.documents.first()
                val lesson = lessonDoc.toObject(Lesson::class.java)?.copy(id = lessonDoc.id)
                
                if (lesson != null) {
                    try {
                        val uniqueContent = createUniqueContentForSubject(subject)
                        val updatedLesson = lesson.copy(content = uniqueContent)
                        
                        db.collection("lessons").document(lesson.id).set(updatedLesson).await()
                        println("✅ Contenu unique mis à jour pour: $subject")
                    } catch (e: Exception) {
                        println("❌ Erreur lors de la mise à jour du contenu pour '$subject': ${e.message}")
                    }
                }
            }
        }
    }
    
    /**
     * Crée du contenu unique pour une matière spécifique
     */
    private fun createUniqueContentForSubject(subject: String): List<Map<String, String>> {
        val content = when (subject) {
            "DROIT AÉRIEN ET PROCÉDURES ATC" -> listOf(
                mapOf("type" to "h2", "text" to "Introduction au droit aérien"),
                mapOf("type" to "p", "text" to "Le droit aérien régit l'ensemble des règles qui s'appliquent à la navigation aérienne. Ces règles sont établies par l'Organisation de l'Aviation Civile Internationale (OACI)."),
                mapOf("type" to "h2", "text" to "Les classes d'espace aérien"),
                mapOf("type" to "p", "text" to "L'espace aérien est divisé en plusieurs classes : A, B, C, D, E, F et G. Chaque classe a ses propres règles d'accès et de communication."),
                mapOf("type" to "h2", "text" to "Responsabilités du pilote"),
                mapOf("type" to "p", "text" to "Le pilote commandant de bord est responsable de la sécurité de l'aéronef et de ses occupants. Il doit respecter les règles de l'air et suivre les instructions du contrôle aérien.")
            )
            "CELLULE ET SYSTÈMES" -> listOf(
                mapOf("type" to "h2", "text" to "Structure générale"),
                mapOf("type" to "p", "text" to "Un avion se compose principalement de la cellule (fuselage, ailes, empennage), du groupe motopropulseur et des systèmes auxiliaires."),
                mapOf("type" to "h2", "text" to "Systèmes hydrauliques"),
                mapOf("type" to "p", "text" to "Le système hydraulique permet d'actionner les gouvernes et le train d'atterrissage avec une force importante."),
                mapOf("type" to "h2", "text" to "Systèmes électriques"),
                mapOf("type" to "p", "text" to "Le système électrique alimente tous les équipements de bord et assure le fonctionnement des instruments.")
            )
            "INSTRUMENTATION" -> listOf(
                mapOf("type" to "h2", "text" to "Instruments de vol"),
                mapOf("type" to "p", "text" to "Les instruments de vol permettent au pilote de connaître l'attitude, l'altitude, la vitesse et la direction de l'avion."),
                mapOf("type" to "h2", "text" to "Horizon artificiel"),
                mapOf("type" to "p", "text" to "L'horizon artificiel indique l'attitude de l'avion par rapport à l'horizon naturel."),
                mapOf("type" to "h2", "text" to "Anémomètre"),
                mapOf("type" to "p", "text" to "L'anémomètre mesure la vitesse de l'avion par rapport à l'air.")
            )
            "MASSES ET CENTRAGE" -> listOf(
                mapOf("type" to "h2", "text" to "Calcul des masses"),
                mapOf("type" to "p", "text" to "Le calcul des masses est essentiel pour la sécurité du vol. Il faut tenir compte de la masse à vide, du carburant, des passagers et du fret."),
                mapOf("type" to "h2", "text" to "Centrage"),
                mapOf("type" to "p", "text" to "Le centrage influence la stabilité et la contrôlabilité de l'avion. Il doit rester dans les limites autorisées."),
                mapOf("type" to "h2", "text" to "Limites de masse"),
                mapOf("type" to "p", "text" to "Respecter les limites de masse maximale au décollage et à l'atterrissage est crucial pour la sécurité.")
            )
            "PERFORMANCES AVIONS" -> listOf(
                mapOf("type" to "h2", "text" to "Performances au décollage"),
                mapOf("type" to "p", "text" to "Les performances au décollage dépendent de la température, de l'altitude, de la pente de piste et du vent."),
                mapOf("type" to "h2", "text" to "Performances en montée"),
                mapOf("type" to "p", "text" to "Le taux de montée varie avec l'altitude et la température. Il est maximum au niveau de la mer et diminue avec l'altitude."),
                mapOf("type" to "h2", "text" to "Performances à l'atterrissage"),
                mapOf("type" to "p", "text" to "La distance d'atterrissage dépend de la masse, de la vitesse d'approche et des conditions de piste.")
            )
            "PRÉPARATION ET SUIVI DU VOL" -> listOf(
                mapOf("type" to "h2", "text" to "Préparation du vol"),
                mapOf("type" to "p", "text" to "La préparation du vol comprend l'étude des conditions météorologiques, le calcul des performances et la planification de la route."),
                mapOf("type" to "h2", "text" to "Briefing météorologique"),
                mapOf("type" to "p", "text" to "Le briefing météorologique fournit les informations essentielles sur les conditions en vol et aux aérodromes."),
                mapOf("type" to "h2", "text" to "Plan de vol"),
                mapOf("type" to "p", "text" to "Le plan de vol détaille la route, les altitudes, les temps de vol et les carburants nécessaires.")
            )
            "PERFORMANCE HUMAINE" -> listOf(
                mapOf("type" to "h2", "text" to "Facteurs humains"),
                mapOf("type" to "p", "text" to "Les facteurs humains influencent la performance du pilote. Il faut connaître ses limites et gérer la fatigue."),
                mapOf("type" to "h2", "text" to "Gestion de la fatigue"),
                mapOf("type" to "p", "text" to "La fatigue réduit la vigilance et les performances. Il est important de bien se reposer avant un vol."),
                mapOf("type" to "h2", "text" to "Stress et pression"),
                mapOf("type" to "p", "text" to "Le stress peut affecter la prise de décision. Il faut apprendre à le gérer efficacement.")
            )
            "MÉTÉOROLOGIE" -> listOf(
                mapOf("type" to "h2", "text" to "Atmosphère et pression"),
                mapOf("type" to "p", "text" to "L'atmosphère terrestre exerce une pression qui diminue avec l'altitude. Cette variation influence les performances de l'avion."),
                mapOf("type" to "h2", "text" to "Systèmes météorologiques"),
                mapOf("type" to "p", "text" to "Les dépressions et anticyclones créent des conditions météorologiques spécifiques qu'il faut comprendre."),
                mapOf("type" to "h2", "text" to "Nuages et précipitations"),
                mapOf("type" to "p", "text" to "Les différents types de nuages indiquent les conditions météorologiques et les risques associés.")
            )
            "NAVIGATION" -> listOf(
                mapOf("type" to "h2", "text" to "Navigation à l'estime"),
                mapOf("type" to "p", "text" to "La navigation à l'estime consiste à calculer la position en fonction de la route, de la vitesse et du temps écoulé."),
                mapOf("type" to "h2", "text" to "Navigation visuelle"),
                mapOf("type" to "p", "text" to "La navigation visuelle utilise les repères au sol pour maintenir la route."),
                mapOf("type" to "h2", "text" to "Cartes aéronautiques"),
                mapOf("type" to "p", "text" to "Les cartes aéronautiques contiennent toutes les informations nécessaires à la navigation.")
            )
            "RADIONAVIGATION" -> listOf(
                mapOf("type" to "h2", "text" to "VOR (VHF Omnidirectional Range)"),
                mapOf("type" to "p", "text" to "Le VOR permet de déterminer la position par rapport à une balise au sol."),
                mapOf("type" to "h2", "text" to "NDB (Non-Directional Beacon)"),
                mapOf("type" to "p", "text" to "L'NDB est une balise qui émet dans toutes les directions et permet la navigation."),
                mapOf("type" to "h2", "text" to "GPS"),
                mapOf("type" to "p", "text" to "Le GPS (Global Positioning System) fournit une position précise en temps réel.")
            )
            "PROCÉDURES OPÉRATIONNELLES" -> listOf(
                mapOf("type" to "h2", "text" to "Procédures de décollage"),
                mapOf("type" to "p", "text" to "Les procédures de décollage comprennent les vérifications pré-vol, la mise en route et le roulage."),
                mapOf("type" to "h2", "text" to "Procédures d'approche"),
                mapOf("type" to "p", "text" to "Les procédures d'approche varient selon le type d'approche et les conditions météorologiques."),
                mapOf("type" to "h2", "text" to "Procédures d'urgence"),
                mapOf("type" to "p", "text" to "Les procédures d'urgence doivent être connues et appliquées en cas de problème.")
            )
            "PRINCIPES DU VOL" -> listOf(
                mapOf("type" to "h2", "text" to "Forces en vol"),
                mapOf("type" to "p", "text" to "Quatre forces principales agissent sur un avion en vol : portance, poids, traction et traînée."),
                mapOf("type" to "h2", "text" to "Portance"),
                mapOf("type" to "p", "text" to "La portance est générée par le profil de l'aile et permet à l'avion de voler."),
                mapOf("type" to "h2", "text" to "Stabilité"),
                mapOf("type" to "p", "text" to "La stabilité de l'avion dépend de la position du centre de gravité et du centre de portance.")
            )
            "COMMUNICATION (VFR - IFR)" -> listOf(
                mapOf("type" to "h2", "text" to "Communications VFR"),
                mapOf("type" to "p", "text" to "Les communications VFR sont plus simples et concernent principalement la circulation aérienne générale."),
                mapOf("type" to "h2", "text" to "Communications IFR"),
                mapOf("type" to "p", "text" to "Les communications IFR sont plus complexes et incluent les plans de vol et les clairances."),
                mapOf("type" to "h2", "text" to "Phonétique aéronautique"),
                mapOf("type" to "p", "text" to "L'alphabet phonétique international est utilisé pour éviter les erreurs de communication.")
            )
            else -> listOf(
                mapOf("type" to "h2", "text" to "Contenu en cours de développement"),
                mapOf("type" to "p", "text" to "Le contenu pour cette matière sera bientôt disponible.")
            )
        }
        
        return content
    }
}
