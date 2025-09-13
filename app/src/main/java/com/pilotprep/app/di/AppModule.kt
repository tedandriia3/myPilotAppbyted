package com.pilotprep.app.di

import android.content.Context
import com.pilotprep.app.data.QuestionRepository
import com.pilotprep.app.data.local.AppDatabase
import com.pilotprep.app.data.local.QuestionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideQuestionDao(appDatabase: AppDatabase): QuestionDao {
        return appDatabase.questionDao()
    }

    @Provides
    @Singleton
    fun provideQuestionRepository(questionDao: QuestionDao): QuestionRepository {
        return QuestionRepository(questionDao)
    }
}