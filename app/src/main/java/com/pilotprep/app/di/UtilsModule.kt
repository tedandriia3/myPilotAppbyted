package com.pilotprep.app.di

import android.content.Context
import com.pilotprep.app.util.NetworkManager
import com.pilotprep.app.util.PerformanceMonitor
import com.pilotprep.app.util.MemoryManager
import com.pilotprep.app.util.ErrorHandler
import com.pilotprep.app.util.NavigationOptimizer
import com.pilotprep.app.util.StructuredLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UtilsModule {
    
    @Provides
    @Singleton
    fun provideNetworkManager(@ApplicationContext context: Context): NetworkManager {
        return NetworkManager(context)
    }
    
    @Provides
    @Singleton
    fun providePerformanceMonitor(@ApplicationContext context: Context): PerformanceMonitor {
        return PerformanceMonitor(context)
    }
    
    @Provides
    @Singleton
    fun provideMemoryManager(@ApplicationContext context: Context): MemoryManager {
        return MemoryManager(context)
    }
    
    @Provides
    @Singleton
    fun provideStructuredLogger(): StructuredLogger {
        return StructuredLogger()
    }
    
    @Provides
    @Singleton
    fun provideErrorHandler(@ApplicationContext context: Context, structuredLogger: StructuredLogger): ErrorHandler {
        return ErrorHandler(context, structuredLogger)
    }
    
    @Provides
    @Singleton
    fun provideNavigationOptimizer(): NavigationOptimizer {
        return NavigationOptimizer()
    }
}
