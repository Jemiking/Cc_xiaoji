package com.ccxiaoji.app.di

import android.content.Context
import com.ccxiaoji.app.notification.NotificationManager
import com.ccxiaoji.app.notification.NotificationScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {
    
    @Provides
    @Singleton
    fun provideNotificationManager(
        @ApplicationContext context: Context
    ): NotificationManager {
        return NotificationManager(context)
    }
    
    @Provides
    @Singleton
    fun provideNotificationScheduler(
        @ApplicationContext context: Context,
        notificationManager: NotificationManager
    ): NotificationScheduler {
        return NotificationScheduler(context, notificationManager)
    }
}