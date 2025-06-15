package com.ccxiaoji.app.di

import com.ccxiaoji.app.navigation.ScheduleNavigatorImpl
import com.ccxiaoji.feature.schedule.api.ScheduleNavigator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 导航模块依赖注入配置
 * 提供各模块导航器的实现绑定
 * TODO: 编译验证 - 检查依赖注入配置
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class NavigationModule {
    
    /**
     * 绑定排班模块导航器实现
     */
    @Binds
    @Singleton
    abstract fun bindScheduleNavigator(
        impl: ScheduleNavigatorImpl
    ): ScheduleNavigator
}