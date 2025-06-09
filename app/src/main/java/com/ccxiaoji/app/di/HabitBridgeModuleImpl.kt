package com.ccxiaoji.app.di

import com.ccxiaoji.app.navigation.HabitNavigatorImpl
import com.ccxiaoji.app.notification.HabitReminderSchedulerImpl
import com.ccxiaoji.feature.habit.api.HabitNavigator
import com.ccxiaoji.feature.habit.api.HabitReminderScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Habit模块桥接接口的实现绑定
 * 在app模块中提供feature-habit模块所需的接口实现
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class HabitBridgeModuleImpl {
    
    @Binds
    @Singleton
    abstract fun bindHabitNavigator(impl: HabitNavigatorImpl): HabitNavigator
    
    @Binds
    @Singleton
    abstract fun bindHabitReminderScheduler(impl: HabitReminderSchedulerImpl): HabitReminderScheduler
}