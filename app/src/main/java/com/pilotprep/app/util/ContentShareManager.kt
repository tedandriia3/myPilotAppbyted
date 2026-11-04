package com.pilotprep.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

/**
 * Gestionnaire de partage de contenu
 */
class ContentShareManager(private val context: Context) {
    
    private val TAG = "ContentShareManager"
    
    /**
     * Partage du texte
     */
    fun shareText(title: String, content: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, content)
            }
            
            val chooser = Intent.createChooser(intent, "Partager via")
            chooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(chooser)
            
            Log.d(TAG, "✅ Partage lancé: $title")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur lors du partage: ${e.message}")
        }
    }
    
    /**
     * Partage d'une image (URI)
     */
    fun shareImage(uri: Uri, title: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
            }
            
            val chooser = Intent.createChooser(intent, "Partager l'image")
            chooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(chooser)
            
            Log.d(TAG, "✅ Partage d'image lancé")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur lors du partage d'image: ${e.message}")
        }
    }
    
    /**
     * Partage de contenu pédagogique formaté
     */
    fun shareLesson(lessonTitle: String, lessonContent: String, subject: String) {
        val formattedContent = """
            ✈️ Pilot Prep - Leçon partagée
            
            📚 $subject
            📝 $lessonTitle
            
            $lessonContent
            
            --
            Apprenez avec Pilot Prep
        """.trimIndent()
        
        shareText("Leçon: $lessonTitle", formattedContent)
    }
    
    /**
     * Partage d'un quiz ou d'une question
     */
    fun shareQuiz(question: String, subject: String) {
        val formattedContent = """
            ✈️ Pilot Prep - Quiz
            
            📚 $subject
            
            Question:
            $question
            
            --
            Testez vos connaissances avec Pilot Prep
        """.trimIndent()
        
        shareText("Quiz: $subject", formattedContent)
    }
    
    /**
     * Exporte les statistiques au format texte
     */
    fun exportStats(stats: Map<String, Any>): String {
        return buildString {
            appendLine("✈️ Pilot Prep - Statistiques")
            appendLine("=".repeat(30))
            appendLine()
            
            stats.forEach { (key, value) ->
                appendLine("📊 $key: $value")
            }
            
            appendLine()
            appendLine("--")
            appendLine("Pilot Prep - Votre partenaire d'apprentissage")
        }
    }
}
