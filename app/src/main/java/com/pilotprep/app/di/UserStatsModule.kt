package com.pilotprep.app.di

import com.pilotprep.app.data.repository.UserStatsRepository
import com.pilotprep.app.util.ErrorHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserStatsModule {

    @Provides
    @Singleton
    fun provideUserStatsRepository(errorHandler: ErrorHandler): UserStatsRepository {
        return UserStatsRepository(errorHandler)
    }
}

