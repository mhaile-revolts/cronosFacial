package com.cronosedx.cronosfacial.di

import com.cronosedx.cronosfacial.network.AiMlService
import com.cronosedx.cronosfacial.network.ApiService
import com.cronosedx.cronosfacial.network.RetrofitInstance
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for application-level dependencies.
 * 
 * Provides singleton instances of network services and other app-wide dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    /**
     * Provides the ApiService for legacy API calls
     */
    @Provides
    @Singleton
    fun provideApiService(): ApiService {
        return RetrofitInstance.api
    }
    
    /**
     * Provides the AiMlService for AI/ML backend integration
     */
    @Provides
    @Singleton
    fun provideAiMlService(): AiMlService {
        return AiMlService()
    }
}
