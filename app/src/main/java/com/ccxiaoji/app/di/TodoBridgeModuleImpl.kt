package com.ccxiaoji.app.di

import com.ccxiaoji.app.navigation.TodoNavigatorImpl
import com.ccxiaoji.app.notification.TodoNotificationSchedulerImpl
import com.ccxiaoji.feature.todo.api.TodoNavigator
import com.ccxiaoji.feature.todo.api.TodoNotificationScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Todo模块桥接接口的实现绑定
 * 在app模块中提供feature-todo模块所需的接口实现
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class TodoBridgeModuleImpl {
    
    @Binds
    @Singleton
    abstract fun bindTodoNavigator(impl: TodoNavigatorImpl): TodoNavigator
    
    @Binds
    @Singleton
    abstract fun bindTodoNotificationScheduler(impl: TodoNotificationSchedulerImpl): TodoNotificationScheduler
}