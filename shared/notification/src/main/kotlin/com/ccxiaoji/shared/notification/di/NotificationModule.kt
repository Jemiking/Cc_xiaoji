package com.ccxiaoji.shared.notification.di

import com.ccxiaoji.shared.notification.api.NotificationApi
import com.ccxiaoji.shared.notification.data.NotificationApiImpl
import com.ccxiaoji.shared.notification.domain.model.NotificationConfig
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 通知模块的依赖注入配置
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {
    
    @Binds
    @Singleton
    abstract fun bindNotificationApi(impl: NotificationApiImpl): NotificationApi
}

/**
 * 提供NotificationConfig的模块
 * 这个需要在app模块中提供实际的配置
 */
@Module
@InstallIn(SingletonComponent::class)
interface NotificationConfigModule {
    companion object {
        @Provides
        @Singleton
        fun provideNotificationConfig(): NotificationConfig {
            // 这个方法会在app模块中被覆盖
            throw NotImplementedError("NotificationConfig must be provided by app module")
        }
    }
}