package com.pilotprep.app.data.model

import androidx.annotation.Keep

// @Keep est une bonne pratique pour s'assurer que Proguard/R8 ne supprime pas cette classe
@Keep
data class Aircraft(
    // On garde l'ID en val car on le définit manuellement, mais les autres en var
    val id: String = "",
    var name: String = "",
    var year: String = "",
    var engine: String = "",
    var maxSpeed: String = "",
    var wingspan: String = "",
    var description: String = "",
    var imageUrl: String = "",
    var category: String = "" // Catégorie de l'avion (ex: "Commercial", "Militaire", "Privé")
)