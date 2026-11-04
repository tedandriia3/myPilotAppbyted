package com.pilotprep.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.pilotprep.app.data.model.NeedToKnowContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import com.pilotprep.app.util.ErrorHandler
import com.pilotprep.app.util.ErrorSeverity
import com.pilotprep.app.util.NetworkRetry
import com.pilotprep.app.util.DataValidator

@Singleton
class NeedToKnowRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val errorHandler: ErrorHandler
) {
    
    /**
     * Récupère tous les contenus "Need to Know" publiés
     */
    suspend fun getAllContents(): List<NeedToKnowContent> {
        return try {
            val snapshot = NetworkRetry.retryWithBackoff(maxRetries = 3, initialDelay = 800L) {
                Result.success(db.collection("need_to_know").get().await())
            }.getOrThrow()
            snapshot.documents.mapNotNull { doc ->
                try {
                    mapDocToNeedToKnow(doc.id, doc.data)
                } catch (e: Exception) {
                    errorHandler.logError(e, "NeedToKnowRepository#getAllContents", ErrorSeverity.LOW)
                    null
                }
            }.filter { DataValidator.isValidNeedToKnowContent(it) }
        } catch (e: Exception) {
            errorHandler.logError(e, "NeedToKnowRepository#getAllContents", ErrorSeverity.MEDIUM)
            emptyList()
        }
    }
    
    
    /**
     * Récupère un contenu par son ID
     */
    suspend fun getContentById(contentId: String): NeedToKnowContent? {
        return try {
            val doc = NetworkRetry.retryWithBackoff(maxRetries = 3, initialDelay = 800L) {
                Result.success(db.collection("need_to_know").document(contentId).get().await())
            }.getOrThrow()
            if (!doc.exists()) return null
            mapDocToNeedToKnow(doc.id, doc.data)
        } catch (e: Exception) {
            errorHandler.logError(e, "NeedToKnowRepository#getContentById", ErrorSeverity.MEDIUM)
            null
        }
    }

    private fun mapDocToNeedToKnow(id: String, data: Map<String, Any>?): NeedToKnowContent? {
        val map = data ?: return null
        val title = (map["title"] as? String)?.trim().orEmpty()
        if (title.isEmpty()) return null
        val licenseRaw = (map["license"] as? String)?.trim().orEmpty()
        val license = if (licenseRaw.isNotEmpty()) licenseRaw else "need_to_know"

        val rawBlocks = when (val blocks = map["content"] ?: map["blocks"] ?: map["items"]) {
            is List<*> -> blocks
            else -> emptyList<Any?>()
        }

        val contentBlocks = rawBlocks.mapNotNull { any ->
            val b = any as? Map<*, *> ?: return@mapNotNull null
            val type = (b["type"] as? String)?.trim().orEmpty()
            val text = b["text"]?.toString()?.trim().orEmpty()
            val imageUrl = b["imageUrl"]?.toString()?.trim().orEmpty().ifEmpty { b["url"]?.toString()?.trim().orEmpty() }
            val items = (b["items"] as? List<*>)?.mapNotNull { it?.toString() }.orEmpty()
            val style = (b["style"] as? String)?.trim() ?: "normal"
            val colorHex = (b["colorHex"] as? String)?.trim().orEmpty()

            com.pilotprep.app.data.model.ContentBlock(
                type = type,
                text = text,
                imageUrl = imageUrl,
                items = items,
                style = style,
                colorHex = colorHex
            ).takeIf { DataValidator.isValidContentBlock(it) }
        }

        val content = com.pilotprep.app.data.model.NeedToKnowContent(
            id = id,
            license = license,
            title = title,
            content = contentBlocks
        )
        return content.takeIf { DataValidator.isValidNeedToKnowContent(it) }
    }
    
}
