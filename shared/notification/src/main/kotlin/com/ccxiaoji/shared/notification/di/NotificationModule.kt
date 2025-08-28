package com.ccxiaoji.shared.notification.di

import com.ccxiaoji.shared.notification.api.NotificationApi
import com.ccxiaoji.shared.notification.api.NotificationEventRepository
import com.ccxiaoji.shared.notification.api.NotificationAccessController
import com.ccxiaoji.shared.notification.data.NotificationApiImpl
import com.ccxiaoji.shared.notification.data.NotificationEventRepositoryImpl
import com.ccxiaoji.shared.notification.data.NotificationAccessControllerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 通知模块的依赖注入配置
 * 
 * 提供两类通知服务：
 * 1. NotificationApi - 用于发送应用内通知
 * 2. NotificationEventRepository - 用于监听系统通知事件
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {
    
    @Binds
    @Singleton
    abstract fun bindNotificationApi(impl: NotificationApiImpl): NotificationApi
    
    @Binds
    @Singleton
    abstract fun bindNotificationEventRepository(
        impl: NotificationEventRepositoryImpl
    ): NotificationEventRepository

    @Binds
    @Singleton
    abstract fun bindNotificationAccessController(
        impl: NotificationAccessControllerImpl
    ): NotificationAccessController
}
