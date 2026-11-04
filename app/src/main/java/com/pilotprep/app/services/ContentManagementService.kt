package com.pilotprep.app.services

import com.google.firebase.firestore.FirebaseFirestore
import com.pilotprep.app.data.model.NeedToKnowContent
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentManagementService @Inject constructor(
    private val db: FirebaseFirestore
) {
    private val collection = db.collection("need_to_know")
    
    /**
     * Ajoute un nouveau contenu
     */
    suspend fun addContent(content: NeedToKnowContent): String? {
        return try {
            println("🔍 DEBUG: Ajout du contenu '${content.title}'...")
            val docRef = collection.add(content).await()
            println("✅ DEBUG: Contenu ajouté avec l'ID: ${docRef.id}")
            docRef.id
        } catch (e: Exception) {
            println("❌ DEBUG: Erreur lors de l'ajout: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Ajoute un contenu simple avec titre et texte
     */
    suspend fun addSimpleContent(title: String, text: String): String? {
        val content = NeedToKnowContent(
            license = "need_to_know",
            title = title,
            content = listOf(
                com.pilotprep.app.data.model.ContentBlock(
                    type = "h2",
                    text = title
                ),
                com.pilotprep.app.data.model.ContentBlock(
                    type = "p",
                    text = text
                )
            )
        )
        return addContent(content)
    }
    
    /**
     * Ajoute un contenu avec des blocs personnalisés
     */
    suspend fun addContentWithBlocks(title: String, blocks: List<Map<String, Any>>): String? {
        val contentBlocks = blocks.map { block ->
            com.pilotprep.app.data.model.ContentBlock(
                type = block["type"] as? String ?: "p",
                text = block["text"] as? String ?: "",
                imageUrl = block["imageUrl"] as? String ?: "",
                items = block["items"] as? List<String> ?: emptyList()
            )
        }
        
        val content = NeedToKnowContent(
            license = "need_to_know",
            title = title,
            content = contentBlocks
        )
        return addContent(content)
    }
    
    /**
     * Supprime un contenu
     */
    suspend fun deleteContent(contentId: String): Boolean {
        return try {
            println("🔍 DEBUG: Suppression du contenu ID: $contentId...")
            collection.document(contentId).delete().await()
            println("✅ DEBUG: Contenu supprimé avec succès")
            true
        } catch (e: Exception) {
            println("❌ DEBUG: Erreur lors de la suppression: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Ajoute des données de test
     */
    suspend fun addTestData(): List<String> {
        val testContents = listOf(
            NeedToKnowContent(
                license = "need_to_know",
                title = "Crew Resource Management (CRM)",
                content = listOf(
                    com.pilotprep.app.data.model.ContentBlock(
                        type = "h2",
                        text = "Introduction au CRM"
                    ),
                    com.pilotprep.app.data.model.ContentBlock(
                        type = "p",
                        text = "Le Crew Resource Management (CRM) est une approche systémique de la sécurité aérienne qui met l'accent sur l'utilisation efficace de toutes les ressources disponibles."
                    ),
                    com.pilotprep.app.data.model.ContentBlock(
                        type = "h2",
                        text = "Les 5 piliers du CRM"
                    ),
                    com.pilotprep.app.data.model.ContentBlock(
                        type = "p",
                        text = "Le CRM repose sur cinq piliers fondamentaux :"
                    ),
                    com.pilotprep.app.data.model.ContentBlock(
                        type = "ul",
                        items = listOf(
                            "Communication efficace",
                            "Leadership situationnel",
                            "Prise de décision",
                            "Gestion des erreurs",
                            "Travail d'équipe"
                        )
                    )
                )
            ),
            NeedToKnowContent(
                license = "need_to_know",
                title = "Gestion des Pannes Moteur",
                content = listOf(
                    com.pilotprep.app.data.model.ContentBlock(
                        type = "h2",
                        text = "Procédures d'urgence"
                    ),
                    com.pilotprep.app.data.model.ContentBlock(
                        type = "p",
                        text = "En cas de panne moteur, les pilotes doivent suivre une séquence de vérifications et d'actions précises pour assurer la sécurité du vol."
                    ),
                    com.pilotprep.app.data.model.ContentBlock(
                        type = "h2",
                        text = "Checklist de sécurité"
                    ),
                    com.pilotprep.app.data.model.ContentBlock(
                        type = "ol",
                        items = listOf(
                            "Maintenir le contrôle de l'avion",
                            "Identifier le problème",
                            "Appliquer les procédures d'urgence",
                            "Communiquer avec le contrôle aérien",
                            "Préparer l'atterrissage d'urgence"
                        )
                    )
                )
            )
        )
        
        val addedIds = mutableListOf<String>()
        for (content in testContents) {
            val id = addContent(content)
            if (id != null) {
                addedIds.add(id)
            }
        }
        
        return addedIds
    }
}









