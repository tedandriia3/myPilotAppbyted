package com.pilotprep.app.data.model

import androidx.annotation.Keep
import com.google.firebase.firestore.PropertyName

@Keep
data class NeedToKnowContent(
    val id: String = "",
    val license: String = "need_to_know", // Toujours "need_to_know"
    val title: String = "",
    val content: List<ContentBlock> = emptyList()
)

@Keep
data class ContentBlock(
    val type: String = "", // "h1", "h2", "h3", "p", "ul", "ol", "quote", "warning", "tip", "image"
    val text: String = "",
    val imageUrl: String = "", // Pour les images
    val items: List<String> = emptyList(), // Pour les listes
    val style: String = "normal", // "normal", "highlight", "important"
    val colorHex: String = "" // Optionnel: couleur spécifique (ex: h2ColorHex)
) {
    // Constructeur par défaut pour Firebase
    constructor() : this("", "", "", emptyList(), "normal", "")
}

@Keep
data class NeedToKnowUiState(
    val contents: List<NeedToKnowContent> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

