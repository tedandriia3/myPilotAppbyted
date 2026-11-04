package com.pilotprep.app.di

import com.pilotprep.app.data.repository.WeeklyTipRepository
import com.pilotprep.app.data.repository.WeeklyTipRepositoryImpl
import com.pilotprep.app.util.ErrorHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WeeklyTipModule {

    @Provides
    @Singleton
    fun provideWeeklyTipRepository(errorHandler: ErrorHandler): WeeklyTipRepository {
        return WeeklyTipRepositoryImpl(errorHandler)
    }
}





















