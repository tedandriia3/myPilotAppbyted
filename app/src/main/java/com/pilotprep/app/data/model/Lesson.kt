package com.pilotprep.app.data.model

import androidx.annotation.Keep
import com.google.firebase.firestore.PropertyName

@Keep
data class Lesson(
    // On garde l'ID qui est essentiel
    val id: String = "",

    // Le log disait que Firestore trouvait "category", pas "license".
    // On va donc utiliser "category" et on peut ajouter une annotation
    // si jamais le champ dans Firestore s'appelle vraiment "license".
    // Pour l'instant, on se fie au log.
    val category: String = "",

    // Les champs que vous aviez déjà et qui sont corrects
    val subject: String = "",
    val title: String = "",

    // LE CHANGEMENT LE PLUS IMPORTANT :
    // On passe le type à Any? (n'importe quel type, peut être null).
    // Cela permet à Firestore de mettre un String, une List, ou une Map,
    // et à votre LessonDetailScreen de faire le tri.
    val content: Any? = null,

    // On ajoute le champ manquant que Firestore a trouvé dans les logs.
    // L'annotation @get:PropertyName est une bonne pratique si votre
    // nom de variable Kotlin est différent du nom de champ dans Firestore.
    @get:PropertyName("orderIndex")
    val orderIndex: Long = 0
)

// La classe LessonContent n'est plus nécessaire car nous traitons
// le contenu de manière flexible directement dans l'écran de détail.
// Vous pouvez donc la supprimer.