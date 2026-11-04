package com.pilotprep.app.di

import com.pilotprep.app.data.repository.AircraftRepository
import com.pilotprep.app.data.repository.ImageOfDayRepository
import com.pilotprep.app.data.repository.ImageOfDayRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ImageOfDayModule {

    @Provides
    @Singleton
    fun provideImageOfDayRepository(
        aircraftRepository: AircraftRepository
    ): ImageOfDayRepository {
        return ImageOfDayRepositoryImpl(aircraftRepository)
    }
}

