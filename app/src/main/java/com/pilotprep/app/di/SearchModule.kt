package com.pilotprep.app.di

import com.google.firebase.firestore.FirebaseFirestore
import com.pilotprep.app.data.repository.SearchRepository
import com.pilotprep.app.util.ErrorHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SearchModule {
    
    @Provides
    @Singleton
    fun provideSearchRepository(
        firestore: FirebaseFirestore,
        errorHandler: ErrorHandler
    ): SearchRepository {
        return SearchRepository(firestore, errorHandler)
    }
}







