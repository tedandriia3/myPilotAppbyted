package com.pilotprep.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// On a retiré toute la logique liée au pré-remplissage depuis un fichier JSON
// car nous utilisons maintenant Firebase.

@Database(entities = [QuestionEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun questionDao(): QuestionDao

    // Le 'companion object' avec la logique de création de la base de données
    // n'est plus nécessaire ici car Hilt va s'en charger.
    // Nous allons le définir dans un module Hilt pour une meilleure pratique.
}