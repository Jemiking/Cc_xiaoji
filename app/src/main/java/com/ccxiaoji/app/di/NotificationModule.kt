package com.ccxiaoji.app.di

import android.content.Context
import com.ccxiaoji.app.R
import com.ccxiaoji.app.presentation.MainActivity
import com.ccxiaoji.shared.notification.domain.model.NotificationConfig
import com.ccxiaoji.shared.notification.api.NotificationApi
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
    fun provideNotificationConfig(
        @ApplicationContext context: Context
    ): NotificationConfig {
        return NotificationConfig(
            mainActivityClass = MainActivity::class.java,
            smallIconResourceId = R.drawable.ic_launcher_foreground,
            packageName = context.packageName
        )
    }
    
    @Provides
    @Singleton
    fun provideNotificationScheduler(
        @ApplicationContext context: Context,
        notificationApi: NotificationApi
    ): NotificationScheduler {
        return NotificationScheduler(context, notificationApi)
    }
}