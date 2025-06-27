package com.ccxiaoji.feature.schedule.di

import android.content.Context
import com.ccxiaoji.feature.schedule.notification.ScheduleNotificationScheduler
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
    fun provideScheduleNotificationScheduler(
        @ApplicationContext context: Context
    ): ScheduleNotificationScheduler {
        return ScheduleNotificationScheduler(context)
    }
}