package com.pilotprep.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.pilotprep.app.data.model.ContentBlock
import com.pilotprep.app.data.model.NeedToKnowContent
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NeedToKnowDataSeeder @Inject constructor(
    private val db: FirebaseFirestore
) {
    private val collection = db.collection("need_to_know")
    
    /**
     * Ajoute des contenus d'exemple pour "Need to Know"
     */
    suspend fun seedNeedToKnowData() {
        try {
            println("🌱 DEBUG: Début du seeding des données Need to Know...")
            
            val contents = NeedToKnowTestData.createSampleContent()
            
            contents.forEach { content ->
                try {
                    val docRef = collection.add(content).await()
                    println("✅ DEBUG: Contenu ajouté avec l'ID: ${docRef.id}")
                } catch (e: Exception) {
                    println("❌ DEBUG: Erreur lors de l'ajout du contenu: ${e.message}")
                }
            }
            
            println("🎉 DEBUG: Seeding terminé avec succès!")
        } catch (e: Exception) {
            println("❌ DEBUG: Erreur générale lors du seeding: ${e.message}")
            e.printStackTrace()
        }
    }
}