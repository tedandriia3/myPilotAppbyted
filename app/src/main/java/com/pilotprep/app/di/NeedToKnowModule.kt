package com.pilotprep.app.di

import com.google.firebase.firestore.FirebaseFirestore
import com.pilotprep.app.data.repository.NeedToKnowDataSeeder
import com.pilotprep.app.data.repository.NeedToKnowRepository
import com.pilotprep.app.util.ErrorHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NeedToKnowModule {
    
    @Provides
    @Singleton
    fun provideNeedToKnowRepository(
        firestore: FirebaseFirestore,
        errorHandler: ErrorHandler
    ): NeedToKnowRepository {
        return NeedToKnowRepository(firestore, errorHandler)
    }
    
    @Provides
    @Singleton
    fun provideNeedToKnowDataSeeder(
        firestore: FirebaseFirestore
    ): NeedToKnowDataSeeder {
        return NeedToKnowDataSeeder(firestore)
    }
}










