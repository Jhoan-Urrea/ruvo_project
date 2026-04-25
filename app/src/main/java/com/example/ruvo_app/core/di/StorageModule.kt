package com.example.ruvo_app.core.di

import android.content.Context
import com.example.ruvo_app.data.repository.CloudinaryImageService
import com.example.ruvo_app.domain.repository.ImageStorageService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Provides
    @Singleton
    fun provideImageStorageService(
        @ApplicationContext context: Context
    ): ImageStorageService {
        return CloudinaryImageService(context)
    }
}
