package com.pilotprep.app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {
    // Le WorkerModule n'est plus nécessaire car ImageOfDayWorkerManager
    // utilise maintenant @Inject dans son constructeur
}
