package com.ccxiaoji.shared.notification.di

import com.ccxiaoji.shared.notification.api.NotificationApi
import com.ccxiaoji.shared.notification.data.NotificationApiImpl
import dagger.Binds
import dagger.Module
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