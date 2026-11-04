package com.pilotprep.app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Module principal qui inclut tous les autres modules
 */
@Module(includes = [
    DatabaseModule::class,
    WorkerModule::class,
    UtilsModule::class,
    FirebaseModule::class,
    ImageOfDayModule::class,
    AudioModule::class
])
@InstallIn(SingletonComponent::class)
object AppModule
