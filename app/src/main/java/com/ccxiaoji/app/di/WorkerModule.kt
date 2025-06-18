package com.ccxiaoji.app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {
    // HiltWorkerFactory is automatically provided by Hilt
    // No additional providers needed here
}